package com.fidecode.biteflow.application.kds;

import com.fidecode.biteflow.application.kds.dto.KdsStationRequest;
import com.fidecode.biteflow.application.kds.dto.KdsStationResponse;
import com.fidecode.biteflow.domain.kds.entity.KdsStation;
import com.fidecode.biteflow.domain.kds.exception.KdsStationNotFoundException;
import com.fidecode.biteflow.domain.kds.repository.KdsStationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KdsStation Use Cases")
class KdsStationUseCasesTest {

    @Mock
    private KdsStationRepository stationRepository;

    @Test
    @DisplayName("CreateKdsStationUseCase deve salvar e retornar DTO")
    void shouldCreateStation() {
        CreateKdsStationUseCase useCase = new CreateKdsStationUseCase(stationRepository);
        UUID storeId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();
        KdsStationRequest req = new KdsStationRequest("Chapa", false, List.of(sectorId.toString()));

        when(stationRepository.save(any(KdsStation.class))).thenAnswer(invocation -> {
            KdsStation s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        KdsStationResponse res = useCase.execute(storeId, req);

        assertNotNull(res);
        assertEquals("Chapa", res.name());
        assertFalse(res.isExpedition());
        verify(stationRepository).save(any(KdsStation.class));
    }

    @Test
    @DisplayName("UpdateKdsStationUseCase deve atualizar estação existente")
    void shouldUpdateStation() {
        UpdateKdsStationUseCase useCase = new UpdateKdsStationUseCase(stationRepository);
        UUID storeId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        KdsStation station = new KdsStation(storeId, "Chapa Antiga", false, List.of());
        station.setId(stationId);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(stationRepository.save(any(KdsStation.class))).thenAnswer(i -> i.getArgument(0));

        KdsStationRequest req = new KdsStationRequest("Chapa Nova", true, List.of());
        KdsStationResponse res = useCase.execute(storeId, stationId, req);

        assertNotNull(res);
        assertEquals("Chapa Nova", res.name());
        assertTrue(res.isExpedition());
    }

    @Test
    @DisplayName("GetKdsStationUseCase deve lançar exceção se não encontrada")
    void shouldThrowIfStationNotFound() {
        GetKdsStationUseCase useCase = new GetKdsStationUseCase(stationRepository);
        UUID storeId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThrows(KdsStationNotFoundException.class, () -> useCase.execute(storeId, stationId));
    }

    @Test
    @DisplayName("DeleteKdsStationUseCase deve realizar soft delete")
    void shouldSoftDeleteStation() {
        DeleteKdsStationUseCase useCase = new DeleteKdsStationUseCase(stationRepository);
        UUID storeId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        KdsStation station = new KdsStation(storeId, "Chapa", false, List.of());
        station.setId(stationId);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        useCase.execute(storeId, stationId);

        verify(stationRepository).save(argThat(s -> s.getDeletedAt() != null));
    }
}

