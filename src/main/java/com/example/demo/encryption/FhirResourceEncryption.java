package com.example.demo.encryption;

import org.hl7.fhir.r4.model.*;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FhirResourceEncryption {

    public List<HumanName> processNames(List<HumanName> names, SecretKey theKey, Function<String, String> operation) {
        return names.stream().map(n -> {
            n.setGiven(n.getGiven().stream()
                    .map(g -> new StringType(operation.apply(g.getValue())))
                    .collect(Collectors.toList()));
            n.setFamily(operation.apply(n.getFamily()));
            return n;
        }).collect(Collectors.toList());
    }

    public List<Identifier> processIdentifiers(List<Identifier> identifiers, SecretKey theKey, Function<String, String> operation) {
        return identifiers.stream()
                .map(i -> new Identifier().setSystem(i.getSystem())
                        .setValue(operation.apply(i.getValue())))
                .collect(Collectors.toList());
    }

    public List<ContactPoint> processTelecoms(List<ContactPoint> telecoms, SecretKey theKey, Function<String, String> operation) {
        return telecoms.stream().map(t -> {
            t.setValue(operation.apply(t.getValue()));
            return t;
        }).collect(Collectors.toList());
    }

    public List<Address> processAddresses(List<Address> addresses, SecretKey theKey, Function<String, String> operation) {
        return addresses.stream().map(address -> {
            if (address.hasCity()) address.setCity(operation.apply(address.getCity()));
            if (address.hasCountry()) address.setCountry(operation.apply(address.getCountry()));
            if (address.hasDistrict()) address.setDistrict(operation.apply(address.getDistrict()));
            if (address.hasLine()) address.setLine(address.getLine().stream()
                    .map(line -> new StringType(operation.apply(line.getValue())))
                    .collect(Collectors.toList()));
            if (address.hasPostalCode()) address.setPostalCode(operation.apply(address.getPostalCode()));
            if (address.hasState()) address.setState(operation.apply(address.getState()));
            if (address.hasText()) address.setText(operation.apply(address.getText()));
            return address;
        }).collect(Collectors.toList());
    }

    public List<Patient.ContactComponent> processContacts(List<Patient.ContactComponent> contacts, SecretKey theKey, Function<String, String> operation) {
        return contacts.stream().map(contact -> {
            if (contact.hasName()) {
                contact.setName(processNames(List.of(contact.getName()), theKey, operation).get(0));
            }
            if (contact.hasTelecom()) {
                contact.setTelecom(processTelecoms(contact.getTelecom(), theKey, operation));
            }
            if (contact.hasAddress()) {
                contact.setAddress(processAddresses(List.of(contact.getAddress()), theKey, operation).get(0));
            }
            return contact;
        }).collect(Collectors.toList());
    }
}