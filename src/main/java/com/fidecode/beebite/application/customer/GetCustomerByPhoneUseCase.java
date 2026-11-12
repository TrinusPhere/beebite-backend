package com.fidecode.beebite.application.customer;

import com.fidecode.beebite.application.customer.dto.CustomerResponse;
import com.fidecode.beebite.domain.customer.entity.Customer;
import com.fidecode.beebite.domain.customer.repository.CustomerRepository;
import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetCustomerByPhoneUseCase {

    private final CustomerRepository customerRepository;

    @Inject
    public GetCustomerByPhoneUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse execute(UUID storeId, String phone) {
        Customer customer = customerRepository.findByPhoneAndStoreId(phone, storeId)
                .orElseThrow(() -> new com.fidecode.beebite.domain.customer.exception.CustomerNotFoundException(phone));
        return CustomerDtoMapper.toResponse(customer);
    }
}
