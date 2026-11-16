package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.store.GetStoreInfoUseCase;
import com.trinusphere.beebite.application.store.UpdateStoreInfoUseCase;
import com.trinusphere.beebite.application.store.dto.StoreInfoRequest;
import com.trinusphere.beebite.application.store.dto.StoreInfoResponse;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreUseCaseTest {

    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    GetStoreInfoUseCase getStoreInfoUseCase;

    private final UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private Store existingStore;

    @BeforeEach
    void setUp() {
        existingStore = new Store("BiteFlow");
        existingStore.setId(storeId);
        existingStore.updateInfo(existingStore.getName(), existingStore.getLogoUrl(), true,
                existingStore.getOpeningHours(), "Rua A, 100");
    }

    @Test
    void shouldReturnStoreInfoResponse() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(existingStore));

        StoreInfoResponse response = getStoreInfoUseCase.execute(storeId);

        assertEquals("BiteFlow", response.name());
        assertTrue(response.isOpen());
        assertEquals("Rua A, 100", response.address());
    }

    @Test
    void shouldThrowWhenStoreNotFound() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> getStoreInfoUseCase.execute(storeId));
    }

    // ── UpdateStoreInfo tests ─────────────────────────

    @Test
    void shouldUpdateStoreAndReturnResponse() {
        UpdateStoreInfoUseCase updateStoreInfoUseCase = new UpdateStoreInfoUseCase(storeRepository);

        StoreInfoRequest request = new StoreInfoRequest("BiteFlow Premium", "https://logo.png", true, "10:00-22:00",
                "Rua B, 200");
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(existingStore));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreInfoResponse response = updateStoreInfoUseCase.execute(storeId, request);

        assertEquals("BiteFlow Premium", response.name());
        assertTrue(response.isOpen());
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void shouldUpdateStoreWithScheduleJsonAndPublicSlug() {
        UpdateStoreInfoUseCase updateStoreInfoUseCase = new UpdateStoreInfoUseCase(storeRepository);

        String longScheduleJson = "{\"autoSchedule\":false,\"schedule\":{\"MONDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":true},\"TUESDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":true},\"WEDNESDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":true},\"THURSDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":true},\"FRIDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":true},\"SATURDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":true},\"SUNDAY\":{\"start\":\"08:00\",\"end\":\"22:00\",\"active\":false}}}";
        StoreInfoRequest request = new StoreInfoRequest("00000000-0000-0000-0000-000000000001", "BiteFlow Lanchonete", null, true, longScheduleJson,
                "Rua da Lanchonete, 123 - Centro", "biteflow-lanchonete");
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(existingStore));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreInfoResponse response = updateStoreInfoUseCase.execute(storeId, request);

        assertEquals("BiteFlow Lanchonete", response.name());
        assertTrue(response.isOpen());
        assertEquals("biteflow-lanchonete", response.publicSlug());
        assertEquals(longScheduleJson, response.openingHours());
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentStore() {
        UpdateStoreInfoUseCase updateStoreInfoUseCase = new UpdateStoreInfoUseCase(storeRepository);

        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
                () -> updateStoreInfoUseCase.execute(storeId, new StoreInfoRequest("Nome", null, false, null, null)));
    }
}
