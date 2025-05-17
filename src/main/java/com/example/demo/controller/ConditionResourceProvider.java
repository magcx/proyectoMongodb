package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.service.ConditionService;
import com.example.demo.repository.ConditionRepository;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConditionResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final ConditionRepository conditionRepository;
    private final ConditionService conditionService;

    public ConditionResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.conditionRepository = new ConditionRepository(mongoTemplate, jsonParser);
        this.conditionService = new ConditionService(conditionRepository);
    }

    @Override
    public Class<Condition> getResourceType() {
        return Condition.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createCondition(@ResourceParam Condition theCondition, RequestDetails theRequestDetails) {
        return conditionService.createCondition(theCondition, theRequestDetails);
    }

    //   OK
    @Read()
    public Condition readCondition(@IdParam IdType theId) {
        return conditionService.readCondition(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateCondition(@IdParam IdType theId, @ResourceParam Condition theCondition) {
        return conditionService.updateCondition(theId, theCondition);
    }

    @Delete
    public MethodOutcome deleteCondition(@IdParam IdType theId) {
        return conditionService.deleteCondition(theId);
    }

    @Search
    public List<Condition> searchCondition(@RequiredParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patientRef){
        return conditionService.getConditions(patientRef);
    }
}