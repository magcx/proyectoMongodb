package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.demo.util.ResourceUtil;
import org.bson.Document;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
            if (resourceExists (identifierSystem, identifierValue, resourceCollection) != null) {
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
        String json = mongoTemplate.findOne(new Query(criteria), String.class, resourceCollection);
        if (json == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(resourceClass, json);
    }
    //    OK
    public <R extends DomainResource> R updateFhirResource(IdType theId, R theResource, String resourceCollection,
                                                         Class<R> resourceClass){
        String versionId = Integer.toString(Integer.parseInt(theResource.getMeta().getVersionId()) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        calendar.setTime(new Date());
        meta.setLastUpdated(calendar.getTime());
        theResource.setMeta(meta);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document resourceDoc = Document.parse(jsonParser.encodeResourceToString(theResource));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), resourceDoc, options,resourceCollection);
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(resourceClass, updatedDoc.toJson());
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

    public String resourceExists(String identifierSystem, String identifierValue, String resourceCollection){
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class, resourceCollection);
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

    public <R extends DomainResource> List<R> getAllResourcesByRef(ReferenceParam patientRef,
                                                                   String resourceCollection, Class<R> resourceClass) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("patient.reference").is("Patient" + "/" + patientRef.getIdPart()),
                Criteria.where("subject.reference").is("Patient" + "/" + patientRef.getIdPart()));

        List<String> resourceJson = findQuery(resourceCollection, criteria);
        if (resourceJson.isEmpty()) throw new ResourceNotFoundException("No resources found");
        return resourceJson.stream()
                .map(String -> jsonParser.parseResource(resourceClass, String))
                .collect(Collectors.toList());
    }

    public <R extends DomainResource> List<R> getAllResourcesByIdentifier(TokenParam identifier, String resourceCollection, Class<R> resourceClass) {
        Criteria criteria = Criteria.where("identifier.value").regex("^" + identifier.getValue());
        List<String> resourceJson = findQuery(resourceCollection, criteria);
        if (resourceJson.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return resourceJson.stream()
                .map(String -> jsonParser.parseResource(resourceClass, String))
                .collect(Collectors.toList());
    }

    //TODO(Unificar métodos find pasando por parámetro la query)
    public <R extends DomainResource> List<R> getResourcesByScheduledDay(ReferenceParam patientRef, String resourceCollection,
                                                     Class<R> resourceClass, DateParam date) {
        String dateOnly  = ResourceUtil.formatDate(date.getValue()).split("T")[0];
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("for.reference").is("Patient" + "/" + patientRef.getIdPart()),
                Criteria.where("restriction.period.start").regex("^" + dateOnly));

        List<String> resourceJson = findQuery(resourceCollection, criteria);
        if (resourceJson.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return resourceJson.stream()
                .map(String -> jsonParser.parseResource(resourceClass, String))
                .collect(Collectors.toList());
    }

    public <R extends DomainResource> List<R> getAllResourcesByCategory (ReferenceParam patientRef, TokenParam categoryRef,
                                                                        String resourceCollection, Class<R> resourceClass) {
        Criteria criteria = new Criteria().orOperator(
                        Criteria.where("patient.reference").is("Patient/" + patientRef.getIdPart()),
                        Criteria.where("subject.reference").is("Patient/" + patientRef.getIdPart())
                )
                .andOperator(
                        Criteria.where("category.coding").elemMatch(
                                Criteria.where("code").is(categoryRef.getValue())
                        )
                );

        List<String> resourceJson = findQuery(resourceCollection, criteria);
        if (resourceJson.isEmpty()) throw new ResourceNotFoundException("No resources found");
        return resourceJson.stream()
                .map(String -> jsonParser.parseResource(resourceClass, String))
                .collect(Collectors.toList());
    }


    private List<String> findQuery(String resourceCollection, Criteria criteria) {
        return mongoTemplate.find(new Query(criteria), String.class, resourceCollection);
    }

    public void storeSecretKey(String id, SecretKey secretKey){
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        Document keyDoc = new Document("id", id).append("key", encodedKey);
        mongoTemplate.insert(keyDoc, "dek");
    }

    public SecretKey getSecretKey(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Document keyDoc = mongoTemplate.findOne(query, Document.class, "dek");
        if (keyDoc == null || !keyDoc.containsKey("key")) {
            throw new ResourceNotFoundException(id);
        }
        String encodedKey = keyDoc.getString("key");
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public boolean deleteSecretKey(String id) {
        Criteria criteria = Criteria.where("id").is(id);
        return (mongoTemplate.remove(new Query(criteria),"dek")).getDeletedCount() != 0;
    }
}