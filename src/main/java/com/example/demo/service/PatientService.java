package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

//TODO(Security attributes inside resources json)
@Service
public class PatientService {
     private final ResourceRepository<Patient> patientRepository;
     private final ResourceUtil<Patient> serviceUtil;

     public PatientService(ResourceRepository<Patient> patientRepository, ResourceUtil<Patient> patientServiceUtil) {
         this.patientRepository = patientRepository;
         this.serviceUtil = patientServiceUtil;
     }

//   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
     public MethodOutcome createPatient(Patient thePatient, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(thePatient);
        if (operationOutcome!= null) {
            return serviceUtil.generateMethodOutcome(operationOutcome, 422, false);
        }
        String theId = serviceUtil.setId(thePatient);
        serviceUtil.setMeta(thePatient);
        return patientRepository.createFhirResource(thePatient, theRequestDetails, theId, "patient",
                thePatient.getIdentifierFirstRep().getSystem(), thePatient.getIdentifierFirstRep().getValue());
     }

     public Patient readPatient(IdType theId) {
         return patientRepository.readFhirResource(theId, "patient",
                 Patient.class);
     }

     public MethodOutcome updatePatient(IdType theId, Patient thePatient) {
         Patient patientUpdated = patientRepository.updateFhirResource(theId, thePatient, "patient",
                                Patient.class);
         if (patientUpdated == null) {
            OperationOutcome oo =  serviceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return serviceUtil.generateMethodOutcome(oo, 418, false);
         }
         return serviceUtil.generateMethodOutcomeWithRes(patientUpdated.getIdElement(), 200,
                 false, thePatient);
     }

     public MethodOutcome deletePatient(IdType theId) {
         return patientRepository.deleteFhirResource(theId,
                 "patient");
     }
//    TODO(El return)

     public List<Patient> getPatients() {
         return patientRepository.getAllResourcesByType("patient", Patient.class);
     }

     public OperationOutcome hasIdentifier(Patient thePatient) {
         if (thePatient.getIdentifier().isEmpty()) {
             return serviceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.REQUIRED, "Identificador requerido");
         }
         return null;
     }
}