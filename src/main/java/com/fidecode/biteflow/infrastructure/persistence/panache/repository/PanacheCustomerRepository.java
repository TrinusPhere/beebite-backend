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
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Customer> findAllByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Customer> content = query.list().stream().map(CustomerMapper::toDomain).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Customer> searchByStoreId(UUID storeId, String search, int page, int size) {
        String like = "%" + search.toLowerCase() + "%";
        var query = repo.find("storeId = ?1 and (lower(name) like ?2 or phone like ?2 or lower(email) like ?2)", storeId, like)
            .page(io.quarkus.panache.common.Page.of(page, size));
        List<Customer> content = query.list().stream().map(CustomerMapper::toDomain).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        CustomerEntity entity = CustomerMapper.toEntity(customer);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            // Preservar timestamps que pertencem à infra e não ao domínio
            CustomerEntity existing = repo.findById(entity.id);
            if (existing != null) {
                entity.createdAt = existing.createdAt;
            }
            entity = em.merge(entity);
        }
        return CustomerMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            em.merge(entity);
        });
    }
}
