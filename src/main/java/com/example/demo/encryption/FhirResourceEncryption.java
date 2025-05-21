package com.example.demo.encryption;

import org.hl7.fhir.r4.model.*;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.stream.Collectors;

public class FhirResourceEncryption {

    public List<HumanName> encryptName(List<HumanName> names, SecretKey theKey) {
        return names.stream().map(n -> {
                    List<StringType> encryptedGiven = n.getGiven().stream().map(g ->
                            new StringType(DataEncryption.encrypt(g.getValue(), theKey))).collect(Collectors.toList());
                    n.setGiven(encryptedGiven);
                    n.setFamily(DataEncryption.encrypt(n.getFamily(), theKey));
                    return n;
                })
                .collect(Collectors.toList());
    }

    public List<Identifier> encryptIdentifier(List<Identifier> identifiers, SecretKey theKey) {
        return identifiers.stream()
                .map(i -> new Identifier().setSystem(i.getSystem()).setValue(
                        DataEncryption.encrypt(i.getValue(), theKey))
                ).collect(Collectors.toList());
    }

    public List<ContactPoint> encryptTelecom(List<ContactPoint> telecoms, SecretKey theKey) {
        return telecoms.stream().map(t -> {
            t.setValue(DataEncryption.encrypt(t.getValue(), theKey));
            return t;
        }).collect(Collectors.toList());
    }

    public List<Address> encryptAddress(List<Address> addresses, SecretKey theKey) {
        return addresses.stream()
                .map(address -> {
                    if (address.hasCity()) address.setCity(DataEncryption.encrypt(address.getCity(), theKey));
                    if (address.hasCountry()) address.setCountry(DataEncryption.encrypt(address.getCountry(), theKey));
                    if (address.hasDistrict()) address.setDistrict(DataEncryption.encrypt(address.getDistrict(), theKey));
                    if (address.hasLine()) address.setLine(address.getLine().stream()
                            .map(line -> new StringType(DataEncryption.encrypt(line.getValue(), theKey)))
                            .collect(Collectors.toList()));
                    if (address.hasPostalCode()) address.setPostalCode(DataEncryption.encrypt(address.getPostalCode(), theKey));
                    if (address.hasState()) address.setState(DataEncryption.encrypt(address.getState(), theKey));
                    if (address.hasText()) address.setText(DataEncryption.encrypt(address.getText(), theKey));
                    return address;
                }).collect(Collectors.toList());
    }

    public List<Patient.ContactComponent> encryptContact(List<Patient.ContactComponent> contacts, SecretKey theKey) {
        return contacts.stream()
                .map(contact -> {
                    if (contact.hasName()) {
                        contact.setName(encryptName(List.of(contact.getName()), theKey).getFirst());
                    }
                    if (contact.hasTelecom()) {
                        contact.setTelecom(encryptTelecom(contact.getTelecom(), theKey));
                    }
                    if (contact.hasAddress()) {
                        contact.setAddress(encryptAddress(List.of(contact.getAddress()), theKey).getFirst());
                    }
                    return contact;
                }).collect(Collectors.toList());
    }
}