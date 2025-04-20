package com.example.demo.model;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public class OrganizationUser {
    private String id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String organization;
    private Date contractStart;
    private Date contractEnd;

    public OrganizationUser(String id, String username, String password, String role, String email, String organization,
                            Date contractStart, Date contractEnd) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.organization = organization;
        this.contractStart = contractStart;
        this.contractEnd = contractEnd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Date getContractStart() {
        return contractStart;
    }

    public void setContractStart(Date contractStart) {
        this.contractStart = contractStart;
    }

    public Date getContractEnd() {
        return contractEnd;
    }

    public void setContractEnd(Date contractEnd) {
        this.contractEnd = contractEnd;
    }
}
