package com.example.demo.service;

import com.example.demo.model.OrganizationUser;
import com.example.demo.model.UserPrincipal;
import com.example.demo.repository.OrganizationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    OrganizationUserRepository orgUserRep;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        OrganizationUser orgUser = orgUserRep.findByUsername(username);
        if (orgUser == null) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("User not found");
        }
        return new UserPrincipal(orgUser);
    }
}