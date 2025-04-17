package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.example.demo.repository.ProcedureRepository;
import com.example.demo.service.ProcedureService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ProcedureResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final ProcedureRepository procedureRepository;
    private final FhirValidator fhirValidator;
    private final ProcedureService procedureService;

    public ProcedureResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate, FhirValidator fhirValidator) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
        this.procedureRepository = new ProcedureRepository(mongoTemplate, jsonParser);
        this.procedureService = new ProcedureService(procedureRepository);
    }

    @Override
    public Class<Procedure> getResourceType() {
        return Procedure.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createProcedure(@ResourceParam Procedure theProcedure) {
        Procedure procedureValidated = validateProcedure(theProcedure);
        if (procedureValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return procedureService.createProcedure(theProcedure);
    }

    //   OK
    @Read()
    public Procedure readProcedure(@IdParam IdType theId) {
        return procedureService.readProcedure(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateProcedure(@IdParam IdType theId, @ResourceParam Procedure theProcedure) {
        Procedure procedureValidated = validateProcedure(theProcedure);
        if (procedureValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return procedureService.updateProcedure(theId, theProcedure);
    }

    @Delete
    public MethodOutcome deleteProcedure(@IdParam IdType theId) {
        return procedureService.deleteProcedure(theId);
    }

    //
//    @Search
//    public OperationOutcome searchProcedure(){
//        return null;
//    }
//  TODO ("the "validate" operation requires a response of HTTP 422 Unprocessable Entity if the validation fails")
//  Tiene que devolver un Procedure
    @Validate
    public Procedure validateProcedure(@ResourceParam Procedure theProcedure) {
        ValidationResult validationResult = fhirValidator.validateWithResult(theProcedure);
        if (!validationResult.isSuccessful()) {
            return null;
        }
        return theProcedure;
    }
}