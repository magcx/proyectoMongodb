package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.ProcedureService;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProcedureResourceProvider implements IResourceProvider {
    private final ProcedureService service;

    public ProcedureResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<Procedure> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<Procedure> resourceUtil = new ResourceUtil<>();
        this.service = new ProcedureService(repository, resourceUtil);
    }

    @Override
    public Class<Procedure> getResourceType() {
        return Procedure.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createProcedure(@ResourceParam Procedure theProcedure, RequestDetails theRequestDetails) {

        return service.createProcedure(theProcedure, theRequestDetails);
    }

    //   OK
    @Read()
    public Procedure readProcedure(@IdParam IdType theId) {
        return service.readProcedure(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateProcedure(@IdParam IdType theId, @ResourceParam Procedure theProcedure) {
        return service.updateProcedure(theId, theProcedure);
    }

    @Delete
    public MethodOutcome deleteProcedure(@IdParam IdType theId) {
        return service.deleteProcedure(theId);
    }

    @Search
    public List<Procedure> searchProcedures(@RequiredParam(name = Procedure.SP_PATIENT) ReferenceParam patientRef){
        return service.getProcedures(patientRef);
    }
}