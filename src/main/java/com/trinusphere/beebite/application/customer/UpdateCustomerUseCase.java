package com.trinusphere.beebite.application.customer;

import com.trinusphere.beebite.application.customer.dto.CustomerRequest;
import com.trinusphere.beebite.application.customer.dto.CustomerResponse;
import com.trinusphere.beebite.domain.customer.entity.Customer;
import com.trinusphere.beebite.domain.customer.exception.CustomerNotFoundException;
import com.trinusphere.beebite.domain.customer.repository.CustomerRepository;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.shared.StoreGuard;
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
