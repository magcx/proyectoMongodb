package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.*;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.example.demo.service.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.hl7.fhir.r4.model.CarePlan.SP_ACTIVITY_DATE;

@RestController
public class CarePlanResourceProvider implements IResourceProvider {
    private final CarePlanService service;


    @Autowired
    public CarePlanResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<CarePlan> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<CarePlan> resourceUtil = new ResourceUtil<>();
        this.service = new CarePlanService(repository, resourceUtil);
    }

    @Override
    public Class<CarePlan> getResourceType() {
        return CarePlan.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createCarePlan(@ResourceParam CarePlan theCarePlan, RequestDetails theRequestDetails) {
        return service.createCarePlan(theCarePlan, theRequestDetails);
    }

    //   OK
    @Read()
    public CarePlan readCarePlan(@IdParam IdType theId) {
        return service.readCarePlan(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateCarePlan(@IdParam IdType theId, @ResourceParam CarePlan theCarePlan) {
        return service.updateCarePlan(theId, theCarePlan);
    }

    @Delete
    public MethodOutcome deleteCarePlan(@IdParam IdType theId) {
        return service.deleteCarePlan(theId);
    }

    @Search
    public List<CarePlan> searchCarePlans(@RequiredParam(name = CarePlan.SP_PATIENT)
                                          ReferenceParam patientRef) {
        return service.getCarePlans(patientRef);
    }

    @Search
    public List<CarePlan> getResourcesByScheduledDay (@RequiredParam(name = CarePlan.SP_PATIENT)
                                                          ReferenceParam patientRef, @RequiredParam(name = SP_ACTIVITY_DATE)
                                                      DateParam activityDate) {
        return service.getResourcesByScheduledDay(patientRef, activityDate);
    }
}