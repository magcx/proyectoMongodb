package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FhirmongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhirmongoApplication.class, args);
	}
//TODO(- OR NOT(?) Build POJOs that extends from its resource type object from hapi fhir (see myorganization example in rest server
// starter github
// - Build resourceproviders that use BSON for persistance
// - Implement hapi fhir json parsers)
// - encodeResourceToString

}
