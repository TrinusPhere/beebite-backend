package com.trinusphere.beebite.domain.customer.repository;

import com.trinusphere.beebite.domain.customer.entity.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port — interface do repositório de clientes.
 */
public interface CustomerRepository {

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByPhoneAndStoreId(String phone, UUID storeId);

    com.trinusphere.beebite.domain.shared.PageResult<Customer> findAllByStoreId(UUID storeId, int page, int size);

    com.trinusphere.beebite.domain.shared.PageResult<Customer> searchByStoreId(UUID storeId, String search, int page,
            int size);

    Customer save(Customer customer);

    void deleteById(UUID id);

    long countByStoreIdAndCreatedSince(UUID storeId, java.time.LocalDateTime since);

    long countByStoreIdAndCreatedBetween(UUID storeId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
