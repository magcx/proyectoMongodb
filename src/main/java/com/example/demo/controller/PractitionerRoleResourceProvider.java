package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.PractitionerRoleRepository;
import com.example.demo.service.PractitionerRoleService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class PractitionerRoleResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final PractitionerRoleRepository practitionerRoleRepository;
    private final PractitionerRoleService practitionerRoleService;

    public PractitionerRoleResourceProvider (JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
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
        return practitionerRoleService.updatePractitionerRole(theId, thePractitionerRole);
    }

    @Delete
    public MethodOutcome deletePractitionerRole(@IdParam IdType theId) {
        return practitionerRoleService.deletePractitionerRole(theId);
    }

//    @Search
//    public OperationOutcome searchPractitionerRole(){
//        return null;
//    }
}