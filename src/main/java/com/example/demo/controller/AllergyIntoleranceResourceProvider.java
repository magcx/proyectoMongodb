package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.AllergyIntoleranceService;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AllergyIntoleranceResourceProvider implements IResourceProvider {
    private final AllergyIntoleranceService service;

    public AllergyIntoleranceResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<AllergyIntolerance> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<AllergyIntolerance> resourceUtil = new ResourceUtil<>();
        this.service = new AllergyIntoleranceService(repository, resourceUtil);
    }

    @Override
    public Class<AllergyIntolerance> getResourceType() {
        return AllergyIntolerance.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createAllergyIntolerance(@ResourceParam AllergyIntolerance theAllergyIntolerance,
                                                  RequestDetails theRequestDetails) {
        return service.createAllergyIntolerance(theAllergyIntolerance, theRequestDetails);
    }

    //   OK
    @Read()
    public AllergyIntolerance readAllergyIntolerance(@IdParam IdType theId) {
        return service.readAllergyIntolerance(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateAllergyIntolerance(@IdParam IdType theId,
                                                  @ResourceParam AllergyIntolerance theAllergyIntolerance) {
        return service.updateAllergyIntolerance(theId, theAllergyIntolerance);
    }

    @Delete
    public MethodOutcome deleteAllergyIntolerance(@IdParam IdType theId) {
        return service.deleteAllergyIntolerance(theId);
    }

    @Search
    public List<AllergyIntolerance> searchAllergiesIntolerances(@RequiredParam(name = AllergyIntolerance.SP_PATIENT)
                                                                    ReferenceParam patientRef) {
        return service.getAllergiesIntolerances(patientRef);
    }
}