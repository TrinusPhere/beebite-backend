package com.trinusphere.beebite.application.customer;

import com.trinusphere.beebite.application.customer.dto.CustomerResponse;
import com.trinusphere.beebite.domain.customer.entity.Customer;
import com.trinusphere.beebite.domain.customer.repository.CustomerRepository;
import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;
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
                .orElseThrow(() -> new com.trinusphere.beebite.domain.customer.exception.CustomerNotFoundException(phone));
        return CustomerDtoMapper.toResponse(customer);
    }
}
