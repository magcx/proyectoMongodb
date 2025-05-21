package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AllergyIntoleranceService {
    private final ResourceRepository<AllergyIntolerance> repository;
    private final ResourceUtil<AllergyIntolerance> resourceUtil;

    public AllergyIntoleranceService(ResourceRepository<AllergyIntolerance> repository,
                                     ResourceUtil<AllergyIntolerance> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    //   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
    public MethodOutcome createAllergyIntolerance(AllergyIntolerance theAllergyIntolerance,
                                                  RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theAllergyIntolerance);
        resourceUtil.setMeta(theAllergyIntolerance);
        return repository.createFhirResource(theAllergyIntolerance, theRequestDetails, theId,
                "allergyIntolerance", theAllergyIntolerance.getIdentifierFirstRep().getSystem(),
                theAllergyIntolerance.getIdentifierFirstRep().getValue());
    }

    public AllergyIntolerance readAllergyIntolerance(IdType theId) {
        return repository.readFhirResource(theId, "allergyIntolerance",
                AllergyIntolerance.class);
    }

    public MethodOutcome updateAllergyIntolerance(IdType theId, AllergyIntolerance theAllergyIntolerance) {
        AllergyIntolerance resourceUpdated = repository.updateFhirResource(theId, theAllergyIntolerance,
                "allergyIntolerance",
                AllergyIntolerance.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theAllergyIntolerance);
    }

    public MethodOutcome deleteAllergyIntolerance(IdType theId) {
        return repository.deleteFhirResource(theId,
                "allergyIntolerance");
    }

    public List<AllergyIntolerance> getAllergiesIntolerances(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "allergyIntolerance", AllergyIntolerance.class);
    }
}