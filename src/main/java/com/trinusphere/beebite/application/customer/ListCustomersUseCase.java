package com.trinusphere.beebite.application.customer;

import com.trinusphere.beebite.application.customer.dto.CustomerResponse;
import com.trinusphere.beebite.application.shared.PageResponse;
import com.trinusphere.beebite.domain.customer.entity.Customer;
import com.trinusphere.beebite.domain.customer.repository.CustomerRepository;
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
        com.trinusphere.beebite.domain.shared.PageResult<Customer> result = (search != null && !search.isBlank())
                ? customerRepository.searchByStoreId(storeId, search, page, size)
                : customerRepository.findAllByStoreId(storeId, page, size);

        List<CustomerResponse> content = result.getContent().stream().map(CustomerDtoMapper::toResponse).toList();

        return new PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
    }
}
