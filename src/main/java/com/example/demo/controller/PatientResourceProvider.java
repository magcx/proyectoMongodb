package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.example.demo.repository.PatientRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import com.example.demo.service.*;

@Controller
public class PatientResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final PatientRepository patientRepository;
    private final FhirValidator fhirValidator;
    private final PatientService patientService;

    public PatientResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate, FhirValidator fhirValidator) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
        this.patientRepository = new PatientRepository(mongoTemplate, jsonParser);
        this.patientService = new PatientService(patientRepository);
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
        Patient patientValidated = validatePatient(thePatient);
        if (patientValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return patientService.createPatient(thePatient);
    }

    //   OK
    @Read()
    public Patient readPatient(@IdParam IdType theId) {
        return patientService.readPatient(theId);
    }

    //    OK
    @Update
    public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient) {
        Patient patientValidated = validatePatient(thePatient);
        if (patientValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return patientService.updatePatient(theId, thePatient);
    }

    @Delete
    public MethodOutcome deletePatient(@IdParam IdType theId) {
        return patientService.deletePatient(theId);
    }

    //
//    @Search
//    public OperationOutcome searchPatient(){
//        return null;
//    }
//  TODO (the "validate" operation requires a response of HTTP 422 Unprocessable Entity if the validation fails)
//  Tiene que devolver un Patient
    @Validate
    public Patient validatePatient(@ResourceParam Patient thePatient) {
        ValidationResult validationResult = fhirValidator.validateWithResult(thePatient);
        if (!validationResult.isSuccessful()) {
            return null;
        }
        return thePatient;
    }
}