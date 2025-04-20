package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ProcedureRepository;
import com.example.demo.service.ProcedureService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcedureResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final ProcedureRepository procedureRepository;
    private final ProcedureService procedureService;

    public ProcedureResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
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
    public MethodOutcome createProcedure(@ResourceParam Procedure theProcedure, RequestDetails theRequestDetails) {

        return procedureService.createProcedure(theProcedure, theRequestDetails);
    }

    //   OK
    @Read()
    public Procedure readProcedure(@IdParam IdType theId) {
        return procedureService.readProcedure(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateProcedure(@IdParam IdType theId, @ResourceParam Procedure theProcedure) {
        return procedureService.updateProcedure(theId, theProcedure);
    }

    @Delete
    public MethodOutcome deleteProcedure(@IdParam IdType theId) {
        return procedureService.deleteProcedure(theId);
    }

//    @Search
//    public OperationOutcome searchProcedure(){
//        return null;
//    }
}