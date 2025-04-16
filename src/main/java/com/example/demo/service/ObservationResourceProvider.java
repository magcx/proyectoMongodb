package com.example.demo.service;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.example.demo.repository.PatientRepository;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ObservationResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final FhirValidator fhirValidator;

    public ObservationResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate,
                                       FhirValidator fhirValidator) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Observation.class;
    }

    @Create
    public MethodOutcome createObservation(@ResourceParam Observation observation) {
        MethodOutcome methodOutcome = new MethodOutcome();
        Observation valObservation = validateObservation(observation);
        if (valObservation == null) {
            methodOutcome.setCreated(false);
            return methodOutcome;
        }
        Document observationDoc = Document.parse(jsonParser.encodeResourceToString(valObservation));
        mongoTemplate.save(observationDoc, "observation");
        methodOutcome.setCreated(true);
        return methodOutcome;
    }

    @Validate
    public Observation validateObservation(@ResourceParam Observation observation) {
        ValidationResult validationResult = fhirValidator.validateWithResult(observation);
        if (!validationResult.isSuccessful()){
            System.out.println("Paciente NO validado");
            return null;
        }
        return observation;
    }

}
