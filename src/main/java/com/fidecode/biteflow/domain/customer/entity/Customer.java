package com.fidecode.biteflow.domain.customer.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.util.UUID;

public class Customer implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private String phone;
    private String email;
    private String zipCode;
    private String address;
    private String number;
    private String complement;
    private String neighborhood;
    private int totalOrders;

    public Customer(UUID storeId, String name) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
    }

    public static Customer reconstitute(UUID id, UUID storeId, String name, String phone, String email, String zipCode,
            String address, String number, String complement, String neighborhood, int totalOrders) {
        Customer c = new Customer(storeId, name);
        c.id = id;
        c.phone = phone;
        c.email = email;
        c.zipCode = zipCode;
        c.address = address;
        c.number = number;
        c.complement = complement;
        c.neighborhood = neighborhood;
        c.totalOrders = totalOrders;
        return c;
    }

    public void updateProfile(String name, String phone, String email, String zipCode, String address, String number,
            String complement, String neighborhood) {
        if (name != null && !name.isBlank())
            this.name = name;
        this.phone = phone;
        this.email = email;
        this.zipCode = zipCode;
        this.address = address;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
    }

    public void incrementTotalOrders() {
        totalOrders++;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getAddress() {
        return address;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}