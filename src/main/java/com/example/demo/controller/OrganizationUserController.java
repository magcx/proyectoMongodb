package com.example.demo.controller;

import com.example.demo.model.OrganizationUser;
import com.example.demo.service.OrganizationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationUserController {
    @Autowired
    private OrganizationUserService orgUserService;

    @PostMapping("/register")
    public OrganizationUser register(@RequestBody OrganizationUser organizationUser) {
        return orgUserService.register(organizationUser);
    }

    @PostMapping("/login")
    public String login (@RequestBody OrganizationUser orgUser){
        return orgUserService.verify(orgUser);
    }
}
