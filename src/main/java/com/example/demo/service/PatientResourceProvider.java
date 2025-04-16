package com.example.demo.service;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.example.demo.repository.PatientRepository;

@Service
public class PatientResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final PatientRepository patientRepository;
    private final FhirValidator fhirValidator;

    public PatientResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate, FhirValidator fhirValidator) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
        this.patientRepository = new PatientRepository(mongoTemplate, jsonParser);
    }
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

//CRUD
//   TODO (custom MethodOutcomes para simplificar código)
//TODO(Validación manual para que no entre en el método)
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
        MethodOutcome methodOutcome = new MethodOutcome();
        OperationOutcome operationOutcome = new OperationOutcome();
        Patient validatedPatient = validatePatient(thePatient);
        if (validatedPatient == null){
            methodOutcome.setCreated(false);
            return methodOutcome;
        }
        if (validatedPatient.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(false);
            return methodOutcome;
        }
        System.out.println("Paciente validado");
        Patient patientSaved = patientRepository.createPatient(validatedPatient);
        if (patientSaved != null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                    .setDiagnostics("Paciente creado correctamente");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(true);
            methodOutcome.setResource(patientSaved);
            methodOutcome.setId(new IdType("Patient", patientSaved.getIdElement().getIdPart()));
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

    @Delete
    public MethodOutcome deletePatient(@IdParam IdType theId){
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setOperationOutcome(patientRepository.deletePatient(theId));
        methodOutcome.setCreated(false);
        methodOutcome.setId(theId);
        return methodOutcome;
    }

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
    public Patient validatePatient(@ResourceParam Patient thePatient) {
        ValidationResult validationResult = fhirValidator.validateWithResult(thePatient);
        if (!validationResult.isSuccessful()){
            System.out.println("Paciente NO validado");
            return null;
        }
        return thePatient;
    }
}