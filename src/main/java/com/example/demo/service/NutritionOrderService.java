package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import com.example.demo.repository.NutritionOrderRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

//TODO(Security attributes inside resources json)
@Service
public class NutritionOrderService {
     private final NutritionOrderRepository nutritionOrderRepository;

     public NutritionOrderService(NutritionOrderRepository nutritionOrderRepository) {
         this.nutritionOrderRepository = nutritionOrderRepository;
     }

//   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
     public MethodOutcome createNutritionOrder(NutritionOrder theNutritionOrder, RequestDetails theRequestDetails) {
        OperationOutcome operationOutcome = hasIdentifier(theNutritionOrder);
        if (operationOutcome!= null) {
            MethodOutcome methodOutcome = new MethodOutcome();
            methodOutcome.setResponseStatusCode(422);
            methodOutcome.setCreated(false);
            methodOutcome.setOperationOutcome(operationOutcome);
            return methodOutcome;
        }
        Meta meta = new Meta();
        String theId = UUID.randomUUID().toString();
        theNutritionOrder.setId(theId);
        meta.setVersionId("1");
        meta.setLastUpdated(new Date(System.currentTimeMillis()));
        theNutritionOrder.setMeta(meta);
        return nutritionOrderRepository.createNutritionOrder(theNutritionOrder, theRequestDetails, theId);
     }

     public NutritionOrder readNutritionOrder(IdType theId) {
         return nutritionOrderRepository.readNutritionOrder(theId);
     }

     public MethodOutcome updateNutritionOrder(IdType theId, NutritionOrder theNutritionOrder) {
         OperationOutcome operationOutcome = new OperationOutcome();
         NutritionOrder nutritionOrderUpdated = nutritionOrderRepository.updateNutritionOrder(theId, theNutritionOrder);
         if (nutritionOrderUpdated == null) {
             operationOutcome.addIssue()
                     .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                     .setDiagnostics("Error actualizando");
             return new MethodOutcome(operationOutcome).setCreated(false);
         }
         return new MethodOutcome().setResource(nutritionOrderUpdated).setId(nutritionOrderUpdated.getIdElement());
     }

     public MethodOutcome deleteNutritionOrder(IdType theId) {
         return nutritionOrderRepository.deleteNutritionOrder(theId);
     }
//    TODO(El return)

     public List<NutritionOrder> getNutritionOrders() {
         return nutritionOrderRepository.getNutritionOrders();
     }

     public OperationOutcome hasIdentifier(NutritionOrder theNutritionOrder) {
         OperationOutcome operationOutcome = new OperationOutcome();
         if (theNutritionOrder.getIdentifier().isEmpty()) {
             operationOutcome.addIssue()
                     .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                     .setCode(OperationOutcome.IssueType.REQUIRED)
                     .setDiagnostics("Identificador requerido");
             return operationOutcome;
         }
         return null;
     }
}