package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.TaskService;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.hl7.fhir.dstu3.model.Task.SP_PERIOD;
import static org.hl7.fhir.r4.model.CarePlan.SP_ACTIVITY_DATE;

@RestController
public class TaskResourceProvider implements IResourceProvider {
    private final TaskService service;

    public TaskResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<Task> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<Task> resourceUtil = new ResourceUtil<>();
        this.service = new TaskService(repository, resourceUtil);
    }

    @Override
    public Class<Task> getResourceType() {
        return Task.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createTask(@ResourceParam Task theTask, RequestDetails theRequestDetails) {
        return service.createTask(theTask, theRequestDetails);
    }

    //   OK
    @Read()
    public Task readTask(@IdParam IdType theId) {
        return service.readTask(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateTask(@IdParam IdType theId, @ResourceParam Task theTask) {
        return service.updateTask(theId, theTask);
    }

    @Delete
    public MethodOutcome deleteTask(@IdParam IdType theId) {
        return service.deleteTask(theId);
    }

//    @Search
//    public List<Task> searchTask(@RequiredParam(name = Task.SP_PATIENT) ReferenceParam patientRef){
//        return service.getTasks(patientRef);
//    }
    @Search
    public List<Task> searchTaskByIdentifier(@RequiredParam(name = Condition.SP_IDENTIFIER) TokenParam identifierRef){
        return service.getTasksByIdentifier(identifierRef);
    }

    @Search
    public List<Task> getResourcesByScheduledDay (@RequiredParam(name = Task.SP_PATIENT) ReferenceParam patientRef,
                                                  @RequiredParam(name = SP_PERIOD) DateParam period) {
        return service.getResourcesByScheduledDay(patientRef, period);
    }
}
