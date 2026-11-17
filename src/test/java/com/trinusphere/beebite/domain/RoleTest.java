package com.trinusphere.beebite.domain;

import com.trinusphere.beebite.domain.auth.valueobject.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldHaveAllRequiredRoles() {
        assertNotNull(Role.valueOf("SUPER_ADMIN"));
        assertNotNull(Role.valueOf("ADMIN"));
        assertNotNull(Role.valueOf("GERENTE"));
        assertNotNull(Role.valueOf("CAIXA"));
    }

    @Test
    void shouldHaveExactlyFourRoles() {
        assertEquals(4, Role.values().length);
    }
}
