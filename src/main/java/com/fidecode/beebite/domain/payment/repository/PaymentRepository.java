package com.fidecode.beebite.domain.payment.repository;

import com.fidecode.beebite.domain.payment.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
    List<Payment> findByOrderId(UUID orderId);
    List<Payment> findByCashSessionId(UUID cashSessionId);
    List<Payment> findByDriverIdAndSessionId(UUID driverId, UUID cashSessionId);
    List<Payment> findByStoreIdAndPeriod(UUID storeId, LocalDateTime start, LocalDateTime end);
}
