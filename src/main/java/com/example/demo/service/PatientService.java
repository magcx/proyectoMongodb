package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
     private final ResourceRepository<Patient> repository;
     private final ResourceUtil<Patient> resourceUtil;

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
        String theId = resourceUtil.setId(thePatient);
        resourceUtil.setMeta(thePatient);
        return repository.createFhirResource(thePatient, theRequestDetails, theId, "patient",
                thePatient.getIdentifierFirstRep().getSystem(), thePatient.getIdentifierFirstRep().getValue());
     }

     public Patient readPatient(IdType theId) {
         return repository.readFhirResource(theId, "patient",
                 Patient.class);
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
         return repository.deleteFhirResource(theId,
                 "patient");
     }

     public List<Patient> getPatients() {
         return repository.getAllResourcesByType("patient",
                 Patient.class);
     }

     public OperationOutcome hasIdentifier(Patient thePatient) {
         if (thePatient.getIdentifier().isEmpty()) {
             return resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.REQUIRED, "Identificador requerido");
         }
         return null;
     }
}