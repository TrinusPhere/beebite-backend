package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Produtos pertencem ao Cardápio ({@code Catalog}) compartilhado entre as lojas do cliente. Estoque, setor de
 * produção e pausa local pertencem à loja ({@code store_product_setting}) e são hidratados conforme o contexto da loja
 * informada nos métodos {@code *InStore}/{@code *ByStoreId}.
 */
public interface ProductRepository {
    /**
     * Busca sem contexto de loja: dados cadastrais do Cardápio, sem estado operacional local. Os produtos retornados
     * NÃO passam em {@code StoreGuard.enforce} (storeId = catalogId) — prefira {@link #findByIdInStore}.
     */
    Optional<Product> findById(UUID id);

    /**
     * Busca o produto no contexto da loja. Vazio se o produto não existe ou pertence a outro Cardápio (isolamento
     * multi-tenant). O produto retornado é hidratado com estoque, setor e pausa local da loja.
     */
    Optional<Product> findByIdInStore(UUID id, UUID storeId);

    com.trinusphere.beebite.domain.shared.PageResult<Product> findAllByStoreId(UUID storeId, int page, int size);

    com.trinusphere.beebite.domain.shared.PageResult<Product> findPendingCostByStoreId(UUID storeId, int page, int size);

    com.trinusphere.beebite.domain.shared.PageResult<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId,
            int page, int size);

    List<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId);

    List<Product> findAllByStoreId(UUID storeId);

    /** Busca por IDs sem contexto de loja (dados cadastrais apenas). */
    List<Product> findAllById(List<UUID> ids);

    /** Busca por IDs no contexto da loja (estoque/setor/pausa local hidratados; ignora IDs de outros Cardápios). */
    List<Product> findAllByIdInStore(List<UUID> ids, UUID storeId);

    /**
     * Persiste os dados cadastrais no Cardápio da loja e o estado operacional (estoque/setor/pausa local) da loja do
     * contexto do produto.
     */
    Product save(Product product);

    /** Equivalente a {@link #save(Product)} para o contexto explícito de {@code storeId}. */
    Product saveForStore(Product product, UUID storeId);

    /** Atualiza SOMENTE a pausa local da loja (não altera o Cardápio nem o iFood). */
    void updateStoreLocalStatus(UUID storeId, UUID productId, ProductStatus localStatus);

    void deleteById(UUID id);

    /**
     * Busca um produto pelo externalCode no Cardápio da loja, INCLUINDO soft-deleted. Usa SQL nativo para contornar o
     * {@code @SQLRestriction("deleted_at IS NULL")}.
     */
    Optional<Product> findByExternalCodeAndStoreId(String externalCode, UUID storeId);

    /** Idem, resolvido diretamente pelo Cardápio. */
    Optional<Product> findByExternalCodeAndCatalogId(String externalCode, UUID catalogId);
}
