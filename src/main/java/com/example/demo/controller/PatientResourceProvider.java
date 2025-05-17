package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.*;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.example.demo.service.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PatientResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private ResourceRepository<Patient> patientRepository;
    private ResourceUtil<Patient> patientServiceUtil;
    private PatientService patientService;


    public PatientResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.patientRepository = new ResourceRepository<>(mongoTemplate, jsonParser);
        this.patientServiceUtil = new ResourceUtil<>();
        this.patientService = new PatientService(patientRepository, patientServiceUtil);
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient, RequestDetails theRequestDetails) {
        return patientService.createPatient(thePatient, theRequestDetails);
    }

    //   OK
    @Read()
    public Patient readPatient(@IdParam IdType theId) {
        return patientService.readPatient(theId);
    }

    //    OK
    @Update
    public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient) {
        return patientService.updatePatient(theId, thePatient);
    }

    @Delete
    public MethodOutcome deletePatient(@IdParam IdType theId) {
        return patientService.deletePatient(theId);
    }

    @Search
    public List<Patient> searchPatient(){
        return patientService.getPatients();
    }
}