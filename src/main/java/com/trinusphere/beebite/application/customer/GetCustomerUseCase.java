package com.trinusphere.beebite.application.customer;

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
