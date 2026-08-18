package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Inject
    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse execute(UUID storeId, CustomerRequest request) {
        Customer customer = new Customer(storeId, request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setZipCode(request.zipCode());
        customer.setAddress(request.address());
        customer.setNumber(request.number());
        customer.setComplement(request.complement());
        customer.setNeighborhood(request.neighborhood());

        Customer saved = customerRepository.save(customer);
        return CustomerDtoMapper.toResponse(saved);
    }
}
