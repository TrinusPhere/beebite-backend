package com.fidecode.biteflow.application.cash;

import com.fidecode.biteflow.application.cash.dto.CashRegisterRequest;
import com.fidecode.biteflow.application.cash.dto.CashRegisterResponse;
import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import com.fidecode.biteflow.domain.cash.exception.CashRegisterNotFoundException;
import com.fidecode.biteflow.domain.cash.repository.CashRegisterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegister Use Cases")
class CashRegisterUseCasesTest {

    @Mock
    private CashRegisterRepository repository;

    @Test
    @DisplayName("CreateCashRegisterUseCase deve criar e retornar response")
    void shouldCreateCashRegister() {
        CreateCashRegisterUseCase useCase = new CreateCashRegisterUseCase(repository);
        UUID storeId = UUID.randomUUID();
        CashRegisterRequest req = new CashRegisterRequest("Caixa 01");

        when(repository.save(any(CashRegister.class))).thenAnswer(i -> {
            CashRegister r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        CashRegisterResponse res = useCase.execute(storeId, req);

        assertNotNull(res);
        assertEquals("Caixa 01", res.name());
        assertTrue(res.active());
        verify(repository).save(any(CashRegister.class));
    }

    @Test
    @DisplayName("GetCashRegisterUseCase deve lançar exceção se não encontrado")
    void shouldThrowIfNotFound() {
        GetCashRegisterUseCase useCase = new GetCashRegisterUseCase(repository);
        UUID storeId = UUID.randomUUID();
        UUID registerId = UUID.randomUUID();

        when(repository.findById(registerId)).thenReturn(Optional.empty());

        assertThrows(CashRegisterNotFoundException.class, () -> useCase.execute(storeId, registerId));
    }
}
