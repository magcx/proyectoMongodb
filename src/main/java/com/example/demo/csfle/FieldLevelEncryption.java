package com.example.demo.csfle;

import com.mongodb.MongoNamespace;
import org.bson.json.JsonWriterSettings;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class FieldLevelEncryption {
    private final int MASTER_KEY_SIZE = 96;
    private String DETERMINISTIC = "AEAD_AES_256_GCM_HMAC_SHA512-Deterministic";
    private String RANDOM = "AEAD_AES_256_GCM_HMAC_SHA512-Random";
    private MongoNamespace ENCRYPTED_NS = new MongoNamespace("fhirservidor", "patient");
    private JsonWriterSettings INDENT = JsonWriterSettings.builder().indent(true).build();
    private byte[] masterKey;
    private SecureRandom SECURE_RANDOM = new SecureRandom();

    public byte[] obtainMasterKey(String filename) {
        masterKey = new byte[MASTER_KEY_SIZE];
        try {
            retrieveMasterKey(filename, masterKey);
            System.out.println("An existing master key was found");
        } catch (IOException e){
            masterKey = generateMasterKey();
            saveMasterKeyToFile(filename,masterKey);
            System.out.println("New master key generated");
        }
        return masterKey;
    }

    private void saveMasterKeyToFile(String filename, byte[] masterKey) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(masterKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] generateMasterKey() {
        masterKey = new byte[MASTER_KEY_SIZE];
        SECURE_RANDOM.nextBytes(masterKey);
        return masterKey;
    }

    private void retrieveMasterKey(String filename, byte[] masterKey) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            fis.read(masterKey, 0, MASTER_KEY_SIZE);
        }
    }
}
