package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.encryption.DataEncryption;
import com.example.demo.encryption.FhirResourceEncryption;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;


@Service
public class PatientService {
     private final ResourceRepository<Patient> repository;
     private final ResourceUtil<Patient> resourceUtil;
     private static final FhirResourceEncryption encryptor = new FhirResourceEncryption();

     public PatientService(ResourceRepository<Patient> repository, ResourceUtil<Patient> resourceUtil) {
         this.repository = repository;
         this.resourceUtil = resourceUtil;
     }

//   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
     public MethodOutcome createPatient(Patient thePatient, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(thePatient);
        if (operationOutcome!= null) {
            return resourceUtil.generateMethodOutcome(operationOutcome, 422, false);
        }
        if (repository.resourceExists(thePatient.getIdentifierFirstRep().getSystem(),
                thePatient.getIdentifierFirstRep().getValue(), "patient") != null) {
            OperationOutcome oo = resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.DUPLICATE, "Duplicate");
            return resourceUtil.generateMethodOutcomeWithId(oo, thePatient.getIdElement(), 422, false);
        }
         SecretKey key = DataEncryption.generateKey();
         if (key == null) {
             OperationOutcome oo = resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.PROCESSING, "fallo en el cifrado de datos");
             return resourceUtil.generateMethodOutcome(oo, 500, false);
         }
        String theId = resourceUtil.setId(thePatient);
        thePatient = encryptPatientData(thePatient, key);
        resourceUtil.setMeta(thePatient);
        return repository.createFhirResource(thePatient, theRequestDetails, theId, "patient",
                thePatient.getIdentifierFirstRep().getSystem(), thePatient.getIdentifierFirstRep().getValue());
     }

     public Patient readPatient(IdType theId) {
         SecretKey key = repository.getSecretKey(theId.getIdPart());
         return decryptPatientData(repository.readFhirResource(theId, "patient", Patient.class),key);
     }

     public MethodOutcome updatePatient(IdType theId, Patient thePatient) {
         SecretKey key = repository.getSecretKey(theId.getIdPart());
         encryptPatientData(thePatient, key);
         Patient resourceUpdated = repository.updateFhirResource(theId, thePatient, "patient",
                                Patient.class);
         if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
         }
         return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                 false, thePatient);
     }

     public MethodOutcome deletePatient(IdType theId) {
         MethodOutcome mo = repository.deleteFhirResource(theId, "patient");
         if (mo.getOperationOutcome() == null) {
             if (!repository.deleteSecretKey(theId.getIdPart())){
                 mo.setOperationOutcome(resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.WARNING,
                         OperationOutcome.IssueType.PROCESSING, "Error eliminando clave"));
             }
         }
         return mo;
     }

     public List<Patient> getPatients() {
         List<Patient> patients = repository.getAllResourcesByType("patient", Patient.class);
         return patients.stream().map(p -> {
             SecretKey key = repository.getSecretKey(p.getIdPart());
             return decryptPatientData(p, key);
         }).toList();
     }

     public OperationOutcome hasIdentifier(Patient thePatient) {
         if (thePatient.getIdentifier().isEmpty() || thePatient.getBirthDate() == null || thePatient.getName().isEmpty()
         || thePatient.getGender() == null) {
             return resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.REQUIRED, "Faltan datos");
         }
         return null;
     }

     public Patient encryptPatientData(Patient thePatient, SecretKey theKey) {
         thePatient.setName(encryptor.processNames(thePatient.getName(), theKey,
                 data -> DataEncryption.encrypt(data, theKey)));
         thePatient.setIdentifier(encryptor.processIdentifiers(thePatient.getIdentifier(), theKey,
                 data -> DataEncryption.encrypt(data, theKey)));
         if (thePatient.hasTelecom()) thePatient.setTelecom(encryptor.processTelecoms(thePatient.getTelecom(), theKey,
                 data -> DataEncryption.encrypt(data, theKey)));
         if (thePatient.hasAddress()) thePatient.setAddress(encryptor.processAddresses(thePatient.getAddress(), theKey,
                 data -> DataEncryption.encrypt(data, theKey)));
         if (thePatient.hasContact()) thePatient.setContact(encryptor.processContacts(thePatient.getContact(), theKey,
                 data -> DataEncryption.encrypt(data, theKey)));
         repository.storeSecretKey(thePatient.getIdPart(), theKey);
         return thePatient;
     }

    public Patient decryptPatientData(Patient thePatient, SecretKey theKey) {
        thePatient.setName(encryptor.processNames(thePatient.getName(), theKey,
                data -> DataEncryption.decrypt(data, theKey)));
        thePatient.setIdentifier(encryptor.processIdentifiers(thePatient.getIdentifier(), theKey,
                data -> DataEncryption.decrypt(data, theKey)));
        if (thePatient.hasTelecom()) thePatient.setTelecom(encryptor.processTelecoms(thePatient.getTelecom(), theKey,
                data -> DataEncryption.decrypt(data, theKey)));
        if (thePatient.hasAddress()) thePatient.setAddress(encryptor.processAddresses(thePatient.getAddress(), theKey,
                data -> DataEncryption.decrypt(data, theKey)));
        if (thePatient.hasContact()) thePatient.setContact(encryptor.processContacts(thePatient.getContact(), theKey,
                data -> DataEncryption.decrypt(data, theKey)));
        return thePatient;
    }
}