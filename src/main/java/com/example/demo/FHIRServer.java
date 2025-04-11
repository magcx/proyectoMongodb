package com.example.demo;

import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.stereotype.Component;

@Component
public class FHIRServer extends RestfulServer {
    @Override
    protected void initialize() {
    }
}