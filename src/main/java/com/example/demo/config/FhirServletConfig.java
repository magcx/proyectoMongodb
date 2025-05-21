package com.example.demo.config;

import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirServletConfig {
    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServletRegistration(RestfulServer restfulServer) {
        ServletRegistrationBean<RestfulServer> servletRegistrationBean = new ServletRegistrationBean<>(restfulServer,
                "/fhir/*");
        servletRegistrationBean.setName("fhirServlet");
        return servletRegistrationBean;
    }
}