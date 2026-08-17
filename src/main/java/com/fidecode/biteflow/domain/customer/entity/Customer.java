package com.fidecode.biteflow.domain.customer.entity;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.util.UUID;

/**
 * Domain entity — Cliente.
 * POJO puro, sem dependência de framework.
 */
public class Customer implements StoreGuard.StoreScoped {

    private UUID id;
    private UUID storeId;
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
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.totalOrders = 0;
    }

    /**
     * Incrementa o contador de pedidos do cliente.
     */
    public void incrementTotalOrders() {
        this.totalOrders++;
    }

    // ── Getters ──
    public UUID getId()              { return id; }
    public UUID getStoreId()         { return storeId; }
    public String getName()          { return name; }
    public String getPhone()         { return phone; }
    public String getEmail()         { return email; }
    public String getZipCode()       { return zipCode; }
    public String getAddress()       { return address; }
    public String getNumber()        { return number; }
    public String getComplement()    { return complement; }
    public String getNeighborhood()  { return neighborhood; }
    public int getTotalOrders()      { return totalOrders; }

    // ── Setters ──
    public void setId(UUID id)                         { this.id = id; }
    public void setName(String name)                   { this.name = name; }
    public void setPhone(String phone)                 { this.phone = phone; }
    public void setEmail(String email)                 { this.email = email; }
    public void setZipCode(String zipCode)             { this.zipCode = zipCode; }
    public void setAddress(String address)             { this.address = address; }
    public void setNumber(String number)               { this.number = number; }
    public void setComplement(String complement)       { this.complement = complement; }
    public void setNeighborhood(String neighborhood)   { this.neighborhood = neighborhood; }
    public void setTotalOrders(int totalOrders)         { this.totalOrders = totalOrders; }
}
