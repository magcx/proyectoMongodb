package com.example.demo.service;

import com.example.demo.model.OrganizationUser;
import com.example.demo.model.UserPrincipal;
import com.example.demo.repository.OrganizationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    @Qualifier("authMongoTemplate")
    private MongoTemplate authMongoTemplate;
    @Autowired
    OrganizationUserRepository orgUserRep;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        OrganizationUser orgUser = orgUserRep.findByUsername(username);
        if (orgUser == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(orgUser);
    }
}
