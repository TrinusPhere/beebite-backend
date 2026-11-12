package com.fidecode.beebite.application.order;

import com.fidecode.beebite.application.order.dto.NfeResponse;
import com.fidecode.beebite.domain.order.exception.OrderNotFoundException;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class EmitOrderNfeUseCase {
    private final OrderRepository repo;

    @Inject
    public EmitOrderNfeUseCase(OrderRepository repo) {
        this.repo = repo;
    }

    public NfeResponse execute(UUID storeId, UUID id) {
        var order = StoreGuard.enforce(repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
                storeId);
        order.markAsNfeEmitted();
        repo.save(order);
        return new NfeResponse(id.toString(), String.format("%09d", new Random().nextInt(999999999)),
                "35260812345678000195550010001234561234567890",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "AUTORIZADA");
    }
}
