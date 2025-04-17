package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.example.demo.repository.CarePlanRepository;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import com.example.demo.service.*;

@Controller
public class CarePlanResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final CarePlanRepository carePlanRepository;
    private final FhirValidator fhirValidator;
    private final CarePlanService carePlanService;

    public CarePlanResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate, FhirValidator fhirValidator) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
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
        CarePlan carePlanValidated = validateCarePlan(theCarePlan);
        if (carePlanValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
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
        CarePlan carePlanValidated = validateCarePlan(theCarePlan);
        if (carePlanValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return carePlanService.updateCarePlan(theId, theCarePlan);
    }

    @Delete
    public MethodOutcome deleteCarePlan(@IdParam IdType theId) {
        return carePlanService.deleteCarePlan(theId);
    }

    //
//    @Search
//    public OperationOutcome searchCarePlan(){
//        return null;
//    }
//  TODO (the "validate" operation requires a response of HTTP 422 Unprocessable Entity if the validation fails)
//  Tiene que devolver un CarePlan
    @Validate
    public CarePlan validateCarePlan(@ResourceParam CarePlan theCarePlan) {
        ValidationResult validationResult = fhirValidator.validateWithResult(theCarePlan);
        if (!validationResult.isSuccessful()) {
            return null;
        }
        return theCarePlan;
    }
}