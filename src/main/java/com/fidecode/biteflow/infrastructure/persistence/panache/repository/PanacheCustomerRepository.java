package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.CustomerEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.CustomerMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheCustomerRepository implements CustomerRepository {

    @Inject CustomerPanacheRepo repo;
    @Inject EntityManager em;

    @Override
    public Optional<Customer> findById(UUID id) {
        return repo.findByIdOptional(id).map(CustomerMapper::toDomain);
    }

    @Override
    public List<Customer> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(CustomerMapper::toDomain).toList();
    }

    @Override
    public List<Customer> searchByStoreId(UUID storeId, String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return repo.find("storeId = ?1 and (lower(name) like ?2 or phone like ?2)", storeId, pattern)
            .list().stream().map(CustomerMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        CustomerEntity entity = CustomerMapper.toEntity(customer);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        return CustomerMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }
}
