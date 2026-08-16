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

    List<Customer> findAllByStoreId(UUID storeId);

    List<Customer> searchByStoreId(UUID storeId, String search);

    Customer save(Customer customer);

    void deleteById(UUID id);
}
