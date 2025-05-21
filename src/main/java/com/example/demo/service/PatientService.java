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
         return repository.readFhirResource(theId, "patient", Patient.class);
     }

     public MethodOutcome updatePatient(IdType theId, Patient thePatient) {
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
         return repository.deleteFhirResource(theId, "patient");
     }

     public List<Patient> getPatients() {
         return repository.getAllResourcesByType("patient", Patient.class);
     }

     public OperationOutcome hasIdentifier(Patient thePatient) {
         if (thePatient.getIdentifier().isEmpty()) {
             return resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.REQUIRED, "Identificador requerido");
         }
         return null;
     }

     public Patient encryptPatientData(Patient thePatient, SecretKey theKey) {
         thePatient.setName(encryptor.encryptName(thePatient.getName(), theKey));
         thePatient.setIdentifier(encryptor.encryptIdentifier(thePatient.getIdentifier(), theKey));
         if (thePatient.hasTelecom()) thePatient.setTelecom(encryptor.encryptTelecom(thePatient.getTelecom(), theKey));
         if (thePatient.hasAddress()) thePatient.setAddress(encryptor.encryptAddress(thePatient.getAddress(), theKey));
         if (thePatient.hasContact()) thePatient.setContact(encryptor.encryptContact(thePatient.getContact(), theKey));
         repository.storeSecretKey(thePatient.getIdPart(), theKey);
         return thePatient;
     }
}