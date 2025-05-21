package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationStatementService {
    private final ResourceRepository<MedicationStatement> repository;
    private final ResourceUtil<MedicationStatement> resourceUtil;

    public MedicationStatementService(ResourceRepository<MedicationStatement> repository,
                                      ResourceUtil<MedicationStatement> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createMedicationStatement(MedicationStatement theMedicationStatement,
                                                   RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theMedicationStatement);
        resourceUtil.setMeta(theMedicationStatement);
        return repository.createFhirResource(theMedicationStatement, theRequestDetails, theId,
                "medicationStatement", theMedicationStatement.getIdentifierFirstRep().getSystem(),
                theMedicationStatement.getIdentifierFirstRep().getValue());
    }

    public MedicationStatement readMedicationStatement(IdType theId) {
        return repository.readFhirResource(theId, "medicationStatement",
                MedicationStatement.class);
    }

    public MethodOutcome updateMedicationStatement(IdType theId, MedicationStatement theMedicationStatement) {
        MedicationStatement resourceUpdated = repository.updateFhirResource(theId, theMedicationStatement,
                "medicationStatement",
                MedicationStatement.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theMedicationStatement);
    }

    public MethodOutcome deleteMedicationStatement(IdType theId) {
        return repository.deleteFhirResource(theId,
                "medicationStatement");
    }

    public List<MedicationStatement> getMedicationStatements(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "medicationStatement", MedicationStatement.class);
    }
}