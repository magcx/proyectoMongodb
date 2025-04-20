package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProcedureRepository {
    MongoTemplate mongoTemplate;
    JsonParser jsonParser;

    //TODO( - manage errors
    // - Encriptar datos sensibles)
    public ProcedureRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

    public MethodOutcome createProcedure(Procedure theProcedure){
        theProcedure.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
        if (procedureFound(theProcedure) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE);
            return new MethodOutcome(theProcedure.getIdElement(), operationOutcome, false);
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theProcedure)),
                "procedure");
        return new MethodOutcome().setCreated(true).setResource(theProcedure).setId(theProcedure.getIdElement());
    }

    public Procedure readProcedure(IdType theId) {
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonProcedure = mongoTemplate.findOne(new Query(criteria), String.class,"procedure");
        if (jsonProcedure == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(Procedure.class, jsonProcedure);
    }

    public Procedure updateProcedure(IdType theId, Procedure theProcedure){
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document procedureDoc = Document.parse(jsonParser.encodeResourceToString(theProcedure));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), procedureDoc, options,
                "procedure");
        if (updatedDoc == null) {
            throw new UnprocessableEntityException("Procedure not found");
        }
        return jsonParser.parseResource(Procedure.class, updatedDoc.toJson());
    }

    public MethodOutcome deleteProcedure(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"procedure")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el procedure con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public String procedureFound(Procedure theProcedure){
        String identifierSystem = theProcedure.getIdentifierFirstRep().getSystem();
        String identifierValue = theProcedure.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"procedure");
    }
}