package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AllergyIntoleranceRepository {
    private MongoTemplate mongoTemplate;
    private JsonParser jsonParser;

    //TODO(Encriptar datos sensibles)
    public AllergyIntoleranceRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }
    //    OK-ish - optimizar
    public MethodOutcome createAllergyIntolerance(AllergyIntolerance theAllergyIntolerance, RequestDetails theRequestDetails, String theId) {
        if (allergyIntoleranceFound(theAllergyIntolerance) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate");
            MethodOutcome methodOutcome = new MethodOutcome(theAllergyIntolerance.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theAllergyIntolerance)),
                "allergyIntolerance");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theAllergyIntolerance);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "AllergyIntolerance",
                theId, "1"));
        return methodOutcome;
    }

    public AllergyIntolerance readAllergyIntolerance(IdType theId) {
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonAllergyIntolerance = mongoTemplate.findOne(new Query(criteria), String.class,"allergyIntolerance");
        if (jsonAllergyIntolerance == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(AllergyIntolerance.class, jsonAllergyIntolerance);
    }
    //    OK-ish - optimizar
    public AllergyIntolerance updateAllergyIntolerance(IdType theId, AllergyIntolerance theAllergyIntolerance){
        AllergyIntolerance allergyIntoleranceFound = readAllergyIntolerance(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document allergyIntoleranceDoc = Document.parse(jsonParser.encodeResourceToString(theAllergyIntolerance));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), allergyIntoleranceDoc, options,"allergyIntolerance");
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        AllergyIntolerance updatedAllergyIntolerance = jsonParser.parseResource(AllergyIntolerance.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(allergyIntoleranceFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedAllergyIntolerance.setMeta(meta);
        return updatedAllergyIntolerance;
    }
    //    OK-ish - optimizar
    public MethodOutcome deleteAllergyIntolerance(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"allergyIntolerance")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el recurso con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public List<AllergyIntolerance> getAllergiesIntolerances(ReferenceParam patientRef) {
        Criteria criteria = Criteria
                .where("patient.reference")
                .is("Patient" + "/" + patientRef.getIdPart());
        List<String> allergiesIntolerancesJson = mongoTemplate.find(new Query(criteria), String.class,"allergyIntolerance");
        if (allergiesIntolerancesJson.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return allergiesIntolerancesJson.stream()
                .map(String -> jsonParser.parseResource(AllergyIntolerance.class, String))
                .collect(Collectors.toList());
    }

    public String allergyIntoleranceFound(AllergyIntolerance theAllergyIntolerance){
        String identifierSystem = theAllergyIntolerance.getIdentifierFirstRep().getSystem();
        String identifierValue = theAllergyIntolerance.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier")
                .elemMatch(Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"allergyIntolerance");
    }
}