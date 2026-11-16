package com.trinusphere.beebite.application.customer;

import com.trinusphere.beebite.application.customer.dto.CustomerRequest;
import com.trinusphere.beebite.application.customer.dto.CustomerResponse;
import com.trinusphere.beebite.domain.customer.entity.Customer;
import com.trinusphere.beebite.domain.customer.repository.CustomerRepository;
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
        customer.updateProfile(customer.getName(), request.phone(), request.email(), request.zipCode(),
                request.address(), request.number(), request.complement(), request.neighborhood());

        Customer saved = customerRepository.save(customer);
        return CustomerDtoMapper.toResponse(saved);
    }
}
