package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoAuthConfig {
    @Bean(name = "authMongoTemplate")
    @Qualifier("authMongoTemplate")
    public MongoTemplate authMongoTemplate() {
        return new MongoTemplate(
                new SimpleMongoClientDatabaseFactory("mongodb://localhost:27017/auth")
        );
    }
}
