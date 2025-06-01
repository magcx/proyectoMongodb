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
public class ConditionService {
    private final ResourceRepository<Condition> repository;
    private final ResourceUtil<Condition> resourceUtil;

    public ConditionService(ResourceRepository<Condition> repository, ResourceUtil<Condition> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }
    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createCondition(Condition theCondition, RequestDetails theRequestDetails) {
            String theId = resourceUtil.setId(theCondition);
            resourceUtil.setMeta(theCondition);
            return repository.createFhirResource(theCondition, theRequestDetails, theId,
                    "condition", theCondition.getIdentifierFirstRep().getSystem(),
                    theCondition.getIdentifierFirstRep().getValue());
    }

    public Condition readCondition(IdType theId) {
        return repository.readFhirResource(theId, "condition",
                Condition.class);
    }

    public MethodOutcome updateCondition(IdType theId, Condition theCondition) {
        Condition resourceUpdated = repository.updateFhirResource(theId, theCondition,
                "condition",
                Condition.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theCondition);
    }

    public MethodOutcome deleteCondition(IdType theId) {
        return repository.deleteFhirResource(theId,
                "condition");
    }

    public List<Condition> getConditions(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "condition", Condition.class);
    }

    public List<Condition> getConditionsByIdentifier(TokenParam identifierRef) {
        return repository.getAllResourcesByIdentifier(identifierRef, "condition", Condition.class);
    }
}