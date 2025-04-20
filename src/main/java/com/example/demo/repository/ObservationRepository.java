package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

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

    public MethodOutcome createObservation(Observation theObservation){
        theObservation.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
        if (observationFound(theObservation) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE);
            return new MethodOutcome(theObservation.getIdElement(), operationOutcome, false);
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theObservation)),
                "observation");
        return new MethodOutcome().setCreated(true).setResource(theObservation).setId(theObservation.getIdElement());
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
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document observationDoc = Document.parse(jsonParser.encodeResourceToString(theObservation));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), observationDoc, options,
                "observation");
        if (updatedDoc == null) {
            System.out.println("No se puede actualizar la observation");
            return null;
        }
        return jsonParser.parseResource(Observation.class, updatedDoc.toJson());
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

    public String observationFound(Observation theObservation){
        String identifierSystem = theObservation.getIdentifierFirstRep().getSystem();
        String identifierValue = theObservation.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"observation");
    }
}
