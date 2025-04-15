package repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static org.hl7.fhir.r4.model.IdType.newRandomUuid;

@Repository
public class PatientRepository {
MongoTemplate mongoTemplate;
JsonParser jsonParser;

//TODO(Encriptar datos sensibles)
    public PatientRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
        this.mongoTemplate = mongoTemplate;
        this.jsonParser = jsonParser;
    }

//TODO (Devuelve 201 con duplicados)
    public Patient createPatient(Patient thePatient){
        if (patientExist(thePatient) != null) {
            return null;
        }
        if(!thePatient.hasId()){
             thePatient.setId(UUID.randomUUID().toString());
        }
        Document patientDoc = Document.parse(jsonParser.encodeResourceToString(thePatient));
        return jsonParser.parseResource(Patient.class,
                ((mongoTemplate.save(patientDoc, "patient")).toJson()));
    }

//    TODO(El return)
    public Patient readPatient(IdType theId) {
        System.out.println(theId.getIdPart());
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        String jsonPatient = mongoTemplate.findOne(new Query(criteria), String.class,"patient");
        if (jsonPatient != null) {
            return jsonParser.parseResource(Patient.class, jsonPatient);
        }
        return null;
    }

    public Patient updatePatient(Patient thePatient){
        Criteria criteria = Criteria.where("id").is(thePatient.getIdPart());
        Document patientDoc = Document.parse(jsonParser.encodeResourceToString(thePatient));
        if (mongoTemplate.findAndReplace(new Query(criteria),
                patientDoc,
                "patient") != null){
            return jsonParser.parseResource(Patient.class, (mongoTemplate.findAndReplace(new Query(criteria),
                    patientDoc,
                    "patient")).toJson());
        }
        return null;
    }

    public OperationOutcome deletePatient (IdType theId){
        OperationOutcome operationOutcome = new OperationOutcome();
        Criteria criteria = Criteria.where("id").is(theId.getIdPart());
        long isDeleted = (mongoTemplate.remove(new Query(criteria),"patient")).getDeletedCount();
        if (isDeleted == 0){
            operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("No se pudo eliminar el patient con id: " + theId.getIdPart());
            return operationOutcome;
        }
        operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                .setCode(OperationOutcome.IssueType.DELETED)
                .setDiagnostics("Paciente eliminado correctamente");
        return operationOutcome;
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
