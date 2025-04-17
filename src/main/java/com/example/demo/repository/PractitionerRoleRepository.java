package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PractitionerRoleRepository {
    //NO SE PUEDEN guardar objetos HAPI FHIR porque Mongo los parsea y se quedan inútiles
        MongoTemplate mongoTemplate;
        JsonParser jsonParser;

        //TODO(Encriptar datos sensibles)
        public PractitionerRoleRepository(MongoTemplate mongoTemplate, JsonParser jsonParser) {
            this.mongoTemplate = mongoTemplate;
            this.jsonParser = jsonParser;
        }

        public MethodOutcome createPractitionerRole(PractitionerRole thePractitionerRole){
            thePractitionerRole.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
            if (practitionerRoleFound(thePractitionerRole) != null) {
                OperationOutcome operationOutcome = new OperationOutcome();
                operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE);
                return new MethodOutcome(thePractitionerRole.getIdElement(), operationOutcome, false);
            }
            mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(thePractitionerRole)),
                    "practitionerRole");
            return new MethodOutcome().setCreated(true).setResource(thePractitionerRole)
                    .setId(thePractitionerRole.getIdElement());
        }

        public PractitionerRole readPractitionerRole(IdType theId) {
            System.out.println(theId.getIdPart());
            Criteria criteria = Criteria.where("id").is(theId.getIdPart());
            String jsonPractitionerRole = mongoTemplate.findOne(new Query(criteria), String.class,
                    "practitionerRole");
            if (jsonPractitionerRole != null) {
                return jsonParser.parseResource(PractitionerRole.class, jsonPractitionerRole);
            }
            return null;
        }

        public PractitionerRole updatePractitionerRole(IdType theId, PractitionerRole thePractitionerRole){
            Criteria criteria = Criteria.where("id").is(theId.getIdPart());
            Document practitionerRoleDoc = Document.parse(jsonParser.encodeResourceToString(thePractitionerRole));
            FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
            Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), practitionerRoleDoc, options,
                    "practitionerRole");
            if (updatedDoc == null) {
                System.out.println("No se puede actualizar el practitioner Role");
                return null;
            }
            return jsonParser.parseResource(PractitionerRole.class, updatedDoc.toJson());
        }

        public MethodOutcome deletePractitionerRole(IdType theId){
            OperationOutcome operationOutcome = new OperationOutcome();
            Criteria criteria = Criteria.where("id").is(theId.getIdPart());
            long isDeleted = (mongoTemplate.remove(new Query(criteria),"practitionerRole")).getDeletedCount();
            if (isDeleted == 0){
                operationOutcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setDiagnostics("No se pudo eliminar el practitionerRole con id: " + theId.getIdPart());
                return new MethodOutcome(operationOutcome);
            }
            return new MethodOutcome().setId(theId);
        }

    public String practitionerRoleFound(PractitionerRole thePractitionerRole){
        String identifierSystem = thePractitionerRole.getIdentifierFirstRep().getSystem();
        String identifierValue = thePractitionerRole.getIdentifierFirstRep().getValue();
        Criteria criteria = Criteria.where("identifier").elemMatch(
                Criteria.where("system").is(identifierSystem)
                        .and("value").is(identifierValue));
        return mongoTemplate.findOne(new Query(criteria), String.class,"practitionerRole");
    }
}