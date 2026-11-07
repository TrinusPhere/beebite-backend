package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashSessionDtoMapper;
import com.fidecode.biteflow.application.cash.dto.CashSessionSummaryResponse;
import com.fidecode.biteflow.application.cash.dto.DriverCollectionSummaryDto;
import com.fidecode.biteflow.application.payment.dto.PaymentDtoMapper;
import com.fidecode.biteflow.application.payment.dto.PaymentResponse;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.cash.entity.CashMovement;
import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.exception.CashSessionNotFoundException;
import com.fidecode.biteflow.domain.cash.repository.CashMovementRepository;
import com.fidecode.biteflow.domain.cash.repository.CashRegisterRepository;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.MovementType;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.payment.entity.Payment;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import com.fidecode.biteflow.domain.payment.valueobject.PaymentCollector;
import com.fidecode.biteflow.domain.payment.valueobject.PaymentStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetCashSessionSummaryUseCase {

    private final CashSessionRepository sessionRepository;
    private final CashRegisterRepository registerRepository;
    private final UserRepository userRepository;
    private final CashMovementRepository movementRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryDriverRepository driverRepository;

    @Inject
    public GetCashSessionSummaryUseCase(CashSessionRepository sessionRepository,
                                        CashRegisterRepository registerRepository,
                                        UserRepository userRepository,
                                        CashMovementRepository movementRepository,
                                        PaymentRepository paymentRepository,
                                        DeliveryDriverRepository driverRepository) {
        this.sessionRepository = sessionRepository;
        this.registerRepository = registerRepository;
        this.userRepository = userRepository;
        this.movementRepository = movementRepository;
        this.paymentRepository = paymentRepository;
        this.driverRepository = driverRepository;
    }

    public CashSessionSummaryResponse execute(UUID storeId, UUID sessionId) {
        CashSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashSessionNotFoundException(sessionId.toString()));
        StoreGuard.enforce(session, storeId);

        String registerName = registerRepository.findById(session.getCashRegisterId())
                .map(CashRegister::getName).orElse("Caixa");

        String userName = userRepository.findById(session.getUserId())
                .map(User::getName).orElse("Operador");

        List<CashMovement> movements = movementRepository.findBySessionId(sessionId);
        List<Payment> payments = paymentRepository.findByCashSessionId(sessionId);

        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal cardSales = BigDecimal.ZERO;
        BigDecimal pixSales = BigDecimal.ZERO;
        BigDecimal otherSales = BigDecimal.ZERO;

        Map<UUID, List<Payment>> driverPayments = new HashMap<>();

        for (Payment p : payments) {
            if (p.getStatus() != PaymentStatus.CONFIRMED) continue;

            if (p.getMethod() == PaymentMethod.DINHEIRO) {
                cashSales = cashSales.add(p.getAmount());
            } else if (p.getMethod() == PaymentMethod.CREDITO || p.getMethod() == PaymentMethod.DEBITO) {
                cardSales = cardSales.add(p.getAmount());
            } else if (p.getMethod() == PaymentMethod.PIX) {
                pixSales = pixSales.add(p.getAmount());
            } else {
                otherSales = otherSales.add(p.getAmount());
            }

            if (p.getCollectedBy() == PaymentCollector.ENTREGADOR && p.getDriverId() != null) {
                driverPayments.computeIfAbsent(p.getDriverId(), k -> new ArrayList<>()).add(p);
            }
        }

        BigDecimal totalSales = cashSales.add(cardSales).add(pixSales).add(otherSales);

        BigDecimal suprimento = BigDecimal.ZERO;
        BigDecimal sangria = BigDecimal.ZERO;
        BigDecimal ajustes = BigDecimal.ZERO;

        for (CashMovement m : movements) {
            if (m.getType() == MovementType.SUPRIMENTO) {
                suprimento = suprimento.add(m.getAmount());
            } else if (m.getType() == MovementType.SANGRIA) {
                sangria = sangria.add(m.getAmount());
            } else if (m.getType() == MovementType.AJUSTE_COMPENSATORIO) {
                ajustes = ajustes.add(m.getAmount());
            }
        }

        BigDecimal calculatedCashBalance = session.getInitialAmount()
                .add(cashSales)
                .add(suprimento)
                .subtract(sangria)
                .add(ajustes);

        // Agrupamento por entregador para conferência
        List<DriverCollectionSummaryDto> driverSummaries = new ArrayList<>();
        for (Map.Entry<UUID, List<Payment>> entry : driverPayments.entrySet()) {
            UUID driverId = entry.getKey();
            List<Payment> list = entry.getValue();

            String driverName = driverRepository.findById(driverId)
                    .map(DeliveryDriver::getName).orElse("Entregador");

            BigDecimal dTotal = BigDecimal.ZERO;
            BigDecimal dCash = BigDecimal.ZERO;
            BigDecimal dPix = BigDecimal.ZERO;
            BigDecimal dCard = BigDecimal.ZERO;

            for (Payment dp : list) {
                dTotal = dTotal.add(dp.getAmount());
                if (dp.getMethod() == PaymentMethod.DINHEIRO) dCash = dCash.add(dp.getAmount());
                else if (dp.getMethod() == PaymentMethod.PIX) dPix = dPix.add(dp.getAmount());
                else dCard = dCard.add(dp.getAmount());
            }

            driverSummaries.add(new DriverCollectionSummaryDto(
                    driverId.toString(), driverName, dTotal, dCash, dPix, dCard, list.size()
            ));
        }

        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentDtoMapper::toResponse)
                .toList();

        return new CashSessionSummaryResponse(
                CashSessionDtoMapper.toResponse(session),
                registerName,
                userName,
                session.getInitialAmount(),
                cashSales,
                cardSales,
                pixSales,
                otherSales,
                totalSales,
                suprimento,
                sangria,
                ajustes,
                calculatedCashBalance,
                driverSummaries,
                paymentResponses
        );
    }
}
