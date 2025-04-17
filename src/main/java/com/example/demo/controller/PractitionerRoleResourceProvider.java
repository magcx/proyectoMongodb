package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.example.demo.repository.PractitionerRoleRepository;
import com.example.demo.service.PractitionerRoleService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class PractitionerRoleResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final PractitionerRoleRepository practitionerRoleRepository;
    private final FhirValidator fhirValidator;
    private final PractitionerRoleService practitionerRoleService;

    public PractitionerRoleResourceProvider (JsonParser jsonParser, MongoTemplate mongoTemplate, FhirValidator fhirValidator) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
        this.practitionerRoleRepository = new PractitionerRoleRepository(mongoTemplate, jsonParser);
        this.practitionerRoleService = new PractitionerRoleService(practitionerRoleRepository);
    }

    @Override
    public Class<PractitionerRole> getResourceType() {
        return PractitionerRole.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPractitionerRole(@ResourceParam PractitionerRole thePractitionerRole) {
        PractitionerRole practitionerRoleValidated = validatePractitionerRole(thePractitionerRole);
        if (practitionerRoleValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return practitionerRoleService.createPractitionerRole(thePractitionerRole);
    }

    //   OK
    @Read()
    public PractitionerRole readPractitionerRole(@IdParam IdType theId) {
        return practitionerRoleService.readPractitionerRole(theId);
    }

    //    OK
    @Update
    public MethodOutcome updatePractitionerRole(@IdParam IdType theId, @ResourceParam PractitionerRole thePractitionerRole) {
        PractitionerRole practitionerRoleValidated = validatePractitionerRole(thePractitionerRole);
        if (practitionerRoleValidated == null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.INVALID);
            throw new UnprocessableEntityException("Recurso no apto para validación", operationOutcome);
        }
        return practitionerRoleService.updatePractitionerRole(theId, thePractitionerRole);
    }

    @Delete
    public MethodOutcome deletePractitionerRole(@IdParam IdType theId) {
        return practitionerRoleService.deletePractitionerRole(theId);
    }
    //
//    @Search
//    public OperationOutcome searchPractitionerRole(){
//        return null;
//    }
//  TODO (the "validate" operation requires a response of HTTP 422 Unprocessable Entity if the validation fails)
//  Tiene que devolver un PractitionerRole
    @Validate
    public PractitionerRole validatePractitionerRole(@ResourceParam PractitionerRole thePractitionerRole) {
        ValidationResult validationResult = fhirValidator.validateWithResult(thePractitionerRole);
        if (!validationResult.isSuccessful()) {
            return null;
        }
        return thePractitionerRole;
    }
}