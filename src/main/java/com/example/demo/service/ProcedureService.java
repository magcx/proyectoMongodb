package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcedureService {
    private final ResourceRepository<Procedure> repository;
    private final ResourceUtil<Procedure> resourceUtil;

    public ProcedureService(ResourceRepository<Procedure> repository, ResourceUtil<Procedure> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    public MethodOutcome createProcedure(Procedure theProcedure, RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theProcedure);
        resourceUtil.setMeta(theProcedure);
        return repository.createFhirResource(theProcedure, theRequestDetails, theId,
                "procedure", theProcedure.getIdentifierFirstRep().getSystem(),
                theProcedure.getIdentifierFirstRep().getValue());
    }

    public Procedure readProcedure(IdType theId) {
        return repository.readFhirResource(theId, "procedure",
                Procedure.class);
    }

    public MethodOutcome updateProcedure(IdType theId, Procedure theProcedure) {
        Procedure resourceUpdated = repository.updateFhirResource(theId, theProcedure,
                "procedure",
                Procedure.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theProcedure);
    }

    public MethodOutcome deleteProcedure(IdType theId) {
        return repository.deleteFhirResource(theId,
                "procedure");
    }

    public List<Procedure> getProcedures(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "procedure", Procedure.class);
    }
}