package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Inject
    public DeleteCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void execute(UUID storeId, UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id.toString()));
        if (!customer.getStoreId().equals(storeId)) throw new ForbiddenAccessException("Acesso negado");
        customerRepository.deleteById(id);
    }
}
