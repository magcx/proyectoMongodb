package com.example.demo.repository;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
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

        public MethodOutcome createPractitionerRole(PractitionerRole thePractitionerRole, RequestDetails theRequestDetails, String theId){
            thePractitionerRole.setId(UUID.randomUUID().toString());  //Se lo toma como que no tiene id
            if (practitionerRoleFound(thePractitionerRole) != null) {
                OperationOutcome operationOutcome = new OperationOutcome();
                operationOutcome.addIssue().setCode(OperationOutcome.IssueType.DUPLICATE)
                        .setDiagnostics("Duplicate Practitioner Role");
                MethodOutcome methodOutcome = new MethodOutcome(thePractitionerRole.getIdElement(), operationOutcome, false);
                methodOutcome.setResponseStatusCode(422);
                return methodOutcome;
            }
            mongoTemplate.insert(Document.parse(jsonParser.encodeResourceToString(thePractitionerRole)),
                    "practitionerRole");
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setCreated(true);
            methodOutcome.setResource(thePractitionerRole);
            methodOutcome.setId(new IdType(theRequestDetails.getFhirServerBase(), "PractitionerRole",
                    theId, "1"));
            return methodOutcome;
        }

        public PractitionerRole readPractitionerRole(IdType theId) {
            System.out.println(theId.getIdPart());
            Criteria criteria = Criteria.where("id").is(theId.getIdPart());
            String jsonPractitionerRole = mongoTemplate.findOne(new Query(criteria), String.class,
                    "practitionerRole");
            if (jsonPractitionerRole == null) {
                throw new ResourceNotFoundException(theId);
            }
            return jsonParser.parseResource(PractitionerRole.class, jsonPractitionerRole);
        }

        public PractitionerRole updatePractitionerRole(IdType theId, PractitionerRole thePractitionerRole){
            PractitionerRole practitionerRoleFound = readPractitionerRole(theId);
            Criteria criteria = Criteria.where("id").is(theId.getIdPart());
            Document practitionerRoleDoc = Document.parse(jsonParser.encodeResourceToString(thePractitionerRole));
            FindAndReplaceOptions options = new FindAndReplaceOptions().returnNew();
            Document updatedDoc = mongoTemplate.findAndReplace(new Query(criteria), practitionerRoleDoc, options,
                    "practitionerRole");
            if (updatedDoc == null) {
                throw new ResourceNotFoundException(theId);
            }
            PractitionerRole updatedPractitionedRoled =  jsonParser.parseResource(PractitionerRole.class, updatedDoc.toJson());
            String versionId = String.valueOf((Integer.parseInt(practitionerRoleFound.getMeta().getVersionId())) + 1);
            Meta meta = new Meta();
            meta.setVersionId(versionId);
            meta.setLastUpdated(new Date(System.currentTimeMillis()));
            updatedPractitionedRoled.setMeta(meta);
            return updatedPractitionedRoled;
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