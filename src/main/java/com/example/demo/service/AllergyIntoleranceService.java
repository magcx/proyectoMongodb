package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.AllergyIntoleranceRepository;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class AllergyIntoleranceService {
    private final AllergyIntoleranceRepository allergyIntoleranceRepository;

    public AllergyIntoleranceService(AllergyIntoleranceRepository AllergyIntoleranceRepository) {
        this.allergyIntoleranceRepository = allergyIntoleranceRepository;
    }

    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createAllergyIntolerance(AllergyIntolerance theAllergyIntolerance, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(theAllergyIntolerance);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theAllergyIntolerance.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theAllergyIntolerance.setMeta(meta);
        return allergyIntoleranceRepository.createAllergyIntolerance(theAllergyIntolerance, theRequestDetails, theId);
    }

    public AllergyIntolerance readAllergyIntolerance(IdType theId) {
        return allergyIntoleranceRepository.readAllergyIntolerance(theId);
    }

    public MethodOutcome updateAllergyIntolerance(IdType theId, AllergyIntolerance theAllergyIntolerance) {
        OperationOutcome operationOutcome = new OperationOutcome();
        AllergyIntolerance allergyIntoleranceUpdated = allergyIntoleranceRepository.updateAllergyIntolerance(theId, theAllergyIntolerance);
        if (allergyIntoleranceUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando paciente");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(allergyIntoleranceUpdated).setId(allergyIntoleranceUpdated.getIdElement());
    }

    public MethodOutcome deleteAllergyIntolerance(IdType theId) {
        return allergyIntoleranceRepository.deleteAllergyIntolerance(theId);
    }
//    TODO(El return)

    public List<AllergyIntolerance> getAllergiesIntolerances() {
        return allergyIntoleranceRepository.getAllergiesIntolerances();
    }

    public OperationOutcome hasIdentifier(AllergyIntolerance theAllergyIntolerance) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theAllergyIntolerance.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}