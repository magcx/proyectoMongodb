package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.NutritionOrderRepository;
import com.example.demo.service.NutritionOrderService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NutritionOrderResourceProvider implements IResourceProvider {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final NutritionOrderRepository nutritionOrderRepository;
    private final NutritionOrderService nutritionOrderService;

    public NutritionOrderResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.nutritionOrderRepository = new NutritionOrderRepository(mongoTemplate, jsonParser);
        this.nutritionOrderService = new NutritionOrderService(nutritionOrderRepository);
    }

    @Override
    public Class<NutritionOrder> getResourceType() {
        return NutritionOrder.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createNutritionOrder(@ResourceParam NutritionOrder theNutritionOrder, RequestDetails theRequestDetails) {
        return nutritionOrderService.createNutritionOrder(theNutritionOrder, theRequestDetails);
    }

    //   OK
    @Read()
    public NutritionOrder readNutritionOrder(@IdParam IdType theId) {
        return nutritionOrderService.readNutritionOrder(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateNutritionOrder(@IdParam IdType theId, @ResourceParam NutritionOrder theNutritionOrder) {
        return nutritionOrderService.updateNutritionOrder(theId, theNutritionOrder);
    }

    @Delete
    public MethodOutcome deleteNutritionOrder(@IdParam IdType theId) {
        return nutritionOrderService.deleteNutritionOrder(theId);
    }

    @Search
    public List<NutritionOrder> searchNutritionOrder(){
        return nutritionOrderService.getNutritionOrders();
    }
}