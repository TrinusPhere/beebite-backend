package com.fidecode.beebite.application.inventory;

import com.fidecode.beebite.application.shared.port.TransactionExecutor;
import com.fidecode.beebite.domain.catalog.entity.Ingredient;
import com.fidecode.beebite.domain.catalog.entity.Product;
import com.fidecode.beebite.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.beebite.domain.catalog.repository.IngredientRepository;
import com.fidecode.beebite.domain.catalog.repository.ProductRepository;
import com.fidecode.beebite.domain.catalog.valueobject.ProductType;
import com.fidecode.beebite.domain.catalog.valueobject.Unit;
import com.fidecode.beebite.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.beebite.domain.inventory.entity.StockMovement;
import com.fidecode.beebite.domain.inventory.repository.StockMovementRepository;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeductOrderInventoryUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;

    private DeductOrderInventoryUseCase useCase;

    private final UUID STORE_ID = UUID.randomUUID();
    private final TransactionExecutor directTx = new TransactionExecutor() {
        @Override
        public <T> T execute(Supplier<T> action) {
            return action.get();
        }
    };

    @BeforeEach
    void setUp() {
        useCase = new DeductOrderInventoryUseCase(productRepository, ingredientRepository, stockMovementRepository, directTx);
    }

    @Test
    void deveDebitarIngredientesDeProdutoRevendaComFichaTecnica() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();

        Product resaleProduct = new Product(STORE_ID, "Coca-Cola Lata", UUID.randomUUID(), new BigDecimal("6.00"));
        resaleProduct.setId(productId);
        resaleProduct.configureInventory(ProductType.RESALE, false, null, Unit.UN);
        resaleProduct.replaceTechnicalSheet(List.of(
                new TechnicalSheetItem(productId, ingredientId, BigDecimal.ONE, new BigDecimal("2.50"))
        ));

        Ingredient ingredient = Ingredient.reconstitute(
                ingredientId, STORE_ID, "Refrigerante Coca-Cola Lata 350ml",
                new BigDecimal("50.00"), Unit.UN, new BigDecimal("2.50"), new BigDecimal("10.00"), true
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(resaleProduct));
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient));

        OrderFinalizedEvent event = new OrderFinalizedEvent(
                UUID.randomUUID(), STORE_ID, new BigDecimal("12.00"), PaymentMethod.PIX,
                null, null, List.of(new OrderFinalizedEvent.ProductSnapshot(productId, "Coca-Cola Lata", 2, new BigDecimal("6.00")))
        );

        useCase.execute(event);

        // Deve debitar 2 unidades do ingrediente vinculado (50 - 2 = 48)
        assertEquals(new BigDecimal("48.00"), ingredient.getStock());
        verify(ingredientRepository).save(ingredient);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void deveDebitarEstoqueDoProdutoSeRevendaNaoTiverFichaTecnica() {
        UUID productId = UUID.randomUUID();

        Product resaleProduct = new Product(STORE_ID, "Item Sem Ficha", UUID.randomUUID(), new BigDecimal("10.00"));
        resaleProduct.setId(productId);
        resaleProduct.configureInventory(ProductType.RESALE, true, null, Unit.UN);
        resaleProduct.initializeStock(20);

        when(productRepository.findById(productId)).thenReturn(Optional.of(resaleProduct));

        OrderFinalizedEvent event = new OrderFinalizedEvent(
                UUID.randomUUID(), STORE_ID, new BigDecimal("20.00"), PaymentMethod.PIX,
                null, null, List.of(new OrderFinalizedEvent.ProductSnapshot(productId, "Item Sem Ficha", 3, new BigDecimal("10.00")))
        );

        useCase.execute(event);

        assertEquals(17, resaleProduct.getStock());
        verify(productRepository).save(resaleProduct);
        verifyNoInteractions(ingredientRepository);
    }

    @Test
    void deveIgnorarItemSemProductIdAoFinalizarPedidoExterno() {
        OrderFinalizedEvent event = new OrderFinalizedEvent(
                UUID.randomUUID(), STORE_ID, new BigDecimal("15.00"), PaymentMethod.PIX,
                null, null, List.of(new OrderFinalizedEvent.ProductSnapshot(null, "Item iFood Sem Catalogo", 1, new BigDecimal("15.00")))
        );

        useCase.execute(event);

        verifyNoInteractions(productRepository);
        verifyNoInteractions(ingredientRepository);
        verifyNoInteractions(stockMovementRepository);
    }
}

