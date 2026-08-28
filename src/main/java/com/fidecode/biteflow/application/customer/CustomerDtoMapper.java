package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;

public final class CustomerDtoMapper {
    private CustomerDtoMapper() {}
    public static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
            c.getId() != null ? c.getId().toString() : null,
            c.getName(),
            c.getPhone(),
            c.getEmail(),
            c.getZipCode(),
            c.getAddress(),
            c.getNumber(),
            c.getComplement(),
            c.getNeighborhood(),
            c.getTotalOrders(),
            null // createdAt will be set from entity
        );
    }
}
