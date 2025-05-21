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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ResourceRepository <T extends DomainResource> {
    private final MongoTemplate mongoTemplate;
    private final JsonParser jsonParser;

    public ResourceRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

    public MethodOutcome createFhirResource(T theResource, RequestDetails theRequestDetails, String theId,
                                            String resourceCollection, String identifierSystem, String identifierValue) {
        if (theResource.getResourceType() == ResourceType.Patient){
            if (resourceExists(identifierSystem, identifierValue, resourceCollection) != null) {
                OperationOutcome operationOutcome = new OperationOutcome();
                operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                        .setDiagnostics("Duplicate");
                MethodOutcome methodOutcome = new MethodOutcome(theResource.getIdElement(), operationOutcome, false);
                methodOutcome.setResponseStatusCode(422);
                return methodOutcome;
            }
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theResource)),
                resourceCollection);
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theResource);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), theResource.getResourceType().toString(),
                theId, "1"));
        return methodOutcome;
    }

    public <R extends DomainResource> R readFhirResource(IdType theId, String resourceCollection, Class<R> resourceClass) {
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String json = mongoTemplate.findOne(new Query(criteria), String.class,resourceCollection);
        if (json == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(resourceClass, json);
    }
    //    OK
    public <R extends DomainResource> R updateFhirResource(IdType theId, R theResource, String resourceCollection,
                                                         Class<R> resourceClass){
        R resourceFound = readFhirResource(theId, resourceCollection, resourceClass);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document resourceDoc = Document.parse(jsonParser.encodeResourceToString(theResource));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), resourceDoc, options,resourceCollection);
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        R updatedResource = jsonParser.parseResource(resourceClass, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(resourceFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        calendar.setTime(new Date());
        meta.setLastUpdated(calendar.getTime());
        updatedResource.setMeta(meta);
        return updatedResource;
    }

    public MethodOutcome deleteFhirResource(IdType theId, String resourceCollection){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),resourceCollection)).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el recurso con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public <R extends DomainResource> List<R> getAllResourcesByType(String resourceCollection, Class<R> resourceClass) {
        List<String> jsonResource = mongoTemplate.findAll(String.class, resourceCollection);
        if (jsonResource.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return jsonResource.stream()
                .map(String -> jsonParser.parseResource(resourceClass, String))
                .collect(Collectors.toList());
    }

    public String resourceExists(String identifierSystem, String identifierValue, String resourceCollection){
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class, resourceCollection);
    }

    public <R extends DomainResource> List<R> getAllResourcesByRef(ReferenceParam patientRef, String resourceCollection, Class<R> resourceClass) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("patient.reference").is("Patient" + "/" + patientRef.getIdPart()),
                Criteria.where("subject.reference").is("Patient" + "/" + patientRef.getIdPart()));
        List<String> resourceJson = mongoTemplate.find(new Query(criteria), String.class,resourceCollection);
        if (resourceJson.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return resourceJson.stream()
                .map(String -> jsonParser.parseResource(resourceClass, String))
                .collect(Collectors.toList());
    }

    public void storeSecretKey(String id, SecretKey secretKey){
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        Document key = new Document("id :", id).append("key: ", encodedKey);
        mongoTemplate.insert(key, "dek");
    }
}