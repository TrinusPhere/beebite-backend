package com.fidecode.beebite.domain;

import com.fidecode.beebite.domain.auth.valueobject.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldHaveAllRequiredRoles() {
        assertNotNull(Role.valueOf("ADMIN"));
        assertNotNull(Role.valueOf("GERENTE"));
        assertNotNull(Role.valueOf("CAIXA"));
    }

    @Test
    void shouldHaveExactlyThreeRoles() {
        assertEquals(3, Role.values().length);
    }
}
