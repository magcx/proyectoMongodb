package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.ObservationRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class ObservationService {
    private final ObservationRepository observationRepository;

    public ObservationService(ObservationRepository observationRepository) {
        this.observationRepository = observationRepository;
    }

    public MethodOutcome createObservation(Observation theObservation, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(theObservation);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theObservation.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theObservation.setMeta(meta);
        return observationRepository.createObservation(theObservation, theRequestDetails, theId);
    }

    public Observation readObservation(IdType theId) {
        return observationRepository.readObservation(theId);
    }

    public MethodOutcome updateObservation(IdType theId, Observation theObservation) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Observation observationUpdated = observationRepository.updateObservation(theId, theObservation);
        if (observationUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando observation");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(observationUpdated).setId(observationUpdated.getIdElement());
    }

    public MethodOutcome deleteObservation(IdType theId) {
        return observationRepository.deleteObservation(theId);
    }

    public OperationOutcome hasIdentifier(Observation theObservation) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theObservation.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}