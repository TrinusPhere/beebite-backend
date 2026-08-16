package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CustomerUseCases {

    private final CustomerRepository customerRepository;

    @Inject
    public CustomerUseCases(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse create(UUID storeId, CustomerRequest request) {
        Customer customer = new Customer(storeId, request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setZipCode(request.zipCode());
        customer.setAddress(request.address());
        customer.setNumber(request.number());
        customer.setComplement(request.complement());
        customer.setNeighborhood(request.neighborhood());

        Customer saved = customerRepository.save(customer);
        return toResponse(saved);
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id.toString()));

        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setZipCode(request.zipCode());
        customer.setAddress(request.address());
        customer.setNumber(request.number());
        customer.setComplement(request.complement());
        customer.setNeighborhood(request.neighborhood());

        return toResponse(customerRepository.save(customer));
    }

    public void delete(UUID id) {
        customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id.toString()));
        customerRepository.deleteById(id);
    }

    public CustomerResponse getById(UUID id) {
        return customerRepository.findById(id)
            .map(CustomerUseCases::toResponse)
            .orElseThrow(() -> new CustomerNotFoundException(id.toString()));
    }

    public List<CustomerResponse> list(UUID storeId, String search) {
        List<Customer> customers = (search != null && !search.isBlank())
            ? customerRepository.searchByStoreId(storeId, search)
            : customerRepository.findAllByStoreId(storeId);
        return customers.stream().map(CustomerUseCases::toResponse).toList();
    }

    public static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
            c.getId() != null ? c.getId().toString() : null,
            c.getName(),
            c.getPhone(),
            c.getEmail(),
            c.getZipCode(),
            c.getAddress(),
            c.getNumber(),
            c.getComplement(),
            c.getNeighborhood(),
            c.getTotalOrders(),
            null // createdAt will be set from entity
        );
    }
}
