package com.example.demo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import org.bson.Document;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoOperations;

public class PatientResourceProvider implements IResourceProvider {
    private MongoOperations mongoOperations;
    private Validator validator;
    private FhirContext fhirContext;

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }
    //CRUD

    @Create
    public MethodOutcome addPatient(@ResourceParam Patient thePatient){
        validatePatient(thePatient);
        MethodOutcome methodOutcome = new MethodOutcome();
        OperationOutcome operationOutcome = new OperationOutcome();
        try {
            String patientJson = fhirContext.newJsonParser().encodeResourceToString(thePatient);
            Document patientDoc = Document.parse(patientJson);
            mongoOperations.insert(patientDoc, "patient");
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                    .setDiagnostics("Paciente creado correctamente");

            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(true);
            methodOutcome.setResource(thePatient);
            methodOutcome.setId(thePatient.getIdElement());

        } catch (Exception e) {
            System.err.println("Error al insertar paciente: " + e.getMessage());
            e.printStackTrace();

            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error agregando paciente a la base de datos: " + e.getMessage());

            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(false);
        }

        return methodOutcome;
    }
//
//    @Read
//    public Patient readPatient(@IdParam IdType theId){
//        return null;
//    }
//
//    @Update
//    public OperationOutcome updatePatient(@ResourceParam Patient thePatient){
//        return null;
//    }
//
//    @Delete
//    public MethodOutcome deletePatient(@IdParam IdType patientId){
//        MethodOutcome outcome = new MethodOutcome();
//        outcome.setCreated(false); // Este es un borrado, no creación.
//        outcome.setId(patientId);  // Establece el ID del paciente eliminado
//        return outcome;
//    }
//
//    @Patch
//    public OperationOutcome updatePatient(){
//        return null;
//    }
//
//    @Search
//    public OperationOutcome searchPatient(){
//        return null;
//    }

    @Validate
    public OperationOutcome validatePatient(@ResourceParam Patient thePatient) {
        try {
            return validator.validatePatientResource(thePatient);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setMongoOperations(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setFhirContext(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }
}