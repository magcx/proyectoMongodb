package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.MedicationStatementService;
import com.example.demo.service.ResourceUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicationStatementResourceProvider implements IResourceProvider {
    private final MedicationStatementService service;

    public MedicationStatementResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<MedicationStatement> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<MedicationStatement> resourceUtil = new ResourceUtil<>();
        this.service = new MedicationStatementService(repository, resourceUtil);
    }

    @Override
    public Class<MedicationStatement> getResourceType() {
        return MedicationStatement.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createMedicationStatement(@ResourceParam MedicationStatement theMedicationStatement,
                                                   RequestDetails theRequestDetails) {
        return service.createMedicationStatement(theMedicationStatement, theRequestDetails);
    }

    //   OK
    @Read()
    public MedicationStatement readMedicationStatement(@IdParam IdType theId) {
        return service.readMedicationStatement(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateMedicationStatement(@IdParam IdType theId,
                                                   @ResourceParam MedicationStatement theMedicationStatement) {
        return service.updateMedicationStatement(theId, theMedicationStatement);
    }

    @Delete
    public MethodOutcome deleteMedicationStatement(@IdParam IdType theId) {
        return service.deleteMedicationStatement(theId);
    }

    @Search
    public List<MedicationStatement> searchMedicationStatements(@RequiredParam(name = MedicationStatement.SP_PATIENT)
                                                                    ReferenceParam patientRef){
        return service.getMedicationStatements(patientRef);
    }
}