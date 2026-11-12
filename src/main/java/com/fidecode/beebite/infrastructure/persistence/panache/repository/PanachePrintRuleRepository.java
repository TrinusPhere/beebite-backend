package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.printing.entity.PrintRule;
import com.fidecode.beebite.domain.printing.repository.PrintRuleRepository;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.PrintRuleMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class PanachePrintRuleRepository implements PrintRuleRepository {
    @Inject
    PrintRulePanacheRepo repo;

    public List<PrintRule> findAllByStoreId(UUID s) {
        return repo.find("storeId", s).list().stream().map(PrintRuleMapper::toDomain).toList();
    }

    @Transactional
    public PrintRule save(PrintRule r) {
        var e = PrintRuleMapper.toEntity(r);
        if (e.id == null)
            repo.persist(e);
        else
            e = repo.getEntityManager().merge(e);
        return PrintRuleMapper.toDomain(e);
    }
}
