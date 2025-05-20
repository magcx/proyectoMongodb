package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.example.demo.repository.*;
import com.example.demo.service.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ObservationResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final ResourceRepository<Observation> repository;
    private final ObservationService service;
    private ResourceUtil<Observation> resourceUtil;

    public ObservationResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        this.resourceUtil = new ResourceUtil<>();
        this.service = new ObservationService(repository, resourceUtil);
    }
    @Override
    public Class<Observation> getResourceType() {

        return Observation.class;
    }

    @Create
    public MethodOutcome createObservation(@ResourceParam Observation theObservation, RequestDetails theRequestDetails) {
        return service.createObservation(theObservation, theRequestDetails);
    }

    @Read
    public Observation readObservation(@IdParam IdType theId) {
        return service.readObservation(theId);
    }

    @Update
    public MethodOutcome updateObservation(@IdParam IdType theId, @ResourceParam Observation theObservation) {
        return service.updateObservation(theId, theObservation);
    }
//    @Search
    @Delete
    public MethodOutcome deleteObservation(@IdParam IdType theId) {
        return service.deleteObservation(theId);
    }

    @Search
    public List<Observation> searchObservations(@RequiredParam(name = Observation.SP_PATIENT) ReferenceParam patientRef) {
        return service.getObservations(patientRef);
    }
}
