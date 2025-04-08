package com.example.demo;

import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Component
public class FHIRServer extends RestfulServer {
    @Autowired
    private Validator validator;

    @Override
    protected void initialize() {
        FhirContext context = FhirContext.forR4();
        setFhirContext(context);

        ApplicationContext appCtx = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext());
        MongoTemplate mongoTemplate = appCtx.getBean(MongoTemplate.class);

        PatientResourceProvider patientProvider = new PatientResourceProvider();
        patientProvider.setMongoOperations(mongoTemplate);
        patientProvider.setValidator(validator);
        patientProvider.setFhirContext(context);
        registerProviders(patientProvider);
        registerInterceptor(new InterceptorLogging());
    }
}