package repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PatientRepository {
MongoTemplate mongoTemplate;
JsonParser jsonParser;

    public PatientRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

     public MethodOutcome createPatient(Patient thePatient){
        MethodOutcome methodOutcome = new MethodOutcome();
        OperationOutcome operationOutcome = new OperationOutcome();
         try {
             if (patientExist(thePatient) != null) {
                 methodOutcome.setCreated(false);
                 return methodOutcome;
             } else {
                 Document patientJson = Document.parse(jsonParser.encodeResourceToString(thePatient));
                 mongoTemplate.save(patientJson, "patient");
                 methodOutcome.setId(new IdType("Patient",
                         patientJson.getObjectId("_id").toHexString()));
             }
         } catch (Exception e) {
             operationOutcome.addIssue()
                     .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                     .setDiagnostics(e.getMessage());
             methodOutcome.setOperationOutcome(operationOutcome);
             return methodOutcome;
         }
         return methodOutcome;
    }

    public Patient readPatient(IdType theId) {
        System.out.println(theId.getIdPart());
        String jsonPatient = mongoTemplate.findById(theId.getIdPart(), String.class,"patient");
        if (jsonPatient != null) {
            return jsonParser.parseResource(Patient.class, jsonPatient);
        }
        return null;
    }

    public OperationOutcome updatePatient(@ResourceParam Patient thePatient){
        return null;
    }

    public String patientExist(Patient thePatient){
        String identifierSystem = thePatient.getIdentifierFirstRep().getSystem();
        String identifierValue = thePatient.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"patient");
    }
}
