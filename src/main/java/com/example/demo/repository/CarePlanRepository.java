package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.UUID;

//NO SE PUEDEN guardar objetos HAPI FHIR porque Mongo los parsea y se quedan inútiles
@Repository
public class CarePlanRepository {
    MongoTemplate mongoTemplate;
    JsonParser jsonParser;

    //TODO(Encriptar datos sensibles)
    public CarePlanRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

    public MethodOutcome createCarePlan(CarePlan theCarePlan, RequestDetails theRequestDetails, String theId){
        theCarePlan.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
        if (carePlanFound(theCarePlan) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate care plan");
            MethodOutcome methodOutcome = new MethodOutcome(theCarePlan.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theCarePlan)),
                "carePlan");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theCarePlan);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), " CarePlan",
                theId, "1"));
        return methodOutcome;
    }

    public CarePlan readCarePlan(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonCarePlan = mongoTemplate.findOne(new Query(criteria), String.class,"carePlan");
        if (jsonCarePlan == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(CarePlan.class, jsonCarePlan);
    }

    public CarePlan updateCarePlan(IdType theId, CarePlan theCarePlan){
        CarePlan carePlanFound = readCarePlan(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document carePlanDoc = Document.parse(jsonParser.encodeResourceToString(theCarePlan));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedCarePlanDoc = mongoTemplate.findAndReplace(new Query(criteria), carePlanDoc, options,
                "carePlan");
        if (updatedCarePlanDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        CarePlan updatedCarePlan = jsonParser.parseResource(CarePlan.class, updatedCarePlanDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(carePlanFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedCarePlan.setMeta(meta);
        return updatedCarePlan;
    }

    public MethodOutcome deleteCarePlan (IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"carePlan")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el plan de cuidados con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public String carePlanFound(CarePlan theCarePlan){
        String identifierSystem = theCarePlan.getIdentifierFirstRep().getSystem();
        String identifierValue = theCarePlan.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class, "carePlan");
    }
}