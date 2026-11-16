package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.ProductionTaskResponse;
import com.trinusphere.beebite.application.kds.port.KdsEventPublisher;
import com.trinusphere.beebite.domain.kds.entity.KdsStation;
import com.trinusphere.beebite.domain.kds.entity.ProductionTask;
import com.trinusphere.beebite.domain.kds.repository.KdsStationRepository;
import com.trinusphere.beebite.domain.kds.repository.ProductionTaskRepository;
import com.trinusphere.beebite.domain.kds.valueobject.TaskStatus;
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
@DisplayName("ProductionTask Actions & Queries Use Cases")
class ProductionTaskActionUseCasesTest {

    @Mock
    private ProductionTaskRepository taskRepository;

    @Mock
    private KdsStationRepository stationRepository;

    @Mock
    private KdsEventPublisher eventPublisher;

    @Test
    @DisplayName("StartProductionTaskUseCase deve mudar status para IN_PREPARATION")
    void shouldStartTask() {
        StartProductionTaskUseCase useCase = new StartProductionTaskUseCase(taskRepository, eventPublisher);
        UUID storeId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "#101", "DELIVERY", "PDV", "Cliente A", "Smash", 1, null, null, null
        );
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(ProductionTask.class))).thenAnswer(i -> i.getArgument(0));

        ProductionTaskResponse res = useCase.execute(storeId, taskId);

        assertEquals("IN_PREPARATION", res.status());
        assertNotNull(res.startedAt());
        verify(eventPublisher).publishTaskUpdated(eq(storeId), any());
    }

    @Test
    @DisplayName("CompleteProductionTaskUseCase deve mudar status para READY (sai da tela ativa)")
    void shouldCompleteTask() {
        CompleteProductionTaskUseCase useCase = new CompleteProductionTaskUseCase(taskRepository, eventPublisher);
        UUID storeId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "#102", "DELIVERY", "PDV", "Cliente B", "Smash", 1, null, null, null
        );
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(ProductionTask.class))).thenAnswer(i -> i.getArgument(0));

        ProductionTaskResponse res = useCase.execute(storeId, taskId);

        assertEquals("READY", res.status());
        assertNotNull(res.completedAt());
        verify(eventPublisher).publishTaskUpdated(eq(storeId), any());
    }

    @Test
    @DisplayName("ListActiveProductionTasksUseCase deve filtrar por setores da estação")
    void shouldListActiveTasksForStation() {
        ListActiveProductionTasksUseCase useCase = new ListActiveProductionTasksUseCase(taskRepository, stationRepository);
        UUID storeId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        KdsStation station = new KdsStation(storeId, "Chapa", false, List.of(sectorId));
        station.setId(stationId);

        ProductionTask task = new ProductionTask(
                storeId, UUID.randomUUID(), UUID.randomUUID(), sectorId,
                "#103", "DELIVERY", "PDV", "Cliente C", "Smash", 1, null, null, null
        );
        task.setId(UUID.randomUUID());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(taskRepository.findActiveTasks(eq(storeId), eq(List.of(sectorId)), eq(false)))
                .thenReturn(List.of(task));

        List<ProductionTaskResponse> res = useCase.execute(storeId, stationId);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("Smash", res.get(0).itemName());
    }
}

