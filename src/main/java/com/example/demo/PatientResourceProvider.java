package com.example.demo;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.*;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import repository.PatientRepository;

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
    //        TODO(Gestionar dar identificadores aleatorios únicos)
//        TODO (custom MethodOutcomes para simplificar código)
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient){
        MethodOutcome methodOutcome = new MethodOutcome();
        OperationOutcome operationOutcome = new OperationOutcome();
        if (thePatient.getIdentifier().isEmpty()){
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            methodOutcome.setOperationOutcome(operationOutcome);
            methodOutcome.setCreated(false);
        } else {
            MethodOutcome patientId = patientRepository.createPatient(thePatient);
            if (patientId.getId() != null) {
                methodOutcome.setCreated(true);
                methodOutcome.setId(patientId.getId());
                System.out.println(methodOutcome.getId().toString());
            } else {
                operationOutcome.addIssue()
                        .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setDiagnostics("Error agregando paciente a la base de datos: ");
                methodOutcome.setOperationOutcome(operationOutcome);
                methodOutcome.setCreated(false);
            }
        }
        return methodOutcome;
    }

    @Read()
    public Patient readPatient(@IdParam IdType theId){
        return patientRepository.readPatient(theId);
    }

    @Update
    public OperationOutcome updatePatient(@ResourceParam Patient thePatient){
        return null;
    }

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