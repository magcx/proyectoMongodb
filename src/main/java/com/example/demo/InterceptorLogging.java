package com.example.demo;

import ca.uhn.fhir.interceptor.api.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;

@Interceptor
public class InterceptorLogging {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
    public void logRequest(RequestDetails requestDetails) {
        System.out.println(">>> [REQUEST] Método: " + requestDetails.getRequestType() +
                ", Path: " + requestDetails.getCompleteUrl());
    }
}
