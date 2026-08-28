package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Inject
    public UpdateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse execute(UUID storeId, UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id.toString()));
        StoreGuard.enforce(customer, storeId);

        customer.updateProfile(request.name(), request.phone(), request.email(), request.zipCode(), request.address(),
                request.number(), request.complement(), request.neighborhood());

        return CustomerDtoMapper.toResponse(customerRepository.save(customer));
    }
}
