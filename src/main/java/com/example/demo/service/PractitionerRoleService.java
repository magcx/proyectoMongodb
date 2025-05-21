package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PractitionerRoleService {
    private final ResourceRepository<PractitionerRole> repository;
    private final ResourceUtil<PractitionerRole> resourceUtil;

    public PractitionerRoleService(ResourceRepository<PractitionerRole> repository,
                                   ResourceUtil<PractitionerRole> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    public MethodOutcome createPractitionerRole(PractitionerRole thePractitionerRole, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(thePractitionerRole);
        if (operationOutcome!= null) {
            return resourceUtil.generateMethodOutcome(operationOutcome, 422, false);
        }
        String theId = resourceUtil.setId(thePractitionerRole);
        resourceUtil.setMeta(thePractitionerRole);
        return repository.createFhirResource(thePractitionerRole, theRequestDetails, theId, "practitionerRole",
                thePractitionerRole.getIdentifierFirstRep().getSystem(), thePractitionerRole.getIdentifierFirstRep().getValue());
    }

    public PractitionerRole readPractitionerRole(IdType theId) {
        return repository.readFhirResource(theId, "practitionerRole",
                PractitionerRole.class);
    }

    public MethodOutcome updatePractitionerRole(IdType theId, PractitionerRole thePractitionerRole) {
        PractitionerRole resourceUpdated = repository.updateFhirResource(theId, thePractitionerRole, "practitionerRole",
                PractitionerRole.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, thePractitionerRole);
    }

    public MethodOutcome deletePractitionerRole(IdType theId) {
        return repository.deleteFhirResource(theId,
                "practitionerRole");
    }

    public List<PractitionerRole> getPractitionerRoles() {
        return repository.getAllResourcesByType("practitionerRole",
                PractitionerRole.class);
    }

    public OperationOutcome hasIdentifier(PractitionerRole thePractitionerRole) {
        if (thePractitionerRole.getIdentifier().isEmpty()) {
            return resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.REQUIRED, "Identificador requerido");
        }
        return null;
    }
}