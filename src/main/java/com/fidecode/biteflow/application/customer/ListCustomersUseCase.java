package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.application.shared.PageResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;

    @Inject
    public ListCustomersUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public PageResponse<CustomerResponse> execute(UUID storeId, String search, int page, int size) {
        com.fidecode.biteflow.domain.shared.PageResult<Customer> result = (search != null && !search.isBlank())
            ? customerRepository.searchByStoreId(storeId, search, page, size)
            : customerRepository.findAllByStoreId(storeId, page, size);

        List<CustomerResponse> content = result.getContent().stream()
            .map(CustomerDtoMapper::toResponse).toList();
        
        return new PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
