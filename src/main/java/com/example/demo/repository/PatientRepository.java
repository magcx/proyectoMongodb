package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

//NO SE PUEDEN guardar objetos HAPI FHIR porque Mongo los parsea y se quedan inútiles
@Repository
public class PatientRepository {
    MongoTemplate mongoTemplate;
    JsonParser jsonParser;

//TODO(Encriptar datos sensibles)
    public PatientRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

    public MethodOutcome createPatient(Patient thePatient){
        thePatient.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
        if (patientFound(thePatient) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE);
            return new MethodOutcome(thePatient.getIdElement(), operationOutcome, false);
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(thePatient)),
                "patient");
        return new MethodOutcome().setCreated(true).setResource(thePatient).setId(thePatient.getIdElement());
    }

    public Patient readPatient(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonPatient = mongoTemplate.findOne(new Query(criteria), String.class,"patient");
        if (jsonPatient != null) {
            return jsonParser.parseResource(Patient.class, jsonPatient);
        }
        return null;
    }

    public Patient updatePatient(IdType theId, Patient thePatient){
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document patientDoc = Document.parse(jsonParser.encodeResourceToString(thePatient));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), patientDoc, options,
                "patient");
        if (updatedDoc == null) {
            System.out.println("No se puede actualizar el patient");
            return null;
        }
        return jsonParser.parseResource(Patient.class, updatedDoc.toJson());
    }

    public MethodOutcome deletePatient(IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"patient")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el patient con id: " + theId.getIdPart());
            return new MethodOutcome(operationOutcome);
        }
        return new MethodOutcome().setId(theId);
    }

    public String patientFound(Patient thePatient){
        String identifierSystem = thePatient.getIdentifierFirstRep().getSystem();
        String identifierValue = thePatient.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"patient");
    }
}