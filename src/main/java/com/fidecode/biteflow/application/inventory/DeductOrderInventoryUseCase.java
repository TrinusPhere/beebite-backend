package com.fidecode.biteflow.application.inventory;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductType;
import com.fidecode.biteflow.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.biteflow.domain.inventory.entity.StockMovement;
import com.fidecode.biteflow.domain.inventory.repository.StockMovementRepository;
import com.fidecode.biteflow.domain.inventory.valueobject.StockMovementType;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.inventory.exception.InsufficientStockException;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.exception.IngredientNotFoundException;

import java.math.BigDecimal;

/**
 * Executa a baixa de estoque gerada pela finalização de um pedido.
 *
 * Produtos PRODUCED consomem os ingredientes da ficha técnica.
 * Produtos RESALE com trackStock=true consomem o próprio estoque do produto.
 */
@ApplicationScoped
public class DeductOrderInventoryUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TransactionExecutor tx;

    @Inject
    public DeductOrderInventoryUseCase(ProductRepository productRepository,
                                       IngredientRepository ingredientRepository,
                                       StockMovementRepository stockMovementRepository,
                                       TransactionExecutor tx) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.tx = tx;
    }

    public void execute(OrderFinalizedEvent event) {
        tx.execute(() -> {
        for (var snapshot : event.getProducts()) {
            Product product = productRepository.findById(snapshot.productId())
.orElseThrow(() -> new ProductNotFoundException(snapshot.productId().toString()));
            StoreGuard.enforce(product, event.getStoreId());

            if (product.getProductType() == ProductType.RESALE) {
                deductResaleProduct(product, snapshot.quantity());
                continue;
            }

            deductTechnicalSheet(event, product, snapshot.quantity());
        }
        });
    }

    private void deductResaleProduct(Product product, int orderQuantity) {
        if (!product.isTrackStock()) return;
        if (product.getStock() < orderQuantity) throw new InsufficientStockException(product.getName());
        product.decreaseStock(orderQuantity);
        productRepository.save(product);
    }

    private void deductTechnicalSheet(OrderFinalizedEvent event, Product product, int orderQuantity) {
        if (product.getTechnicalSheet() == null || product.getTechnicalSheet().isEmpty()) return;

        for (var sheetItem : product.getTechnicalSheet()) {
            var ingredient = ingredientRepository.findById(sheetItem.getIngredientId())
.orElseThrow(() -> new IngredientNotFoundException(sheetItem.getIngredientId().toString()));
            StoreGuard.enforce(ingredient, event.getStoreId());

            BigDecimal quantity = sheetItem.getQuantity().multiply(BigDecimal.valueOf(orderQuantity));
            BigDecimal previous = ingredient.getStock();
            if (previous.compareTo(quantity) < 0) throw new InsufficientStockException(ingredient.getName());
            ingredient.decreaseStock(quantity);
            ingredientRepository.save(ingredient);

            StockMovement movement = StockMovement.create(
                event.getStoreId(),
                ingredient.getId(),
                StockMovementType.VENDA,
                quantity,
                previous,
                ingredient.getStock(),
                "Baixa automática por venda",
                "Pedido " + event.getOrderId()
            );
            movement.linkSource("ORDER", event.getOrderId());
            stockMovementRepository.save(movement);
        }
    }
}
