package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.example.demo.repository.ProcedureRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Service;

@Service
public class ProcedureService {
    private final ProcedureRepository procedureRepository;

    public ProcedureService(ProcedureRepository procedureRepository) {
        this.procedureRepository = procedureRepository;
    }

    public MethodOutcome createProcedure(Procedure theProcedure) {
        OperationOutcome operationOutcome = hasIdentifier(theProcedure);
        if (operationOutcome!= null) {
            return new MethodOutcome(theProcedure.getIdElement(), operationOutcome, false);
        }
        return procedureRepository.createProcedure(theProcedure);
    }

    public Procedure readProcedure(IdType theId) {
        return procedureRepository.readProcedure(theId);
    }

    public MethodOutcome updateProcedure(IdType theId, Procedure theProcedure) {
        OperationOutcome operationOutcome = new OperationOutcome();
        Procedure procedureUpdated = procedureRepository.updateProcedure(theId, theProcedure);
        if (procedureUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando paciente");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(procedureUpdated).setId(procedureUpdated.getIdElement());
    }

    public MethodOutcome deleteProcedure(IdType theId) {
        return procedureRepository.deleteProcedure(theId);
    }
//    TODO(El return)

    public OperationOutcome hasIdentifier(Procedure theProcedure) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theProcedure.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}
