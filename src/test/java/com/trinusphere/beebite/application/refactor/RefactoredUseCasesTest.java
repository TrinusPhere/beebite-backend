package com.trinusphere.beebite.application.refactor;

import com.trinusphere.beebite.application.integration.*;
import com.trinusphere.beebite.application.integration.dto.*;
import com.trinusphere.beebite.application.messaging.*;
import com.trinusphere.beebite.application.messaging.dto.*;
import com.trinusphere.beebite.application.printing.*;
import com.trinusphere.beebite.application.printing.dto.*;
import com.trinusphere.beebite.application.production.*;
import com.trinusphere.beebite.application.production.dto.*;
import com.trinusphere.beebite.domain.integration.entity.IntegrationConfig;
import com.trinusphere.beebite.domain.integration.repository.IntegrationConfigRepository;
import com.trinusphere.beebite.domain.messaging.entity.Conversation;
import com.trinusphere.beebite.domain.messaging.repository.ConversationRepository;
import com.trinusphere.beebite.domain.printing.repository.PrinterRepository;
import com.trinusphere.beebite.domain.production.repository.ProductionSectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefactoredUseCasesTest {
    @Mock
    IntegrationConfigRepository integrationRepository;
    @Mock
    ProductionSectorRepository productionSectorRepository;
    @Mock
    ConversationRepository conversationRepository;
    @Mock
    PrinterRepository printerRepository;

    @Test
    void configureWhatsappPreservaTokenQuandoRequestNaoTrazNovoToken() {
        UUID storeId = UUID.randomUUID();
        IntegrationConfig config = new IntegrationConfig(storeId, "WHATSAPP");
        config.configureWhatsapp("META", "85999999999", "business", "token-atual");
        when(integrationRepository.findByStoreIdAndProvider(storeId, "WHATSAPP")).thenReturn(Optional.of(config));
        when(integrationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var useCase = new ConfigureWhatsappUseCase(integrationRepository);
        var response = useCase.execute(storeId,
                new WhatsappIntegrationRequest("META", "85888888888", "business2", "", null));

        assertTrue(response.tokenConfigured());
        assertEquals("85888888888", response.phoneNumber());
    }

    @Test
    void createProductionSectorAplicaCodigoEStatus() {
        UUID storeId = UUID.randomUUID();
        when(productionSectorRepository.save(any())).thenAnswer(i -> {
            var s = i.getArgument(0, com.trinusphere.beebite.domain.production.entity.ProductionSector.class);
            s.setId(UUID.randomUUID());
            return s;
        });
        var useCase = new CreateProductionSectorUseCase(productionSectorRepository);
        var response = useCase.execute(storeId, new ProductionSectorRequest("Cozinha", "KITCHEN", true));
        assertEquals("KITCHEN", response.code());
        assertTrue(response.active());
    }

    @Test
    void resolveConversationCriaQuandoNaoExiste() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(conversationRepository.findByStoreOrderChannel(storeId, orderId, "IFOOD")).thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenAnswer(i -> {
            var c = i.getArgument(0, Conversation.class);
            c.setId(UUID.randomUUID());
            return c;
        });
        var useCase = new ResolveConversationUseCase(conversationRepository);
        var response = useCase.execute(storeId,
                new ResolveConversationRequest(orderId.toString(), "IFOOD", "DELIVERY", "João", "85999999999"));
        assertEquals("IFOOD", response.channel());
        assertEquals(orderId.toString(), response.orderId());
    }

    @Test
    void deletePrinterValidaOwnershipAntesDeExcluir() {
        UUID storeId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        var printer = new com.trinusphere.beebite.domain.printing.entity.Printer(storeId, "Cozinha", "NETWORK");
        printer.setId(id);
        when(printerRepository.findById(id)).thenReturn(Optional.of(printer));
        var useCase = new DeletePrinterUseCase(printerRepository);
        useCase.execute(storeId, id);
        verify(printerRepository).deleteById(id);
    }
}
