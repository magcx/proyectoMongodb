package com.example.demo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import com.example.demo.repository.ResourceRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Service;

import java.util.List;

//TODO(Security attributes inside resources json)
@Service
public class NutritionOrderService {
    private final ResourceRepository<NutritionOrder> repository;
    private final ResourceUtil<NutritionOrder> resourceUtil;

    public NutritionOrderService(ResourceRepository<NutritionOrder> repository, ResourceUtil<NutritionOrder> resourceUtil) {
        this.repository = repository;
        this.resourceUtil = resourceUtil;
    }

//   "The server SHALL populate the id, meta.versionId and meta.lastUpdated with the new correct values."
     public MethodOutcome createNutritionOrder(NutritionOrder theNutritionOrder, RequestDetails theRequestDetails) {
         String theId = resourceUtil.setId(theNutritionOrder);
         resourceUtil.setMeta(theNutritionOrder);
         return repository.createFhirResource(theNutritionOrder, theRequestDetails, theId,
                 "nutritionOrder", theNutritionOrder.getIdentifierFirstRep().getSystem(),
                 theNutritionOrder.getIdentifierFirstRep().getValue());
     }

     public NutritionOrder readNutritionOrder(IdType theId) {
         return repository.readFhirResource(theId, "nutritionOrder",
                 NutritionOrder.class);
     }

     public MethodOutcome updateNutritionOrder(IdType theId, NutritionOrder theNutritionOrder) {
         NutritionOrder resourceUpdated = repository.updateFhirResource(theId, theNutritionOrder,
                 "nutritionOrder",
                 NutritionOrder.class);
         if (resourceUpdated == null) {
             OperationOutcome oo =  resourceUtil.generateOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
                     OperationOutcome.IssueType.PROCESSING, "Error actualizando recurso");
             return resourceUtil.generateMethodOutcome(oo, 418, false);
         }
         return resourceUtil.generateMethodOutcomeWithRes(resourceUpdated.getIdElement(), 200,
                 false, theNutritionOrder);
     }

     public MethodOutcome deleteNutritionOrder(IdType theId) {
         return repository.deleteFhirResource(theId,
                 "nutritionOrder");
     }

    public List<NutritionOrder> getNutritionOrders(ReferenceParam patientRef) {
        return repository.getAllResourcesByRef(patientRef,"nutritionOrder", NutritionOrder.class);
    }
}