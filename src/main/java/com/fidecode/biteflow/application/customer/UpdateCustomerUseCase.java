package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
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
        if (!customer.getStoreId().equals(storeId)) throw new ForbiddenAccessException("Acesso negado");

        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setZipCode(request.zipCode());
        customer.setAddress(request.address());
        customer.setNumber(request.number());
        customer.setComplement(request.complement());
        customer.setNeighborhood(request.neighborhood());

        return CustomerDtoMapper.toResponse(customerRepository.save(customer));
    }
}
