package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.MedicationStatementRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MedicationStatementService {
    private final MedicationStatementRepository medicationStatementRepository;

    public MedicationStatementService(MedicationStatementRepository medicationStatementRepository) {
        this.medicationStatementRepository = medicationStatementRepository;
    }

    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createMedicationStatement(MedicationStatement theMedicationStatement, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(theMedicationStatement);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theMedicationStatement.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theMedicationStatement.setMeta(meta);
        return medicationStatementRepository.createMedicationStatement(theMedicationStatement, theRequestDetails, theId);
    }

    public MedicationStatement readMedicationStatement(IdType theId) {
        return medicationStatementRepository.readMedicationStatement(theId);
    }

    public MethodOutcome updateMedicationStatement(IdType theId, MedicationStatement theMedicationStatement) {
        OperationOutcome operationOutcome = new OperationOutcome();
        MedicationStatement medicationStatementUpdated = medicationStatementRepository.updateMedicationStatement(theId, theMedicationStatement);
        if (medicationStatementUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(medicationStatementUpdated).setId(medicationStatementUpdated.getIdElement());
    }

    public MethodOutcome deleteMedicationStatement(IdType theId) {
        return medicationStatementRepository.deleteMedicationStatement(theId);
    }
//    TODO(El return)

    public List<MedicationStatement> getMedicationStatements(ReferenceParam patientRef) {
        return medicationStatementRepository.getMedicationStatements(patientRef);
    }

    public OperationOutcome hasIdentifier(MedicationStatement theMedicationStatement) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theMedicationStatement.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}