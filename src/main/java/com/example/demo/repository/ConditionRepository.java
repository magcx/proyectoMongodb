package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ConditionRepository {
    private MongoTemplate mongoTemplate;
    private JsonParser jsonParser;

    //TODO(Encriptar datos sensibles)
    public ConditionRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }
    //    OK-ish - optimizar
    public MethodOutcome createCondition(Condition theCondition, RequestDetails theRequestDetails, String theId) {
        if (conditionFound(theCondition) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate");
            MethodOutcome methodOutcome = new MethodOutcome(theCondition.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theCondition)),
                "condition");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theCondition);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "Condition",
                theId, "1"));
        return methodOutcome;
    }

    public Condition readCondition(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonCondition = mongoTemplate.findOne(new Query(criteria), String.class,"condition");
        if (jsonCondition == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(Condition.class, jsonCondition);
    }
    //    OK-ish - optimizar
    public Condition updateCondition(IdType theId, Condition theCondition){
        Condition conditionFound = readCondition(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document conditionDoc = Document.parse(jsonParser.encodeResourceToString(theCondition));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), conditionDoc, options,"condition");
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        Condition updatedCondition = jsonParser.parseResource(Condition.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(conditionFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedCondition.setMeta(meta);
        return updatedCondition;
    }
    //    OK-ish - optimizar
    public MethodOutcome deleteCondition(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"condition")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el recurso con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public List<Condition> getConditions(ReferenceParam patientRef) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("patient.reference").is("Patient" + "/" + patientRef.getIdPart()),
                Criteria.where("subject.reference").is("Patient" + "/" + patientRef.getIdPart()));
        List<String> conditionJson = mongoTemplate.find(new Query(criteria), String.class,"condition");
        if (conditionJson.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return conditionJson.stream()
                .map(String -> jsonParser.parseResource(Condition.class, String))
                .collect(Collectors.toList());
    }

    public String conditionFound(Condition theCondition){
        String identifierSystem = theCondition.getIdentifierFirstRep().getSystem();
        String identifierValue = theCondition.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"condition");
    }
}