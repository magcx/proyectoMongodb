package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.PractitionerRoleRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class PractitionerRoleService {
    private final PractitionerRoleRepository practitionerRoleRepository;

    public PractitionerRoleService(PractitionerRoleRepository practitionerRoleRepository) {
        this.practitionerRoleRepository = practitionerRoleRepository;
    }

    public MethodOutcome createPractitionerRole(PractitionerRole thePractitionerRole, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(thePractitionerRole);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        thePractitionerRole.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        thePractitionerRole.setMeta(meta);
        return practitionerRoleRepository.createPractitionerRole(thePractitionerRole,theRequestDetails, theId);
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