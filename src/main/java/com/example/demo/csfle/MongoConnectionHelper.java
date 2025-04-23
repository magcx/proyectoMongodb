package com.example.demo.csfle;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.vault.ClientEncryption;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

public class MongoConnectionHelper {
    private Map<String,Map<String,Object>> kmsProviders;
    private ClientEncryption encryption;
    private MongoClient client;
    private String LOCAL = "local";
    private ConnectionString CONNECTION_STR = new ConnectionString(System.getProperty("mongodb.uri"));
    private final MongoNamespace ENCRYPTED_NS = new MongoNamespace("fhirservidor", "patient");
    private final MongoNamespace VAULT_NS = new MongoNamespace("csfle", "vault");

    public MongoConnectionHelper(byte[] masterKey) {
        this.kmsProviders = generateKmsProvider(masterKey);
        this.encryption = createEncryptionClient();
        this.client = createClient();
    }

    private MongoClient createClient() {
        return null;
    }

    private ClientEncryption createEncryptionClient() {
        MongoClientSettings kvmcs = MongoClientSettings.builder().applyConnectionString(CONNECTION_STR).build();
        ClientEncryptionSettings encryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(kvmcs)
                .keyVaultNamespace(VAULT_NS.getFullName())
                .kmsProviders()
                .build();
        return null;
    }
//TODO(This has to be replace by the hashi vault)
    private Map<String, Map<String, Object>> generateKmsProvider(byte[] masterKey) {
        System.out.println("Generating KMS provider for master key");
        return new HashMap<>(){{
            put(LOCAL, new HashMap<>(){{
                put("key", masterKey);
            }});
        }};
    }
}
