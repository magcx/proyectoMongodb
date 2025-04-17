package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.example.demo.repository.PractitionerRoleRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.stereotype.Service;

@Service
public class PractitionerRoleService {
    private final PractitionerRoleRepository practitionerRoleRepository;

    public PractitionerRoleService(PractitionerRoleRepository practitionerRoleRepository) {
        this.practitionerRoleRepository = practitionerRoleRepository;
    }

    public MethodOutcome createPractitionerRole(PractitionerRole thePractitionerRole) {
        OperationOutcome operationOutcome = hasIdentifier(thePractitionerRole);
        if (operationOutcome!= null) {
            return new MethodOutcome(thePractitionerRole.getIdElement(), operationOutcome, false);
        }
        return practitionerRoleRepository.createPractitionerRole(thePractitionerRole);
    }

    public PractitionerRole readPractitionerRole(IdType theId) {
        return practitionerRoleRepository.readPractitionerRole(theId);
    }

    public MethodOutcome updatePractitionerRole(IdType theId, PractitionerRole thePractitionerRole) {
        OperationOutcome operationOutcome = new OperationOutcome();
        PractitionerRole practitionerRoleUpdated = practitionerRoleRepository.updatePractitionerRole(theId, thePractitionerRole);
        if (practitionerRoleUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando paciente");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(practitionerRoleUpdated).setId(practitionerRoleUpdated.getIdElement());
    }

    public MethodOutcome deletePractitionerRole(IdType theId) {
        return practitionerRoleRepository.deletePractitionerRole(theId);
    }
//    TODO(El return)

    public OperationOutcome hasIdentifier(PractitionerRole thePractitionerRole) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (thePractitionerRole.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}