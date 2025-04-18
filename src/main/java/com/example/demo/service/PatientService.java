package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.example.demo.repository.PatientRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
     private final PatientRepository patientRepository;

     public PatientService(PatientRepository patientRepository) {
         this.patientRepository = patientRepository;
     }

     public MethodOutcome createPatient(Patient thePatient) {
            OperationOutcome operationOutcome = hasIdentifier(thePatient);
            if (operationOutcome!= null) {
             return new MethodOutcome(thePatient.getIdElement(), operationOutcome, false);
            }
            return patientRepository.createPatient(thePatient);
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