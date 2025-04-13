package com.example.demo;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import com.mongodb.client.result.UpdateResult;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import repository.PatientRepository;

import java.util.function.Supplier;

@Service
public class PatientResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final PatientRepository patientRepository;

    public PatientResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.patientRepository = new PatientRepository(mongoTemplate, jsonParser);
    }
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

//CRUD
//   TODO(Gestionar dar identificadores aleatorios únicos)
//   TODO (custom MethodOutcomes para simplificar código)
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
        MethodOutcome methodOutcome = new MethodOutcome();
        OperationOutcome operationOutcome = new OperationOutcome();
        if (thePatient.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(false);
            return methodOutcome;
        }
        Patient patientSaved = patientRepository.createPatient(thePatient);
        if (patientSaved != null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                    .setDiagnostics("Paciente creado correctamente");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(true);
            methodOutcome.setResource(patientSaved);
            methodOutcome.setId(new IdType("Patient", thePatient.getIdElement().getIdPart()));
        }
        return methodOutcome;
    }

//   OK
    @Read()
    public Patient readPatient(@IdParam IdType theId){
        return patientRepository.readPatient(theId);
    }

//    OK
    @Update
    public MethodOutcome updatePatient(@IdParam IdType theId,@ResourceParam Patient thePatient){
        OperationOutcome operationOutcome = new OperationOutcome();
        MethodOutcome methodOutcome = new MethodOutcome();
        Patient patientUpdated = patientRepository.updatePatient(thePatient);
        if (patientUpdated == null) {
            operationOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setDiagnostics("Error actualizando paciente");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(false);
        } else {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                    .setDiagnostics("Paciente actualizado correctamente");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(true);
            methodOutcome.setResource(patientUpdated);
            methodOutcome.setId(new IdType("Patient", thePatient.getIdElement().getIdPart()));
        }
        return methodOutcome;
    }
//
//    @Delete
//    public MethodOutcome deletePatient(@IdParam IdType patientId){
//        MethodOutcome outcome = new MethodOutcome();
//
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

//    @Validate
//    public Patient validatePatient(@ResourceParam Patient thePatient) {
//        try {
//            ValidationResult validationResult =  patientValidator.validatePatientResource(thePatient);
//            if (!validationResult.isSuccessful()){
//                return null;
//            } else if (validationResult.isSuccessful()){
//                return thePatient;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public void setValidator(PatientValidator patientValidator) {
//        this.patientValidator = patientValidator;
//    }

//    public void setFhirContext(FhirContext fhirContext) {
//        this.fhirContext = fhirContext;
//    }
}