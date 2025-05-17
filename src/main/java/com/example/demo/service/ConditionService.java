package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ConditionRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ConditionService {
    private final ConditionRepository conditionRepository;
//TODO(setMeta
    public ConditionService(ConditionRepository conditionRepository) {
        this.conditionRepository = conditionRepository;
    }

    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createCondition(Condition theCondition, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(theCondition);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theCondition.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theCondition.setMeta(meta);
        return conditionRepository.createCondition(theCondition, theRequestDetails, theId);
    }

    public Condition readCondition(IdType theId) {
        return conditionRepository.readCondition(theId);
    }

    public MethodOutcome updateCondition(IdType theId, Condition theCondition) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Condition conditionUpdated = conditionRepository.updateCondition(theId, theCondition);
        if (conditionUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(conditionUpdated).setId(conditionUpdated.getIdElement());
    }

    public MethodOutcome deleteCondition(IdType theId) {
        return conditionRepository.deleteCondition(theId);
    }
//    TODO(El return)

    public List<Condition> getConditions(ReferenceParam patientRef) {
        return conditionRepository.getConditions(patientRef);
    }

    public OperationOutcome hasIdentifier(Condition theCondition) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theCondition.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}