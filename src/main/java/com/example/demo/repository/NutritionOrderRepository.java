package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//https://www.hl7.org/fhir/http.html#create
//NO SE PUEDEN guardar objetos HAPI FHIR porque Mongo los parsea y se quedan inútiles
@Repository
public class NutritionOrderRepository {
    private MongoTemplate mongoTemplate;
    private JsonParser jsonParser;

//TODO(Encriptar datos sensibles)
    public NutritionOrderRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }
//    OK-ish - optimizar
    public MethodOutcome createNutritionOrder(NutritionOrder theNutritionOrder, RequestDetails theRequestDetails, String theId) {
        if (nutritionOrderFound(theNutritionOrder) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate");
            MethodOutcome methodOutcome = new MethodOutcome(theNutritionOrder.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theNutritionOrder)),
                "nutritionOrder");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theNutritionOrder);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "NutritionOrder",
                theId, "1"));
        return methodOutcome;
    }

    public NutritionOrder readNutritionOrder(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonNutritionOrder = mongoTemplate.findOne(new Query(criteria), String.class,"nutritionOrder");
        if (jsonNutritionOrder == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(NutritionOrder.class, jsonNutritionOrder);
    }
    //    OK-ish - optimizar
    public NutritionOrder updateNutritionOrder(IdType theId, NutritionOrder theNutritionOrder){
        NutritionOrder nutritionOrderFound = readNutritionOrder(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document nutritionOrderDoc = Document.parse(jsonParser.encodeResourceToString(theNutritionOrder));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), nutritionOrderDoc, options,"nutritionOrder");
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        NutritionOrder updatedNutritionOrder = jsonParser.parseResource(NutritionOrder.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(nutritionOrderFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedNutritionOrder.setMeta(meta);
        return updatedNutritionOrder;
    }
    //    OK-ish - optimizar
    public MethodOutcome deleteNutritionOrder(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"nutritionOrder")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el recurso con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public List<NutritionOrder> getNutritionOrders() {
        List<String> nutritionOrdersJson = mongoTemplate.findAll(String.class,"nutritionOrder");
        if (nutritionOrdersJson.isEmpty()) {
            throw new ResourceNotFoundException("No resources found");
        }
        return nutritionOrdersJson.stream()
                .map(String -> jsonParser.parseResource(NutritionOrder.class, String))
                .collect(Collectors.toList());
    }

    public String nutritionOrderFound(NutritionOrder theNutritionOrder){
        String identifierSystem = theNutritionOrder.getIdentifierFirstRep().getSystem();
        String identifierValue = theNutritionOrder.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"nutritionOrder");
    }
}