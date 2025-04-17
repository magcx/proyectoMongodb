package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.example.demo.repository.CarePlanRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.CarePlan;
import org.springframework.stereotype.Service;

@Service
public class CarePlanService {
    private final CarePlanRepository carePlanRepository;

    public CarePlanService(CarePlanRepository carePlanRepository) {
        this.carePlanRepository = carePlanRepository;
    }

    public MethodOutcome createCarePlan(CarePlan theCarePlan) {
        OperationOutcome operationOutcome = hasIdentifier(theCarePlan);
        if (operationOutcome!= null) {
            return new MethodOutcome(theCarePlan.getIdElement(), operationOutcome, false);
        }
        return carePlanRepository.createCarePlan(theCarePlan);
    }

    public CarePlan readCarePlan(IdType theId) {
        return carePlanRepository.readCarePlan(theId);
    }

    public MethodOutcome updateCarePlan(IdType theId, CarePlan theCarePlan) {
        OperationOutcome operationOutcome = new OperationOutcome();
        CarePlan carePlanUpdated = carePlanRepository.updateCarePlan(theId, theCarePlan);
        if (carePlanUpdated == null) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setDiagnostics("Error actualizando plan de cuidados");
            return new MethodOutcome(operationOutcome).setCreated(false);
        }
        return new MethodOutcome().setResource(carePlanUpdated).setId(carePlanUpdated.getIdElement());
    }

    public MethodOutcome deleteCarePlan(IdType theId) {
        return carePlanRepository.deleteCarePlan(theId);
    }

    public OperationOutcome hasIdentifier(CarePlan theCarePlan) {
        OperationOutcome operationOutcome = new OperationOutcome();
        if (theCarePlan.getIdentifier().isEmpty()) {
            operationOutcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                    .setCode(OperationOutcome.IssueType.REQUIRED)
                    .setDiagnostics("Identificador requerido");
            return operationOutcome;
        }
        return null;
    }
}