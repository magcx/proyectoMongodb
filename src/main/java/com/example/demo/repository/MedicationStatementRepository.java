package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationStatement;
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
public class MedicationStatementRepository {
    private MongoTemplate mongoTemplate;
    private JsonParser jsonParser;

    //TODO(Encriptar datos sensibles)
    public MedicationStatementRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }
    //    OK-ish - optimizar
    public MethodOutcome createMedicationStatement(MedicationStatement theMedicationStatement, RequestDetails theRequestDetails, String theId) {
        if (medicationStatementFound(theMedicationStatement) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate");
            MethodOutcome methodOutcome = new MethodOutcome(theMedicationStatement.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(theMedicationStatement)),
                "medicationStatement");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(theMedicationStatement);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "MedicationStatement",
                theId, "1"));
        return methodOutcome;
    }

    public MedicationStatement readMedicationStatement(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonMedicationStatement = mongoTemplate.findOne(new Query(criteria), String.class,"medicationStatement");
        if (jsonMedicationStatement == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(MedicationStatement.class, jsonMedicationStatement);
    }
    //    OK-ish - optimizar
    public MedicationStatement updateMedicationStatement(IdType theId, MedicationStatement theMedicationStatement){
        MedicationStatement medicationStatementFound = readMedicationStatement(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document medicationStatementDoc = Document.parse(jsonParser.encodeResourceToString(theMedicationStatement));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), medicationStatementDoc, options,"medicationStatement");
        if (updatedDoc == null) {
            throw new ResourceNotFoundException(theId);
        }
        MedicationStatement updatedMedicationStatement = jsonParser.parseResource(MedicationStatement.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(medicationStatementFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedMedicationStatement.setMeta(meta);
        return updatedMedicationStatement;
    }
    //    OK-ish - optimizar
    public MethodOutcome deleteMedicationStatement(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"medicationStatement")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el recurso con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public List<MedicationStatement> getMedicationStatements() {
        List<String> medicationStatementsJson = mongoTemplate.findAll(String.class,"medicationStatement");
        if (medicationStatementsJson.isEmpty()) {
            throw new ResourceNotFoundException("No resourcess found");
        }
        return medicationStatementsJson.stream()
                .map(String -> jsonParser.parseResource(MedicationStatement.class, String))
                .collect(Collectors.toList());
    }

    public String medicationStatementFound(MedicationStatement theMedicationStatement){
        String identifierSystem = theMedicationStatement.getIdentifierFirstRep().getSystem();
        String identifierValue = theMedicationStatement.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"medicationStatement");
    }
}