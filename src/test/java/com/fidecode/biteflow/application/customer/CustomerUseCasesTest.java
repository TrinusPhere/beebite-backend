package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
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
class CustomerUseCasesTest {

    @Mock private CustomerRepository customerRepository;

    private CustomerUseCases useCases;
    private final UUID STORE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCases = new CustomerUseCases(customerRepository);
    }

    @Test
    void deveCriarCliente() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CustomerRequest request = new CustomerRequest(
            "João Silva", "(11) 98888-7777", "joao@email.com",
            "01310-100", "Rua das Flores", "123", "Apto 42", "Centro"
        );

        CustomerResponse response = useCases.create(STORE_ID, request);

        assertNotNull(response.id());
        assertEquals("João Silva", response.name());
        assertEquals("(11) 98888-7777", response.phone());
        assertEquals("Centro", response.neighborhood());
    }

    @Test
    void deveAtualizarCliente() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(STORE_ID, "João");
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerRequest request = new CustomerRequest(
            "João Atualizado", "(11) 91111-2222", null,
            null, null, null, null, null
        );

        CustomerResponse response = useCases.update(customerId, request);
        assertEquals("João Atualizado", response.name());
        assertEquals("(11) 91111-2222", response.phone());
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
            () -> useCases.update(id, new CustomerRequest("X", null, null, null, null, null, null, null)));
    }

    @Test
    void deveDeletarCliente() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(STORE_ID, "João");
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        useCases.delete(id);
        verify(customerRepository).deleteById(id);
    }

    @Test
    void deveListarClientesComBusca() {
        Customer c1 = new Customer(STORE_ID, "João");
        c1.setId(UUID.randomUUID());

        when(customerRepository.searchByStoreId(STORE_ID, "João")).thenReturn(List.of(c1));

        List<CustomerResponse> result = useCases.list(STORE_ID, "João");
        assertEquals(1, result.size());
        assertEquals("João", result.get(0).name());
    }

    @Test
    void deveListarTodosOsClientes() {
        Customer c1 = new Customer(STORE_ID, "João");
        c1.setId(UUID.randomUUID());
        Customer c2 = new Customer(STORE_ID, "Maria");
        c2.setId(UUID.randomUUID());

        when(customerRepository.findAllByStoreId(STORE_ID)).thenReturn(List.of(c1, c2));

        List<CustomerResponse> result = useCases.list(STORE_ID, null);
        assertEquals(2, result.size());
    }

    @Test
    void deveBuscarClientePorId() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(STORE_ID, "João");
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        CustomerResponse response = useCases.getById(id);
        assertEquals("João", response.name());
    }
}
