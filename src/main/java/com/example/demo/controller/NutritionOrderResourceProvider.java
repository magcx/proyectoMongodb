package com.example.demo.controller;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.NutritionOrderService;
import com.example.demo.util.ResourceUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.NutritionOrder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NutritionOrderResourceProvider implements IResourceProvider {
    private final NutritionOrderService service;

    public NutritionOrderResourceProvider(JsonParser jsonParser, MongoTemplate mongoTemplate) {
        super();
        ResourceRepository<NutritionOrder> repository = new ResourceRepository<>(mongoTemplate, jsonParser);
        ResourceUtil<NutritionOrder> resourceUtil = new ResourceUtil<>();
        this.service = new NutritionOrderService(repository, resourceUtil);
    }

    @Override
    public Class<NutritionOrder> getResourceType() {
        return NutritionOrder.class;
    }

    //CRUD
//   Para devolver 201 Created necesita .setCreated true, .setID y setResource
    @Create
    public MethodOutcome createNutritionOrder(@ResourceParam NutritionOrder theNutritionOrder, RequestDetails theRequestDetails) {
        return service.createNutritionOrder(theNutritionOrder, theRequestDetails);
    }

    //   OK
    @Read()
    public NutritionOrder readNutritionOrder(@IdParam IdType theId) {
        return service.readNutritionOrder(theId);
    }

    //    OK
    @Update
    public MethodOutcome updateNutritionOrder(@IdParam IdType theId, @ResourceParam NutritionOrder theNutritionOrder) {
        return service.updateNutritionOrder(theId, theNutritionOrder);
    }

    @Delete
    public MethodOutcome deleteNutritionOrder(@IdParam IdType theId) {
        return service.deleteNutritionOrder(theId);
    }

    @Search
    public List<NutritionOrder> searchNutritionOrders(@RequiredParam(name = NutritionOrder.SP_PATIENT) ReferenceParam patientRef){
        return service.getNutritionOrders(patientRef);
    }
}