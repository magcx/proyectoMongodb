package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import com.example.demo.repository.*;
import com.example.demo.service.*;

@Controller
public class ObservationResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final ObservationRepository observationRepository;
    private final ObservationService observationService;

    public ObservationResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.observationRepository = new ObservationRepository(mongoTemplate, jsonParser);
        this.observationService = new ObservationService(observationRepository);
    }
    @Override
    public Class<Observation> getResourceType() {

        return Observation.class;
    }

    @Create
    public MethodOutcome createObservation(@ResourceParam Observation theObservation) {
        return observationService.createObservation(theObservation);
    }

    @Read
    public Observation readObservation(@IdParam IdType theId) {
        return observationService.readObservation(theId);
    }

    @Update
    public MethodOutcome updateObservation(@IdParam IdType theId, @ResourceParam Observation theObservation) {
        return observationService.updateObservation(theId, theObservation);
    }
//    @Search
    @Delete
    public MethodOutcome deleteObservation(@IdParam IdType theId) {
        return observationService.deleteObservation(theId);
    }
}
