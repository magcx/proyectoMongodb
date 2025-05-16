package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MedicationRepository {
    private MongoTemplate mongoTemplate;
    private JsonParser jsonParser;

    //TODO(Encriptar datos sensibles)
    public MedicationRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }
    //    OK-ish - optimizar
    public MethodOutcome createMedication(Medication theMedication, RequestDetails theRequestDetails, String theId) {
        if (medicationFound(theMedication) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate");
            MethodOutcome methodOutcome = new MethodOutcome(theMedication.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theMedication)),
                "medication");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theMedication);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "Medication",
                theId, "1"));
        return methodOutcome;
    }

    public Medication readMedication(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonMedication = mongoTemplate.findOne(new Query(criteria), String.class,"medication");
        if (jsonMedication == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(Medication.class, jsonMedication);
    }
    //    OK-ish - optimizar
    public Medication updateMedication(IdType theId, Medication theMedication){
        Medication medicationFound = readMedication(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document medicationDoc = Document.parse(jsonParser.encodeResourceToString(theMedication));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), medicationDoc, options,"medication");
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        Medication updatedMedication = jsonParser.parseResource(Medication.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(medicationFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedMedication.setMeta(meta);
        return updatedMedication;
    }
    //    OK-ish - optimizar
    public MethodOutcome deleteMedication(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"medication")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el recurso con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public List<Medication> getMedications() {
        List<String> medicationsJson = mongoTemplate.findAll(String.class,"medication");
        if (medicationsJson.isEmpty()) {
            throw new ResourceNotFoundException("No resource found");
        }
        return medicationsJson.stream()
                .map(String -> jsonParser.parseResource(Medication.class, String))
                .collect(Collectors.toList());
    }

    public String medicationFound(Medication theMedication){
        String identifierSystem = theMedication.getIdentifierFirstRep().getSystem();
        String identifierValue = theMedication.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"medication");
    }
}
