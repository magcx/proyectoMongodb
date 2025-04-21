package com.example.demo.service;

import com.example.demo.model.OrganizationUser;
import com.example.demo.repository.OrganizationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OrganizationUserService {
    @Autowired
    private OrganizationUserRepository orgUserRepo;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(12);

    public OrganizationUser register (OrganizationUser orgUser){
        orgUser.setPassword(bcryptEncoder.encode(orgUser.getPassword()));
        orgUserRepo.insert(orgUser);
        return orgUser;
    }

    public String verify(OrganizationUser orgUser) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(orgUser.getUsername(), orgUser.getPassword()));
        if (authentication.isAuthenticated()){
           return jwtService.generateToken(orgUser.getUsername());
        }
        return "fail";
    }
}