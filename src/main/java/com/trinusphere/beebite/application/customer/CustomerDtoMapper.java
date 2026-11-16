package com.trinusphere.beebite.application.customer;

import com.trinusphere.beebite.application.customer.dto.CustomerResponse;
import com.trinusphere.beebite.domain.customer.entity.Customer;

public final class CustomerDtoMapper {
    private CustomerDtoMapper() {
    }

    public static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId() != null ? c.getId().toString() : null, c.getName(), c.getPhone(),
                c.getEmail(), c.getZipCode(), c.getAddress(), c.getNumber(), c.getComplement(), c.getNeighborhood(),
                c.getTotalOrders(), null // createdAt will be set from entity
        );
    }
}
