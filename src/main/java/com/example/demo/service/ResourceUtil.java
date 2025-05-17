package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class ResourceUtil<T extends DomainResource> {

    public MethodOutcome generateMethodOutcome(OperationOutcome operationOutcome, int statusCode, boolean isCreated) {
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setResponseStatusCode(statusCode);
        methodOutcome.setCreated(isCreated);
        methodOutcome.setOperationOutcome(operationOutcome);
        return methodOutcome;
    }

    public MethodOutcome generateMethodOutcomeWithId(OperationOutcome operationOutcome, IdType theId, int statusCode, boolean isCreated) {
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(theId);
        methodOutcome.setResponseStatusCode(statusCode);
        methodOutcome.setCreated(isCreated);
        methodOutcome.setOperationOutcome(operationOutcome);
        return methodOutcome;
    }

    public MethodOutcome generateMethodOutcomeWithRes(IdType theId, int statusCode, boolean isCreated, T theResource) {
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(theId);
        methodOutcome.setResponseStatusCode(statusCode);
        methodOutcome.setCreated(isCreated);
        methodOutcome.setResource(theResource);
        return methodOutcome;
    }

    public OperationOutcome generateOperationOutcome(OperationOutcome.IssueSeverity severityEnum,
                                                     OperationOutcome.IssueType statusCode, String diagnostics) {
        OperationOutcome operationOutcome = new OperationOutcome();
                 operationOutcome.addIssue()
                .setSeverity(severityEnum)
                .setCode(statusCode)
                .setDiagnostics(diagnostics);
        return operationOutcome;
    }

    public void setMeta(T theResource){
        Meta meta = new Meta();
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theResource.setMeta(meta);
    }

    public String setId(T theResource){
        String theId = UUID.randomUUID().toString();
        theResource.setId(theId);
        return theId;
    }
}