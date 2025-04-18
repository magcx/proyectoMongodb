package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import com.example.demo.repository.CarePlanRepository;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import com.example.demo.service.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CarePlanResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final CarePlanRepository carePlanRepository;
    private final CarePlanService carePlanService;

    @Autowired
    public CarePlanResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.carePlanRepository = new CarePlanRepository(mongoTemplate, jsonParser);
        this.carePlanService = new CarePlanService(carePlanRepository);
    }

    @Override
    public Class<CarePlan> getResourceType() {
        return CarePlan.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createCarePlan(@ResourceParam CarePlan theCarePlan) {
        return carePlanService.createCarePlan(theCarePlan);
    }

    //   OK
    @Read()
    public CarePlan readCarePlan(@IdParam IdType theId) {
        return carePlanService.readCarePlan(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateCarePlan(@IdParam IdType theId, @ResourceParam CarePlan theCarePlan) {
        return carePlanService.updateCarePlan(theId, theCarePlan);
    }

    @Delete
    public MethodOutcome deleteCarePlan(@IdParam IdType theId) {
        return carePlanService.deleteCarePlan(theId);
    }

//    @Search
//    public OperationOutcome searchCarePlan(){
//        return null;
//    }
}