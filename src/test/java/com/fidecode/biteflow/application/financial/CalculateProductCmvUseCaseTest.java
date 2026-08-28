package com.fidecode.biteflow.application.financial;

import com.fidecode.biteflow.application.financial.dto.ProductCmvResponse;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateProductCmvUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private CalculateProductCmvUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveCalcularCmvDoProdutoViaFichaTecnica() {
        // Product price = 35.00
        // Ingredients: Pão (qty=1, cost=1.50), Carne (qty=2, cost=3.00)
        // Production cost = (1 * 1.50) + (2 * 3.00) = 1.50 + 6.00 = 7.50
        // Contribution margin = 35.00 - 7.50 = 27.50
        // CMV% = (7.50 / 35.00) * 100 = 21.43%
        UUID productId = UUID.randomUUID();
        UUID ingredientPaoId = UUID.randomUUID();
        UUID ingredientCarneId = UUID.randomUUID();

        var pao = new Ingredient(STORE_ID, "Pão Brioche", BigDecimal.TEN, Unit.UN, new BigDecimal("1.50"),
                BigDecimal.ZERO);
        pao.setId(ingredientPaoId);

        var carne = new Ingredient(STORE_ID, "Carne Bovina", BigDecimal.TEN, Unit.KG, new BigDecimal("3.00"),
                BigDecimal.ZERO);
        carne.setId(ingredientCarneId);

        var product = new Product(STORE_ID, "X-Bacon Especial", null, new BigDecimal("35.00"));
        product.setId(productId);
        product.replaceTechnicalSheet(List.of(
                new TechnicalSheetItem(productId, ingredientPaoId, BigDecimal.ONE, new BigDecimal("1.50")),
                new TechnicalSheetItem(productId, ingredientCarneId, new BigDecimal("2"), new BigDecimal("3.00"))));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(ingredientRepository.findAllById(List.of(ingredientPaoId, ingredientCarneId)))
                .thenReturn(List.of(pao, carne));

        ProductCmvResponse response = useCase.execute(STORE_ID, productId);

        assertEquals(productId, response.productId());
        assertEquals("X-Bacon Especial", response.productName());
        assertEquals(new BigDecimal("35.00"), response.sellingPrice());
        assertEquals(new BigDecimal("7.50"), response.productionCost());
        assertEquals(new BigDecimal("27.50"), response.contributionMargin());
        assertEquals(new BigDecimal("21.43"), response.cmvPercentage());
        assertEquals("GREEN", response.alertLevel());
        assertEquals(2, response.ingredients().size());
    }
}
