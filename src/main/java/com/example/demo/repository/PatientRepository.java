package com.example.demo.repository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import com.google.common.collect.Multimap;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

//https://www.hl7.org/fhir/http.html#create
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
        if (patientFound(thePatient) != null) {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                    .setDiagnostics("Duplicate patient");

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

//    @Override
//    public <T extends IBaseResource, I extends IIdType> T read(Class<T> aClass, I i, Map<String, String> map) {
//        return null;
//    }
//
//    @Override
//    public MethodOutcome create(Patient thePatient, Map<String, String> map) {
//        thePatient.setMeta
//        return null;
//    }
//
//    @Override
//    public <T extends IBaseResource> MethodOutcome update(T t, Map<String, String> map) {
//        return null;
//    }
//
//    @Override
//    public <T extends IBaseResource, I extends IIdType> MethodOutcome delete(Class<T> aClass, I i, Map<String, String> map) {
//        return null;
//    }
//
//    @Override
//    public <B extends IBaseBundle, T extends IBaseResource> B search(Class<B> aClass, Class<T> aClass1, Multimap<String, List<IQueryParameterType>> multimap, Map<String, String> map) {
//        return null;
//    }
//
//    @Override
//    public <R extends IBaseResource, P extends IBaseParameters, T extends IBaseResource> R invoke(Class<T> aClass, String s, P p, Class<R> aClass1, Map<String, String> map) {
//        return null;
//    }
//
//    @Override
//    public <R extends IBaseResource, P extends IBaseParameters, I extends IIdType> R invoke(I i, String s, P p, Class<R> aClass, Map<String, String> map) {
//        return null;
//    }
//
//    @Override
//    public FhirContext fhirContext() {
//        return null;
//    }
}