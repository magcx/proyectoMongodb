package com.example.demo.config;

import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.stereotype.Component;

@Component
public class FHIRServer extends RestfulServer {
    @Override
    protected void initialize() {

    }
}
