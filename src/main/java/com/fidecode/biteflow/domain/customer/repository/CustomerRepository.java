package com.fidecode.biteflow.domain.customer.repository;

import com.fidecode.biteflow.domain.customer.entity.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port — interface do repositório de clientes.
 */
public interface CustomerRepository {

    Optional<Customer> findById(UUID id);

    com.fidecode.biteflow.domain.shared.PageResult<Customer> findAllByStoreId(UUID storeId, int page, int size);

    com.fidecode.biteflow.domain.shared.PageResult<Customer> searchByStoreId(UUID storeId, String search, int page,
            int size);

    Customer save(Customer customer);

    void deleteById(UUID id);

    long countByStoreIdAndCreatedSince(UUID storeId, java.time.LocalDateTime since);
}
