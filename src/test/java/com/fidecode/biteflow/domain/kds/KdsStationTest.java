package com.fidecode.biteflow.domain.kds;

import com.fidecode.biteflow.domain.kds.entity.KdsStation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KdsStation — Regras de Domínio")
class KdsStationTest {

    @Test
    @DisplayName("Deve criar estação KDS ativa com setores associados")
    void shouldCreateActiveKdsStation() {
        UUID storeId = UUID.randomUUID();
        UUID sector1 = UUID.randomUUID();
        UUID sector2 = UUID.randomUUID();

        KdsStation station = new KdsStation(storeId, "Chapa & Frituras", false, List.of(sector1, sector2));

        assertNotNull(station);
        assertEquals(storeId, station.getStoreId());
        assertEquals("Chapa & Frituras", station.getName());
        assertFalse(station.isExpedition());
        assertTrue(station.isActive());
        assertEquals(2, station.getSectorIds().size());
        assertTrue(station.getSectorIds().contains(sector1));
        assertTrue(station.getSectorIds().contains(sector2));
    }

    @Test
    @DisplayName("Deve atualizar dados da estação")
    void shouldUpdateKdsStation() {
        UUID storeId = UUID.randomUUID();
        KdsStation station = new KdsStation(storeId, "Chapa", false, List.of());

        UUID newSector = UUID.randomUUID();
        station.update("Chapa e Grelhados", true, List.of(newSector));

        assertEquals("Chapa e Grelhados", station.getName());
        assertTrue(station.isExpedition());
        assertEquals(1, station.getSectorIds().size());
    }

    @Test
    @DisplayName("Deve ativar e desativar estação")
    void shouldToggleStationActive() {
        UUID storeId = UUID.randomUUID();
        KdsStation station = new KdsStation(storeId, "Bebidas", false, List.of());

        assertTrue(station.isActive());
        station.deactivate();
        assertFalse(station.isActive());
        station.activate();
        assertTrue(station.isActive());
    }

    @Test
    @DisplayName("Deve validar que o nome não pode ser vazio")
    void shouldValidateNameNotEmpty() {
        UUID storeId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> new KdsStation(storeId, "", false, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new KdsStation(storeId, "   ", false, List.of()));
    }

    @Test
    @DisplayName("Deve reconstituir estação com estado preservado")
    void shouldReconstituteKdsStation() {
        UUID id = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        KdsStation station = KdsStation.reconstitute(
                id, storeId, "Expedição", true, true, List.of(sectorId), now, now, null
        );

        assertEquals(id, station.getId());
        assertEquals(storeId, station.getStoreId());
        assertEquals("Expedição", station.getName());
        assertTrue(station.isExpedition());
        assertTrue(station.isActive());
        assertEquals(1, station.getSectorIds().size());
        assertEquals(now, station.getCreatedAt());
    }
}

