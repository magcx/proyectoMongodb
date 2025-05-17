package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.MedicationStatementRepository;
import com.example.demo.service.MedicationStatementService;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicationStatementResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final MedicationStatementRepository medicationStatementRepository;
    private final MedicationStatementService medicationStatementService;

    public MedicationStatementResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.medicationStatementRepository = new MedicationStatementRepository(mongoTemplate, jsonParser);
        this.medicationStatementService = new MedicationStatementService(medicationStatementRepository);
    }

    @Override
    public Class<MedicationStatement> getResourceType() {
        return MedicationStatement.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createMedicationStatement(@ResourceParam MedicationStatement theMedicationStatement, RequestDetails theRequestDetails) {
        return medicationStatementService.createMedicationStatement(theMedicationStatement, theRequestDetails);
    }

    //   OK
    @Read()
    public MedicationStatement readMedicationStatement(@IdParam IdType theId) {
        return medicationStatementService.readMedicationStatement(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateMedicationStatement(@IdParam IdType theId, @ResourceParam MedicationStatement theMedicationStatement) {
        return medicationStatementService.updateMedicationStatement(theId, theMedicationStatement);
    }

    @Delete
    public MethodOutcome deleteMedicationStatement(@IdParam IdType theId) {
        return medicationStatementService.deleteMedicationStatement(theId);
    }

    @Search
    public List<MedicationStatement> searchMedicationStatements(@RequiredParam(name = MedicationStatement.SP_PATIENT) ReferenceParam patientRef){
        return medicationStatementService.getMedicationStatements(patientRef);
    }
}