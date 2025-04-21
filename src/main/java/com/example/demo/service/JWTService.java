package com.example.demo.service;

import io.jsonwebtoken.Jwts;

import java.util.HashMap;
import java.util.Map;

public class JWTService {
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<String, Object>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username);
    }
}
