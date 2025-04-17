package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.example.demo.repository.ObservationRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Service;

@Service
public class ObservationService {
    private final ObservationRepository observationRepository;

    public ObservationService(ObservationRepository observationRepository) {
        this.observationRepository = observationRepository;
    }

    public MethodOutcome createObservation(Observation theObservation) {
        return observationRepository.createObservation(theObservation);
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

}
