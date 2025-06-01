package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObservationService {
    private final ResourceRepository<Observation> repository;
    private final ResourceUtil<Observation> resourceUtil;

    public ObservationService(ResourceRepository<Observation> repository, ResourceUtil<Observation> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    public MethodOutcome createObservation(Observation theObservation, RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theObservation);
        resourceUtil.setMeta(theObservation);
        return repository.createFhirResource(theObservation, theRequestDetails, theId,
                "observation", theObservation.getIdentifierFirstRep().getSystem(),
                theObservation.getIdentifierFirstRep().getValue());
    }

    public Observation readObservation(IdType theId) {
        return repository.readFhirResource(theId, "observation",
                Observation.class);
    }

    public MethodOutcome updateObservation(IdType theId, Observation theObservation) {
        Observation resourceUpdated = repository.updateFhirResource(theId, theObservation,
                "observation",
                Observation .class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theObservation);
    }

    public MethodOutcome deleteObservation(IdType theId) {
        return repository.deleteFhirResource(theId,
                "observation");
    }

    public List<Observation> getObservations(ReferenceParam patientRef, TokenParam categoryRef) {
        return repository.getAllResourcesByCategory (patientRef, categoryRef, "observation", Observation.class);
    }

    public List<Observation> getObservationsByIdentifier(TokenParam identifierRef) {
        return repository.getAllResourcesByIdentifier(identifierRef, "observation", Observation.class);
    }
}