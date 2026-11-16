package com.trinusphere.beebite.application.delivery;

import com.trinusphere.beebite.application.delivery.dto.SettlementReportResponse;
import com.trinusphere.beebite.application.delivery.dto.SettlementReportResponse.SettlementEntry;
import com.trinusphere.beebite.application.delivery.dto.SettlementReportResponse.SettlementOrderDto;
import com.trinusphere.beebite.domain.delivery.entity.DeliveryDriver;
import com.trinusphere.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GetSettlementReportUseCase {

    private final DeliveryDriverRepository driverRepository;
    private final OrderRepository orderRepository;

    @Inject
    public GetSettlementReportUseCase(DeliveryDriverRepository driverRepository, OrderRepository orderRepository) {
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
    }

    public SettlementReportResponse execute(UUID storeId, String period, String dateStr) {
        LocalDateTime start;
        LocalDateTime end;

        if (dateStr != null && !dateStr.isBlank()) {
            LocalDate date = LocalDate.parse(dateStr);
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59, 999999999);
        } else if ("weekly".equalsIgnoreCase(period)) {
            start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            end = LocalDate.now().atTime(23, 59, 59, 999999999);
        } else if ("monthly".equalsIgnoreCase(period)) {
            start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            end = LocalDate.now().atTime(23, 59, 59, 999999999);
        } else {
            // default: daily
            start = LocalDate.now().atStartOfDay();
            end = LocalDate.now().atTime(23, 59, 59, 999999999);
        }

        List<DeliveryDriver> drivers = driverRepository.findAllByStoreId(storeId, 0, 1000).getContent();
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);

        List<SettlementEntry> entries = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (DeliveryDriver driver : drivers) {
            String driverType = driver.isFixed() ? "FIXO" : (driver.isDaily() ? "DIARISTA" : "AVULSO");
            BigDecimal allowance = driver.getAllowance() != null ? driver.getAllowance() : BigDecimal.ZERO;
            BigDecimal feePerDelivery = driver.isHasFixedDeliveryValue() && driver.getFixedDeliveryValue() != null
                    ? driver.getFixedDeliveryValue()
                    : BigDecimal.ZERO;

            List<Order> driverOrders = orders.stream()
                    .filter(o -> o.getDriverId() != null && o.getDriverId().equals(driver.getId()))
                    .filter(o -> o.getStatus() != OrderStatus.CANCELADO)
                    .toList();

            int deliveries = driverOrders.size();
            BigDecimal total = feePerDelivery.multiply(BigDecimal.valueOf(deliveries)).add(allowance);
            grandTotal = grandTotal.add(total);

            List<SettlementOrderDto> orderDtos = driverOrders.stream()
                    .map(this::toOrderDto)
                    .toList();

            entries.add(new SettlementEntry(
                    driver.getId().toString(),
                    driver.getName(),
                    driverType,
                    deliveries,
                    allowance,
                    feePerDelivery,
                    total,
                    orderDtos
            ));
        }

        return new SettlementReportResponse(
                period != null ? period.toLowerCase() : "daily",
                entries,
                grandTotal
        );
    }

    private SettlementOrderDto toOrderDto(Order order) {
        Long displayIdNum = null;
        if (order.getDisplayId() != null) {
            try {
                displayIdNum = Long.parseLong(order.getDisplayId().replaceAll("\\D", ""));
            } catch (Exception ignored) {
                // Ignore if displayId is non-numeric
            }
        }

        return new SettlementOrderDto(
                order.getId() != null ? order.getId().toString() : null,
                displayIdNum,
                order.getCustomerName(),
                order.getCustomerAddress(),
                order.getTotalValue(),
                order.getDeliveryCost(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
                order.getCreatedAt()
        );
    }
}
