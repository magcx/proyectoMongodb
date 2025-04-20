package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.PatientRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
//TODO(Security attributes inside resources json)
@Service
public class PatientService {
     private final PatientRepository patientRepository;

     public PatientService(PatientRepository patientRepository) {
         this.patientRepository = patientRepository;
     }

//   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
     public MethodOutcome createPatient(Patient thePatient, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(thePatient);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        thePatient.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        thePatient.setMeta(meta);
        return patientRepository.createPatient(thePatient, theRequestDetails, theId, "1");
     }

     public Patient readPatient(IdType theId) {
         return patientRepository.readPatient(theId);
     }

     public MethodOutcome updatePatient(IdType theId, Patient thePatient) {
         OperationOutcome operationOutcome = new OperationOutcome();
         Patient patientUpdated = patientRepository.updatePatient(theId, thePatient);
         if (patientUpdated == null) {
             operationOutcome.addIssue()
                     .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                     .setDiagnostics("Error actualizando paciente");
             return new MethodOutcome(operationOutcome).setCreated(false);
         }
         return new MethodOutcome().setResource(patientUpdated).setId(patientUpdated.getIdElement());
     }

     public MethodOutcome deletePatient(IdType theId) {
         return patientRepository.deletePatient(theId);
     }
//    TODO(El return)

     public OperationOutcome hasIdentifier(Patient thePatient) {
         OperationOutcome operationOutcome = new OperationOutcome();
         if (thePatient.getIdentifier().isEmpty()) {
             operationOutcome.addIssue()
                     .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                     .setCode(OperationOutcome.IssueType.REQUIRED)
                     .setDiagnostics("Identificador requerido");
             return operationOutcome;
         }
         return null;
     }
}