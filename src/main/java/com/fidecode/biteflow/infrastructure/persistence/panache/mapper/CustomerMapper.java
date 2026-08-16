package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.CustomerEntity;

public class CustomerMapper {

    private CustomerMapper() {}

    public static CustomerEntity toEntity(Customer c) {
        CustomerEntity e = new CustomerEntity();
        e.id = c.getId();
        e.storeId = c.getStoreId();
        e.name = c.getName();
        e.phone = c.getPhone();
        e.email = c.getEmail();
        e.zipCode = c.getZipCode();
        e.addressStreet = c.getAddress();
        e.addressNumber = c.getNumber();
        e.addressComplement = c.getComplement();
        e.addressNeighborhood = c.getNeighborhood();
        e.totalOrders = c.getTotalOrders();
        return e;
    }

    public static Customer toDomain(CustomerEntity e) {
        Customer c = new Customer(e.storeId, e.name);
        c.setId(e.id);
        c.setPhone(e.phone);
        c.setEmail(e.email);
        c.setZipCode(e.zipCode);
        c.setAddress(e.addressStreet);
        c.setNumber(e.addressNumber);
        c.setComplement(e.addressComplement);
        c.setNeighborhood(e.addressNeighborhood);
        c.setTotalOrders(e.totalOrders);
        return c;
    }
}
