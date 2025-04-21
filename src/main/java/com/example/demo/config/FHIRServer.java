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
//        TODO(Spring Security + JWT
//         - Mongo validation rules (en la DB)
        setFhirContext(getFhirContext());
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
        RequestValidatingInterceptor requestValidatingInterceptor = new RequestValidatingInterceptor();
        requestValidatingInterceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        requestValidatingInterceptor.setAddResponseHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
        requestValidatingInterceptor.setAddValidationResultsToResponseOperationOutcome(true);
        requestValidatingInterceptor.setResponseHeaderValue("${severity}: ${message}");
        requestValidatingInterceptor.setResponseHeaderValueNoIssues("No issues detected");
        requestValidatingInterceptor.setValidator(fhirValidator);
        return requestValidatingInterceptor;
    }

    public ResponseValidatingInterceptor responseValidatingInterceptor(FhirValidator fhirValidator) {
        ResponseValidatingInterceptor responseValidatingInterceptor = new ResponseValidatingInterceptor();
        responseValidatingInterceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        responseValidatingInterceptor.setAddResponseHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
        responseValidatingInterceptor.setResponseHeaderValue("${severity}: ${message}");
        responseValidatingInterceptor.setResponseHeaderValueNoIssues("No issues detected");
        responseValidatingInterceptor.setValidator(fhirValidator);
        return responseValidatingInterceptor;
    }

    public LoggingInterceptor loggingInterceptor() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLogExceptions(true);
        loggingInterceptor.setMessageFormat("${requestVerb} - " + "OperationType: ${operationType}, " +
                "RequestID: ${requestId}, " + "ResourceID/name: ${idOrResourceName}, " +
                "Request parameters: ${requestParameters}, " + "${processingTimeMillis} ms");
        loggingInterceptor.setErrorMessageFormat("${exceptionMessage}");
        loggingInterceptor.setLoggerName("the.logger");
        return loggingInterceptor;
    }
}