package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.demo.repository.ObservationRepository;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ObservationService {
    private final ResourceRepository<Observation> repository;
    private final ResourceUtil<Observation> resourceUtil;

    public ObservationService(ResourceRepository<Observation> repository, ResourceUtil<Observation> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    public MethodOutcome createObservation(Observation theObservation, RequestDetails theRequestDetails) {
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theObservation.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theObservation.setMeta(meta);
        return repository.createFhirResource(theObservation, theRequestDetails, theId, "observation",
                null, null);
    }

    public Observation readObservation(IdType theId) {
        return repository.readFhirResource(theId, "observation",
                Observation.class);
    }

    public MethodOutcome updateObservation(IdType theId, Observation theObservation) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Observation observationUpdated = repository.updateFhirResource(theId, theObservation, "observation",
                Observation.class);;
        if (observationUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando observation");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(observationUpdated.getIdElement(), 200,
                false, theObservation);
    }

    public MethodOutcome deleteObservation(IdType theId) {
        return repository.deleteFhirResource(theId,
                "observation");
    }

    public List<Observation> getObservations(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "observation", Observation.class);
    }

}