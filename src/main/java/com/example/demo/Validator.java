package com.example.demo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class Validator {
    private final FhirContext ctx = FhirContext.forR4();
    private final FhirValidator fhirValidator = ctx.newValidator();

    public Validator(){

    ValidationSupportChain validationSupportChain = new ValidationSupportChain(
            new DefaultProfileValidationSupport(ctx), //Validación de StructureDefinition
            new SnapshotGeneratingValidationSupport(ctx), //Validación y generación de perfiles
            new CommonCodeSystemsTerminologyService(ctx), //Validación de CodeSystem
            new InMemoryTerminologyServerValidationSupport(ctx) //Validación de CodeSystem y ValueSet desde la memoria
    );
    PrePopulatedValidationSupport prePopulatedValidationSupport = new PrePopulatedValidationSupport(ctx);
    try {
        prePopulatedValidationSupport.addStructureDefinition(loadProfile());
    } catch (IOException e) {
        e.printStackTrace();
    }
    validationSupportChain.addValidationSupport(prePopulatedValidationSupport);

    //Capa de caché por encima de la validación para evitar repetir validaciones previamente realizadas.
    CachingValidationSupport cachingValidationSupport = new CachingValidationSupport(validationSupportChain);

    //Contenedor de la estructura del validador
    FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator(ctx);
    fhirInstanceValidator.setValidationSupport(cachingValidationSupport);

    //Se crea un validador general con la configuración que se ha definido anteriormente
    fhirValidator.registerValidatorModule(fhirInstanceValidator);
    }

    private static StructureDefinition loadProfile() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/fhirProfiles/MyPatient.json"));
        IParser parser = FhirContext.forR4().newJsonParser();
        return parser.parseResource(StructureDefinition.class, content);
    }

    public OperationOutcome validatePatientResource (Patient thePatient) throws ValidationException {
        ValidationResult validationResult = fhirValidator.validateWithResult(thePatient);
        return (OperationOutcome) validationResult.toOperationOutcome();
    }
}