package com.example.demo;

import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.stereotype.Component;

@Component
public class FHIRServer extends RestfulServer {
    @Override
    protected void initialize() {
    }
}



//package com.example.demo;

//
//import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.context.support.ConceptValidationOptions;
//import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
//import ca.uhn.fhir.context.support.ValidationSupportContext;
//import ca.uhn.fhir.parser.JsonParser;
//import ca.uhn.fhir.rest.server.RestfulServer;
//import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
//import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
//import ca.uhn.fhir.validation.FhirValidator;
//import jakarta.servlet.annotation.WebServlet;
//import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
//import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
//import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
//import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
//import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
//import org.springframework.boot.autoconfigure.mongo.MongoProperties;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.mongodb.MongoDatabaseFactory;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
////TODO(MIGRAR config? popular initialize y mantener config separando responsabilidades)
////@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
//@Component
//public class FHIRServer extends RestfulServer {
//    private final SnowstormTerminologyAdapter snowstormTerminologyAdapter;
//
//    public FHIRServer(SnowstormTerminologyAdapter snowstormTerminologyAdapter) {
//        this.snowstormTerminologyAdapter = snowstormTerminologyAdapter;
//    }
//    @Override
//    protected void initialize() {
//        System.out.println(">>> FHIR Server inicializado correctamente en /fhir");
//        registerInterceptor(new InterceptorLogging());
//        registerInterceptor(new ValidationInterceptor(fhirValidator()));
//        setProviders(new PatientResourceProvider(fhirJsonParser(fhirContext),
//                mongoTemplate(mongoDatabaseFactory(mongoProperties))));
//        ServletRegistrationBean<RestfulServer> servletRegistrationBean = new ServletRegistrationBean<>(restfulServer,
//                "/fhir/*");
//        servletRegistrationBean.setName("fhirServlet");
//        servletRegistrationBean.setLoadOnStartup(1);
//    }
//
//    public FhirValidator fhirValidator() {
//        RestTemplate restTemplate = restTemplate();
//        FhirContext ctx = fhirContext();
//        FhirValidator fhirValidator = ctx.newValidator();
//        RemoteTerminologyServiceValidationSupport remoteSupport =
//                new RemoteTerminologyServiceValidationSupport(ctx) {
//                    @Override
//                    public CodeValidationResult validateCode(ValidationSupportContext context, ConceptValidationOptions options,
//                                                             String codeSystem, String code, String display, String valueSetUrl) {
//                        if ("http://snomed.info/sct".equals(codeSystem)) {
//                            // Lógica personalizada para Snowstorm
//                            String url = "http://localhost:8080/browser/MAIN/SNOMEDCT-ES/concepts/" + code;
//                            try {
//                                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                                if (response.getStatusCode().is2xxSuccessful()) {
//                                    return new CodeValidationResult()
//                                            .setCode(code)
//                                            .setDisplay(display);
//                                }
//                            } catch (Exception e) {
//                                return new CodeValidationResult()
//                                        .setMessage("Error al validar con Snowstorm: " + e.getMessage());
//                            }
//                        }
//                        return super.validateCode(context, options, codeSystem, code, display, valueSetUrl);
//                    }
//                };
//        remoteSupport.setBaseUrl("http://localhost:8081/fhir-terminology");
//
//        ValidationSupportChain validationSupportChain = new ValidationSupportChain(
//                new DefaultProfileValidationSupport(ctx), //Validación de
//                remoteSupport,
//                new InMemoryTerminologyServerValidationSupport(ctx),
//                new CommonCodeSystemsTerminologyService(ctx)
////              new SnapshotGeneratingValidationSupport(ctx), //Validación y generación de perfiles
//        );
////        CachingValidationSupport cachingValidationSupport = new CachingValidationSupport(validationSupportChain);
//        //Contenedor de la estructura del validador
//        FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator(ctx);
//        fhirInstanceValidator.setAnyExtensionsAllowed(true);
//        fhirInstanceValidator.setValidationSupport(validationSupportChain);
//        //Se crea un validador general con la configuración que se ha definido anteriormente
//        fhirValidator.registerValidatorModule(fhirInstanceValidator);
//        return fhirValidator;
//    }
//
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//
//    public MongoDatabaseFactory mongoDatabaseFactory(MongoProperties mongoProperties) {
//        return new SimpleMongoClientDatabaseFactory(mongoProperties.getUri());
//    }
//
//    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
//        return new MongoTemplate(mongoDatabaseFactory);
//    }
//
//    public FhirContext fhirContext() {
//        return FhirContext.forR4();
//    }
//
//    public JsonParser fhirJsonParser(FhirContext fhirContext) {
//        return (JsonParser) fhirContext.newJsonParser();
//    }
//}
