package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.*;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.example.demo.service.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PatientResourceProvider implements IResourceProvider {
    private final PatientService service;

    public PatientResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<Patient> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<Patient> resourceUtil = new ResourceUtil<>();
        this.service = new PatientService(repository, resourceUtil);
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient, RequestDetails theRequestDetails) {
        return service.createPatient(thePatient, theRequestDetails);
    }

    //   OK
    @Read()
    public Patient readPatient(@IdParam IdType theId) {
        return service.readPatient(theId);
    }

    //    OK
    @Update
    public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient) {
        return service.updatePatient(theId, thePatient);
    }

    @Delete
    public MethodOutcome deletePatient(@IdParam IdType theId) {
        return service.deletePatient(theId);
    }

    @Search
    public List<Patient> searchPatient(){
        return service.getPatients();
    }
}