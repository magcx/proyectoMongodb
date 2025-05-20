package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.UUID;

@Repository
public class ObservationRepository {
    MongoTemplate mongoTemplate;
    JsonParser jsonParser;

    //TODO(Encriptar datos sensibles)
    public ObservationRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

    public MethodOutcome createObservation(Observation theObservation, RequestDetails theRequestDetails, String theId){
        theObservation.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
//        if (observationFound(theObservation) != null) {
//            OperationOutcome operationOutcome = new OperationOutcome();
//            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
//                    .setDiagnostics("Duplicate observation");
//            MethodOutcome methodOutcome = new MethodOutcome(theObservation.getIdElement(), operationOutcome, false);
//            methodOutcome.setResponseStatusCode(422);
//            return methodOutcome;
//        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theObservation)),
                "observation");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theObservation);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "Observation",
                theId, "1"));
        return methodOutcome;
    }

    public Observation readObservation(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonObservation = mongoTemplate.findOne(new Query(criteria), String.class,"observation");
        if (jsonObservation == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(Observation.class, jsonObservation);
    }

    public Observation updateObservation(IdType theId, Observation theObservation){
        Observation observationFound = readObservation(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document observationDoc = Document.parse(jsonParser.encodeResourceToString(theObservation));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), observationDoc, options,
                "observation");
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        Observation updatedObservation = jsonParser.parseResource(Observation.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(observationFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedObservation.setMeta(meta);
        return updatedObservation;
    }

    public MethodOutcome deleteObservation(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"observation")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar la observacion con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        operationOutcome.setId(theId.getIdPart());
        return new MethodOutcome(operationOutcome);
    }

//    public String observationFound(Observation theObservation){
//        String identifierSystem = theObservation.getIdentifierFirstRep().getSystem();
//        String identifierValue = theObservation.getIdentifierFirstRep().getValue();
//        Criteria criteria = Criteria.where("identifier").elemMatch(
//                Criteria.where("system").is(identifierSystem)
//                        .and("value").is(identifierValue));
//        return mongoTemplate.findOne(new Query(criteria), String.class,"observation");
//    }
}
