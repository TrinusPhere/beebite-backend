package com.trinusphere.beebite.domain.printing.repository;

import com.trinusphere.beebite.domain.printing.entity.Printer;
import java.util.*;

public interface PrinterRepository {
    List<Printer> findAllByStoreId(UUID storeId);

    Optional<Printer> findById(UUID id);

    Printer save(Printer p);

    void deleteById(UUID id);
}
