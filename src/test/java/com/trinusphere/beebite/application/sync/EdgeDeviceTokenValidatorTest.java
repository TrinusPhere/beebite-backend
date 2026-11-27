package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.sync.exception.InvalidEdgeDeviceTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdgeDeviceTokenValidatorTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void shouldAcceptValidBcryptAndPlainTokensAndRejectInvalidOnes() {
        EdgeDeviceTokenValidator validator = new EdgeDeviceTokenValidator(passwordEncoder);
        UUID storeId = UUID.randomUUID();

        Store bcryptStore = Store.reconstitute(
                storeId, "Balcão", null, true, null, null, "balcao",
                "EDGE", "$2a$10$validHash", null);
        when(passwordEncoder.matches("secret-123", "$2a$10$validHash")).thenReturn(true);
        when(passwordEncoder.matches("wrong", "$2a$10$validHash")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validate("secret-123", bcryptStore));
        assertThrows(InvalidEdgeDeviceTokenException.class, () -> validator.validate("wrong", bcryptStore));
        assertThrows(InvalidEdgeDeviceTokenException.class, () -> validator.validate("  ", bcryptStore));

        Store plainStore = Store.reconstitute(
                storeId, "Balcão", null, true, null, null, "balcao",
                "EDGE", "plain-edge-key", null);
        assertDoesNotThrow(() -> validator.validate("plain-edge-key", plainStore));
        assertThrows(InvalidEdgeDeviceTokenException.class, () -> validator.validate("other-key", plainStore));

        Store unconfiguredStore = Store.reconstitute(
                storeId, "Balcão", null, true, null, null, "balcao",
                "EDGE", null, null);
        assertThrows(InvalidEdgeDeviceTokenException.class, () -> validator.validate("secret-123", unconfiguredStore));
    }
}

