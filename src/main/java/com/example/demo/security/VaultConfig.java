package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.*;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultConfig extends AbstractVaultConfiguration {

    @Bean
    @Override
    public VaultEndpoint vaultEndpoint() {
        return new VaultEndpoint();
    }
    @Bean
    @Override
    public ClientAuthentication clientAuthentication() {
        return new TokenAuthentication(getEnvironment().getProperty("vault.token"));
    }

    @Bean
    public VaultTemplate vaultTemplate(VaultEndpoint vaultEndpoint, ClientAuthentication clientAuthentication ) {
        return new VaultTemplate(vaultEndpoint,clientAuthentication);
    }
}git