package com.example.demo.repository;

import com.example.demo.model.OrganizationUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationUserRepository extends MongoRepository <OrganizationUser, String> {
    OrganizationUser findByUsername(String username);
}
