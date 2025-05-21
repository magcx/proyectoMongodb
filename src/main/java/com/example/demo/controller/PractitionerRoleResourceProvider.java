package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.PractitionerRoleService;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PractitionerRoleResourceProvider implements IResourceProvider {
    private final PractitionerRoleService service;

    public PractitionerRoleResourceProvider (JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<PractitionerRole> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<PractitionerRole> resourceUtil = new ResourceUtil<>();
        this.service = new PractitionerRoleService(repository, resourceUtil);
    }

    @Override
    public Class<PractitionerRole> getResourceType() {
        return PractitionerRole.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPractitionerRole(@ResourceParam PractitionerRole thePractitionerRole, RequestDetails theRequestDetails) {
      return service.createPractitionerRole(thePractitionerRole, theRequestDetails);
    }
    //   OK
    @Read()
    public PractitionerRole readPractitionerRole(@IdParam IdType theId) {
        return service.readPractitionerRole(theId);
    }

    //    OK
    @Update
    public MethodOutcome updatePractitionerRole(@IdParam IdType theId, @ResourceParam PractitionerRole thePractitionerRole) {
        return service.updatePractitionerRole(theId, thePractitionerRole);
    }

    @Delete
    public MethodOutcome deletePractitionerRole(@IdParam IdType theId) {
        return service.deletePractitionerRole(theId);
    }

    @Search
    public List<PractitionerRole> searchPractitionerRoles(){
        return service.getPractitionerRoles();
    }
}