package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarePlanService {
    private final ResourceRepository<CarePlan> repository;
    private final ResourceUtil<CarePlan> resourceUtil;

    public CarePlanService(ResourceRepository<CarePlan> repository, ResourceUtil<CarePlan> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

    public MethodOutcome createCarePlan(CarePlan theCarePlan, RequestDetails theRequestDetails) {
        String theId = resourceUtil.setId(theCarePlan);
        resourceUtil.setMeta(theCarePlan);
        return repository.createFhirResource(theCarePlan, theRequestDetails, theId,
                "carePlan", theCarePlan.getIdentifierFirstRep().getSystem(),
                theCarePlan.getIdentifierFirstRep().getValue());
    }

    public CarePlan readCarePlan(IdType theId) {
        return repository.readFhirResource(theId, "carePlan",
                CarePlan.class);
    }

    public MethodOutcome updateCarePlan(IdType theId, CarePlan theCarePlan) {
        CarePlan resourceUpdated = repository.updateFhirResource(theId, theCarePlan,
                "carePlan",
                CarePlan.class);
        if (resourceUpdated == null) {
            OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                    OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
            return resourceUtil.generateMethodOutcome(oo, 418, false);
        }
        return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                false, theCarePlan);
    }

    public MethodOutcome deleteCarePlan(IdType theId) {
        return repository.deleteFhirResource(theId,
                "carePlan");
    }

    public List<CarePlan> getCarePlans(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef, "carePlan", CarePlan.class);
    }
}