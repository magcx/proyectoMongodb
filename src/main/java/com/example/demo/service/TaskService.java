package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final ResourceRepository<Task> repository;
    private final ResourceUtil<Task> resourceUtil;

    public TaskService(ResourceRepository<Task> repository, ResourceUtil<Task> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }
    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createTask(Task theTask, RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theTask);
        resourceUtil.setMeta(theTask);
        return repository.createFhirResource(theTask, theRequestDetails, theId,
                "task", theTask.getIdentifierFirstRep().getSystem(),
                theTask.getIdentifierFirstRep().getValue());
    }

    public Task readTask(IdType theId) {
        return repository.readFhirResource(theId, "task",
                Task.class);
    }

    public MethodOutcome updateTask(IdType theId, Task theTask) {
        Task resourceUpdated = repository.updateFhirResource(theId, theTask,
                "task",
                Task.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theTask);
    }

    public MethodOutcome deleteTask(IdType theId) {
        return repository.deleteFhirResource(theId,
                "task");
    }

    public List<Task> getTasks(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "task", Task.class);
    }

    public List<Task> getTasksByIdentifier(TokenParam identifierRef) {
        return repository.getAllResourcesByIdentifier(identifierRef, "task", Task.class);
    }

    public List<Task> getResourcesByScheduledDay(ReferenceParam patientRef, DateParam periodDate) {
        return repository.getResourcesByScheduledDay(patientRef, "task", Task.class, periodDate);
    }
}