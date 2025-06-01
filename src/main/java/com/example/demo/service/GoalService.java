package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {
    private final ResourceRepository<Goal> repository;
    private final ResourceUtil<Goal> resourceUtil;

    public GoalService(ResourceRepository<Goal> repository, ResourceUtil<Goal> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }
    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createGoal(Goal theGoal, RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theGoal);
        resourceUtil.setMeta(theGoal);
        return repository.createFhirResource(theGoal, theRequestDetails, theId,
                "goal", theGoal.getIdentifierFirstRep().getSystem(),
                theGoal.getIdentifierFirstRep().getValue());
    }

    public Goal readGoal(IdType theId) {
        return repository.readFhirResource(theId, "goal",
                Goal.class);
    }

    public MethodOutcome updateGoal(IdType theId, Goal theGoal) {
        Goal resourceUpdated = repository.updateFhirResource(theId, theGoal,
                "goal",
                Goal.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theGoal);
    }

    public MethodOutcome deleteGoal(IdType theId) {
        return repository.deleteFhirResource(theId,
                "goal");
    }

    public List<Goal> getGoals(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "goal", Goal.class);
    }
}