package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.customer.entity.Customer;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.CustomerEntity;

public class CustomerMapper {

    private CustomerMapper() {
    }

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
        return Customer.reconstitute(e.id, e.storeId, e.name, e.phone, e.email, e.zipCode, e.addressStreet,
                e.addressNumber, e.addressComplement, e.addressNeighborhood, e.totalOrders);
    }
}
