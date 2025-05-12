package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.MedicationRepository;
import com.example.demo.service.MedicationService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicationResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final MedicationRepository medicationRepository;
    private final MedicationService medicationService;

    public MedicationResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.medicationRepository = new MedicationRepository(mongoTemplate, jsonParser);
        this.medicationService = new MedicationService(medicationRepository);
    }

    @Override
    public Class<Medication> getResourceType() {
        return Medication.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createMedication(@ResourceParam Medication theMedication, RequestDetails theRequestDetails) {
        return medicationService.createMedication(theMedication, theRequestDetails);
    }

    //   OK
    @Read()
    public Medication readMedication(@IdParam IdType theId) {
        return medicationService.readMedication(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateMedication(@IdParam IdType theId, @ResourceParam Medication theMedication) {
        return medicationService.updateMedication(theId, theMedication);
    }

    @Delete
    public MethodOutcome deleteMedication(@IdParam IdType theId) {
        return medicationService.deleteMedication(theId);
    }

    @Search
    public List<Medication> searchMedication(){
        return medicationService.getMedications();
    }
}
