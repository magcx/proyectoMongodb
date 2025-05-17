package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.AllergyIntoleranceRepository;
import com.example.demo.service.AllergyIntoleranceService;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AllergyIntoleranceResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final AllergyIntoleranceRepository allergyIntoleranceRepository;
    private final AllergyIntoleranceService allergyIntoleranceService;

    public AllergyIntoleranceResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.allergyIntoleranceRepository = new AllergyIntoleranceRepository(mongoTemplate, jsonParser);
        this.allergyIntoleranceService = new AllergyIntoleranceService(allergyIntoleranceRepository);
    }

    @Override
    public Class<AllergyIntolerance> getResourceType() {
        return AllergyIntolerance.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createAllergyIntolerance(@ResourceParam AllergyIntolerance theAllergyIntolerance, RequestDetails theRequestDetails) {
        return allergyIntoleranceService.createAllergyIntolerance(theAllergyIntolerance, theRequestDetails);
    }

    //   OK
    @Read()
    public AllergyIntolerance readAllergyIntolerance(@IdParam IdType theId) {
        return allergyIntoleranceService.readAllergyIntolerance(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateAllergyIntolerance(@IdParam IdType theId, @ResourceParam AllergyIntolerance theAllergyIntolerance) {
        return allergyIntoleranceService.updateAllergyIntolerance(theId, theAllergyIntolerance);
    }

    @Delete
    public MethodOutcome deleteAllergyIntolerance(@IdParam IdType theId) {
        return allergyIntoleranceService.deleteAllergyIntolerance(theId);
    }

    @Search
    public List<AllergyIntolerance> searchAllergiesIntolerances(@RequiredParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patientRef) {
        return allergyIntoleranceService.getAllergiesIntolerances(patientRef);
    }
}