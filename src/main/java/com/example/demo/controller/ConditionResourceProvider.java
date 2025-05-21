package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.ConditionService;
import com.example.demo.service.ResourceUtil;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConditionResourceProvider implements IResourceProvider {
    private final ConditionService service;

    public ConditionResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<Condition> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<Condition> resourceUtil = new ResourceUtil<>();
        this.service = new ConditionService(repository, resourceUtil);
    }

    @Override
    public Class<Condition> getResourceType() {
        return Condition.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createCondition(@ResourceParam Condition theCondition, RequestDetails theRequestDetails) {
        return service.createCondition(theCondition, theRequestDetails);
    }

    //   OK
    @Read()
    public Condition readCondition(@IdParam IdType theId) {
        return service.readCondition(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateCondition(@IdParam IdType theId, @ResourceParam Condition theCondition) {
        return service.updateCondition(theId, theCondition);
    }

    @Delete
    public MethodOutcome deleteCondition(@IdParam IdType theId) {
        return service.deleteCondition(theId);
    }

    @Search
    public List<Condition> searchCondition(@RequiredParam(name = Condition.SP_PATIENT) ReferenceParam patientRef){
        return service.getConditions(patientRef);
    }
}