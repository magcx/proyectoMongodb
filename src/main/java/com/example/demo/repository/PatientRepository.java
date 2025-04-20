package com.example.demo.repository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.Multimap;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;

//https://www.hl7.org/fhir/http.html#create
//NO SE PUEDEN guardar objetos HAPI FHIR porque Mongo los parsea y se quedan inútiles
@Repository
public class PatientRepository {
    private MongoTemplate mongoTemplate;
    private JsonParser jsonParser;

//TODO(Encriptar datos sensibles)
    public PatientRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }
//    OK-ish - optimizar
    public MethodOutcome createPatient(Patient thePatient, RequestDetails theRequestDetails, String theId) {
        if (patientFound(thePatient) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate patient");
            MethodOutcome methodOutcome = new MethodOutcome(thePatient.getIdElement(), operationOutcome, false);
            methodOutcome.setResponseStatusCode(422);
            return methodOutcome;
        }
        mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(thePatient)),
                "patient");
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setCreated(true);
        methodOutcome.setResource(thePatient);
        methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "Patient",
                theId, "1"));
        return methodOutcome;
    }

    public Patient readPatient(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonPatient = mongoTemplate.findOne(new Query(criteria), String.class,"patient");
        if (jsonPatient == null) {
            throw new ResourceNotFoundException(theId);
        }
        return jsonParser.parseResource(Patient.class, jsonPatient);
    }
    //    OK-ish - optimizar
    public Patient updatePatient(IdType theId, Patient thePatient){
        Patient patiendFound = readPatient(theId);
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        Document patientDoc = Document.parse(jsonParser.encodeResourceToString(thePatient));
        FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
        Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), patientDoc, options,"patient");
        if (updatedDoc == null) {
            return null;
        }
        Patient updatedPatient = jsonParser.parseResource(Patient.class, updatedDoc.toJson());
        String versionId = String.valueOf((Integer.parseInt(patiendFound.getMeta().getVersionId())) + 1);
        Meta meta = new Meta();
        meta.setVersionId(versionId);
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        updatedPatient.setMeta(meta);
        return updatedPatient;
    }
    //    OK-ish - optimizar
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