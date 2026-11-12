package com.fidecode.beebite.application.customer;

import com.fidecode.beebite.application.customer.dto.CustomerResponse;
import com.fidecode.beebite.domain.customer.entity.Customer;
import com.fidecode.beebite.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.beebite.domain.customer.repository.CustomerRepository;
import com.fidecode.beebite.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Inject
    public GetCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse execute(UUID storeId, UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id.toString()));
        StoreGuard.enforce(customer, storeId);
        return CustomerDtoMapper.toResponse(customer);
    }
}
