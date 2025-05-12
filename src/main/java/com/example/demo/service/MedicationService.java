package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.MedicationRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MedicationService {
    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createMedication(Medication theMedication, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(theMedication);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theMedication.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theMedication.setMeta(meta);
        return medicationRepository.createMedication(theMedication, theRequestDetails, theId);
    }

    public Medication readMedication(IdType theId) {
        return medicationRepository.readMedication(theId);
    }

    public MethodOutcome updateMedication(IdType theId, Medication theMedication) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Medication medicationUpdated = medicationRepository.updateMedication(theId, theMedication);
        if (medicationUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando recurso");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(medicationUpdated).setId(medicationUpdated.getIdElement());
    }

    public MethodOutcome deleteMedication(IdType theId) {
        return medicationRepository.deleteMedication(theId);
    }
//    TODO(El return)

    public List<Medication> getMedications() {
        return medicationRepository.getMedications();
    }

    public OperationOutcome hasIdentifier(Medication theMedication) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theMedication.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}