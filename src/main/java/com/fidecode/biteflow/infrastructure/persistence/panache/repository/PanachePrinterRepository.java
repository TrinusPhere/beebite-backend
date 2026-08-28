package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.printing.entity.Printer;
import com.fidecode.biteflow.domain.printing.repository.PrinterRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.PrinterMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class PanachePrinterRepository implements PrinterRepository {
    @Inject
    PrinterPanacheRepo repo;

    public List<Printer> findAllByStoreId(UUID s) {
        return repo.find("storeId", s).list().stream().map(PrinterMapper::toDomain).toList();
    }

    public Optional<Printer> findById(UUID id) {
        return repo.findByIdOptional(id).map(PrinterMapper::toDomain);
    }

    @Transactional
    public Printer save(Printer p) {
        var e = PrinterMapper.toEntity(p);
        if (e.id == null)
            repo.persist(e);
        else
            e = repo.getEntityManager().merge(e);
        return PrinterMapper.toDomain(e);
    }

    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(e -> e.deletedAt = LocalDateTime.now());
    }
}
