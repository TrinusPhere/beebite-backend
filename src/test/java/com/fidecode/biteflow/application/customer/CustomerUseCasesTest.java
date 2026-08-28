package com.fidecode.biteflow.application.customer;

import com.fidecode.biteflow.application.customer.dto.CustomerRequest;
import com.fidecode.biteflow.application.customer.dto.CustomerResponse;
import com.fidecode.biteflow.application.shared.PageResponse;
import com.fidecode.biteflow.domain.customer.entity.Customer;
import com.fidecode.biteflow.domain.customer.exception.CustomerNotFoundException;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.shared.PageResult;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerUseCasesTest {

    @Mock
    private CustomerRepository customerRepository;

    private CreateCustomerUseCase createCustomerUseCase;
    private UpdateCustomerUseCase updateCustomerUseCase;
    private DeleteCustomerUseCase deleteCustomerUseCase;
    private ListCustomersUseCase listCustomersUseCase;
    private GetCustomerUseCase getCustomerUseCase;

    private final UUID STORE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createCustomerUseCase = new CreateCustomerUseCase(customerRepository);
        updateCustomerUseCase = new UpdateCustomerUseCase(customerRepository);
        deleteCustomerUseCase = new DeleteCustomerUseCase(customerRepository);
        listCustomersUseCase = new ListCustomersUseCase(customerRepository);
        getCustomerUseCase = new GetCustomerUseCase(customerRepository);
    }

    @Test
    void deveCriarCliente() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CustomerRequest request = new CustomerRequest("João Silva", "(11) 98888-7777", "joao@email.com", "01310-100",
                "Rua das Flores", "123", "Apto 42", "Centro");

        CustomerResponse response = createCustomerUseCase.execute(STORE_ID, request);

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

        CustomerRequest request = new CustomerRequest("João Atualizado", "(11) 91111-2222", null, null, null, null,
                null, null);

        CustomerResponse response = updateCustomerUseCase.execute(STORE_ID, customerId, request);
        assertEquals("João Atualizado", response.name());
        assertEquals("(11) 91111-2222", response.phone());
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> updateCustomerUseCase.execute(STORE_ID, id,
                new CustomerRequest("X", null, null, null, null, null, null, null)));
    }

    @Test
    void deveDeletarCliente() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(STORE_ID, "João");
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        deleteCustomerUseCase.execute(STORE_ID, id);
        verify(customerRepository).deleteById(id);
    }

    @Test
    void deveListarClientesComBusca() {
        Customer c1 = new Customer(STORE_ID, "João");
        c1.setId(UUID.randomUUID());

        PageResult<Customer> pageResult = new PageResult<>(List.of(c1), 0, 20, 1L, 1);
        when(customerRepository.searchByStoreId(eq(STORE_ID), eq("João"), eq(0), eq(20))).thenReturn(pageResult);

        PageResponse<CustomerResponse> result = listCustomersUseCase.execute(STORE_ID, "João", 0, 20);
        assertEquals(1, result.getContent().size());
        assertEquals("João", result.getContent().get(0).name());
    }

    @Test
    void deveListarTodosOsClientes() {
        Customer c1 = new Customer(STORE_ID, "João");
        c1.setId(UUID.randomUUID());
        Customer c2 = new Customer(STORE_ID, "Maria");
        c2.setId(UUID.randomUUID());

        PageResult<Customer> pageResult = new PageResult<>(List.of(c1, c2), 0, 20, 2L, 1);
        when(customerRepository.findAllByStoreId(eq(STORE_ID), eq(0), eq(20))).thenReturn(pageResult);

        PageResponse<CustomerResponse> result = listCustomersUseCase.execute(STORE_ID, null, 0, 20);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void deveBuscarClientePorId() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(STORE_ID, "João");
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        CustomerResponse response = getCustomerUseCase.execute(STORE_ID, id);
        assertEquals("João", response.name());
    }
}
