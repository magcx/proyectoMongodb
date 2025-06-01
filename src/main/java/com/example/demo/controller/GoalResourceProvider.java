package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.GoalService;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GoalResourceProvider implements IResourceProvider {
    private final GoalService service;

    public GoalResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<Goal> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<Goal> resourceUtil = new ResourceUtil<>();
        this.service = new GoalService(repository, resourceUtil);
    }

    @Override
    public Class<Goal> getResourceType() {
        return Goal.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createGoal(@ResourceParam Goal theGoal, RequestDetails theRequestDetails) {
        return service.createGoal(theGoal, theRequestDetails);
    }

    //   OK
    @Read()
    public Goal readGoal(@IdParam IdType theId) {
        return service.readGoal(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateGoal(@IdParam IdType theId, @ResourceParam Goal theGoal) {
        return service.updateGoal(theId, theGoal);
    }

    @Delete
    public MethodOutcome deleteGoal(@IdParam IdType theId) {
        return service.deleteGoal(theId);
    }

    @Search
    public List<Goal> searchGoal(@RequiredParam(name = Goal.SP_PATIENT) ReferenceParam patientRef){
        return service.getGoals(patientRef);
    }
}
