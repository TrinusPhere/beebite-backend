package com.fidecode.biteflow.domain.customer;

import com.fidecode.biteflow.domain.customer.entity.Customer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveCriarClienteComNome() {
        Customer customer = new Customer(STORE_ID, "João Silva");

        assertEquals(STORE_ID, customer.getStoreId());
        assertEquals("João Silva", customer.getName());
        assertEquals(0, customer.getTotalOrders());
    }

    @Test
    void deveLancarExcecaoSeStoreIdNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(null, "João"));
    }

    @Test
    void deveLancarExcecaoSeNomeVazio() {
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(STORE_ID, ""));
    }

    @Test
    void deveLancarExcecaoSeNomeNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Customer(STORE_ID, null));
    }

    @Test
    void deveIncrementarTotalOrders() {
        Customer customer = new Customer(STORE_ID, "João");
        assertEquals(0, customer.getTotalOrders());

        customer.incrementTotalOrders();
        assertEquals(1, customer.getTotalOrders());

        customer.incrementTotalOrders();
        assertEquals(2, customer.getTotalOrders());
    }

    @Test
    void deveSetarCamposOpcionais() {
        Customer customer = new Customer(STORE_ID, "João");
        customer.setPhone("(11) 98888-7777");
        customer.setEmail("joao@email.com");
        customer.setZipCode("01310-100");
        customer.setAddress("Rua das Flores");
        customer.setNumber("123");
        customer.setComplement("Apto 42");
        customer.setNeighborhood("Centro");

        assertEquals("(11) 98888-7777", customer.getPhone());
        assertEquals("joao@email.com", customer.getEmail());
        assertEquals("01310-100", customer.getZipCode());
        assertEquals("Rua das Flores", customer.getAddress());
        assertEquals("123", customer.getNumber());
        assertEquals("Apto 42", customer.getComplement());
        assertEquals("Centro", customer.getNeighborhood());
    }
}
