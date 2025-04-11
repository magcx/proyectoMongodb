package com.example.demo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class FhirServletConfig {
//TODO(JWT, hapi fhir LoggingInterceptor class, see docs)

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoProperties mongoProperties) {
        return new SimpleMongoClientDatabaseFactory(mongoProperties.getUri());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }
    @Bean
    public JsonParser fhirJsonParser(FhirContext fhirContext) {
        return (JsonParser) fhirContext.newJsonParser();
    }

//    TODO(Validacion SNOMED CT snowstorm)
    @Bean
    public FhirValidator fhirValidator() {
        FhirContext ctx = fhirContext();
        FhirValidator fhirValidator = ctx.newValidator();
        ValidationSupportChain validationSupportChain = new ValidationSupportChain(
                new DefaultProfileValidationSupport(ctx), //Validación de StructureDefinition
                new SnapshotGeneratingValidationSupport(ctx), //Validación y generación de perfiles
                new CommonCodeSystemsTerminologyService(ctx), //Validación de CodeSystem
                new InMemoryTerminologyServerValidationSupport(ctx) //Validación de CodeSystem y ValueSet desde la memoria
        );
        CachingValidationSupport cachingValidationSupport = new CachingValidationSupport(validationSupportChain);
        //Contenedor de la estructura del validador
        FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator(ctx);
        fhirInstanceValidator.setValidationSupport(cachingValidationSupport);
        //Se crea un validador general con la configuración que se ha definido anteriormente
        fhirValidator.registerValidatorModule(fhirInstanceValidator);
        return fhirValidator;
    }

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServletRegistration(RestfulServer restfulServer, FhirContext fhirContext, MongoProperties mongoProperties
    ) {
        restfulServer.registerInterceptor(new InterceptorLogging());
        restfulServer.setProviders(new PatientResourceProvider(fhirJsonParser(fhirContext),
                                                                mongoTemplate(mongoDatabaseFactory(mongoProperties))));
        ServletRegistrationBean<RestfulServer> servletRegistrationBean = new ServletRegistrationBean<>(restfulServer,
                "/fhir/*");
        servletRegistrationBean.setName("fhirServlet");
        return servletRegistrationBean;
    }
}
