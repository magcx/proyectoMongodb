package com.example.demo.service;

import com.example.demo.model.OrganizationUser;
import com.example.demo.repository.OrganizationUserRepository;
import org.checkerframework.checker.units.qual.A;
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
    AuthenticationManager authManager;
    @Autowired
    JWTService jwtService;

    private BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(12);

    public OrganizationUser register (OrganizationUser orgUser){
        orgUser.setUsername(bcryptEncoder.encode(orgUser.getUsername()));
        orgUser.setPassword(bcryptEncoder.encode(orgUser.getPassword()));
        orgUser.setName(bcryptEncoder.encode(orgUser.getName()));
        orgUser.setLastName(bcryptEncoder.encode(orgUser.getLastName()));
        orgUser.setEmail(bcryptEncoder.encode(orgUser.getEmail()));
        return orgUserRepo.insert(orgUser);
    }

    public Object verify(OrganizationUser orgUser) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(orgUser.getUsername(), orgUser.getPassword()));
        if (authentication.isAuthenticated()){
           jwtService.generateToken(orgUser.getUsername());
        }
         return
    }
}