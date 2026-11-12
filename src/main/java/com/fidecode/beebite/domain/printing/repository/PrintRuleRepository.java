package com.fidecode.beebite.domain.printing.repository;

import com.fidecode.beebite.domain.printing.entity.PrintRule;
import java.util.*;

public interface PrintRuleRepository {
    List<PrintRule> findAllByStoreId(UUID storeId);

    PrintRule save(PrintRule r);
}
