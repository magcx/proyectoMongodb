package com.example.demo.config;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import com.example.demo.controller.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class FHIRServer extends RestfulServer {
    private final JsonParser jsonParser;
    private final MongoTemplate mongoTemplate;
    private final FhirValidator fhirValidator;

    @Autowired
    public FHIRServer(JsonParser jsonParser, MongoTemplate mongoTemplate, FhirValidator fhirValidator) {
        this.jsonParser = jsonParser;
        this.mongoTemplate = mongoTemplate;
        this.fhirValidator = fhirValidator;
    }

    @Override
    protected void initialize() {
//      TODO(Interceptor logging más robusto y Nginx)
        registerInterceptor(loggingInterceptor());
        registerInterceptor(requestValidatingInterceptor(fhirValidator));
        registerInterceptor(responseValidatingInterceptor(fhirValidator));
        setProviders(new ProcedureResourceProvider(jsonParser, mongoTemplate),
                new PractitionerRoleResourceProvider(jsonParser, mongoTemplate),
                new CarePlanResourceProvider(jsonParser, mongoTemplate),
                new ObservationResourceProvider(jsonParser, mongoTemplate),
                new PatientResourceProvider(jsonParser, mongoTemplate));
    }

    public RequestValidatingInterceptor requestValidatingInterceptor(FhirValidator fhirValidator) {
        RequestValidatingInterceptor requestInterceptor = new RequestValidatingInterceptor();
        requestInterceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        requestInterceptor.setAddResponseHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
        requestInterceptor.setResponseHeaderValue("Validation on ${line}: ${message} ${severity}");
        requestInterceptor.setResponseHeaderValueNoIssues("No issues detected");
        requestInterceptor.setValidator(fhirValidator);
        return requestInterceptor;
    }

    public ResponseValidatingInterceptor responseValidatingInterceptor(FhirValidator fhirValidator) {
        ResponseValidatingInterceptor responseValidatingInterceptor = new ResponseValidatingInterceptor();
        responseValidatingInterceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        responseValidatingInterceptor.setAddResponseHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
        responseValidatingInterceptor.setResponseHeaderValue("Validation on ${line}: ${message} ${severity}");
        responseValidatingInterceptor.setResponseHeaderValueNoIssues("No issues detected");
        responseValidatingInterceptor.setValidator(fhirValidator);
        return responseValidatingInterceptor;
    }

    public LoggingInterceptor loggingInterceptor() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLogExceptions(true);
        loggingInterceptor.setMessageFormat("${requestVerb} - " + "RequestID: ${requestId}, " +
                "OperationType: ${operationType}, "+
                "Request parameters: ${requestParameters}, " + "ResourceID/name: ${idOrResourceName}, " +
                "${processingTimeMillis} ms");
        loggingInterceptor.setErrorMessageFormat("${exceptionMessage}");
        loggingInterceptor.setLoggerName("the.logger");
        return loggingInterceptor;
    }
}