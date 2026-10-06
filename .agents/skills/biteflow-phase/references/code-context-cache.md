# BiteFlow — Contexto do Código (Cache de Conhecimento)

> **Objetivo**: Evitar re-leitura de arquivos já analisados. Este documento captura
> a estrutura, assinaturas e comportamentos verificados do código existente.
> Última atualização: Fase 5 completa.

---

## 1. Estrutura de Pacotes (verificada)

```
com.fidecode.biteflow/
├── domain/
│   ├── auth/          (User, Role, Permission, UserRepository)
│   ├── catalog/       (Product, Ingredient, Category, ModifierGroup, TechnicalSheetItem)
│   ├── order/         (Order, OrderItem, OrderDomainService, OrderRepository)
│   ├── customer/      (Customer, CustomerRepository)
│   ├── delivery/      (DeliveryDriver, DeliveryDriverRepository)
│   ├── financial/     (FinancialRecord, FinancialRecordProduct, FinancialStatus, CmvAlertLevel)
│   ├── store/         (Store, StoreRepository)
│   └── shared/        (DomainException hierarchy, StoreGuard)
├── application/
│   ├── auth/          (LoginUseCase, CreateUserUseCase, etc.)
│   ├── catalog/       (CRUD individual UCs por entidade)
│   ├── order/         (CreateOrderUC, ManageOrderUC, UpdateOrderUC, etc.)
│   ├── customer/      (CRUD individual UCs)
│   ├── delivery/      (CRUD individual UCs)
│   ├── financial/     (Create, GetSummary, GetByProduct, PaymentMethods, List, CMV UCs)
│   ├── dashboard/     (GetDashboardUseCase)
│   └── store/         (GetStoreInfoUC, UpdateStoreInfoUC)
├── infrastructure/
│   ├── persistence/panache/
│   │   ├── entity/    (JPA Entities: *Entity.java)
│   │   ├── repository/ (Dual-class: *PanacheRepo + Panache*Repository)
│   │   ├── mapper/    (Domain ↔ Entity mappers)
│   │   └── converter/ (StringListConverter)
│   ├── security/      (JwtTokenProvider, PasswordHasher, JwtPermissionsAugmentor)
│   └── event/         (OrderFinalizedObserver)
└── presentation/
    ├── rest/           (*Resource extends BaseResource)
    └── exception/      (GlobalExceptionMapper, ErrorResponse)
```

---

## 2. Assinaturas Críticas (já verificadas — não re-ler)

### Domain — Order
- `Order.getStatus()` → `OrderStatus`
- `Order.changeStatus(OrderStatus)` → void (muta o objeto)
- `Order.assignDriver(UUID driverId, String driverName)` → void
- `Order.getCustomerId()` → `UUID` (nullable)
- `Order.getDriverId()` → `UUID` (nullable)
- `Order.getItems()` → `List<OrderItem>`
- `Order.getTotalValue()` → `BigDecimal`
- `Order.getPaymentMethod()` → `PaymentMethod`

### Domain — OrderItem
- `OrderItem.getProductId()` → `UUID`
- `OrderItem.getProductName()` → `String`
- `OrderItem.getQuantity()` → `int`
- `OrderItem.getUnitPrice()` → `BigDecimal`

### Domain — Product
- `Product.calculateTotalCost()` → `BigDecimal` (soma dos `TechnicalSheetItem.getLineCost()`)
- `Product.getMargin()` → `BigDecimal` (price - totalCost)
- `Product.getTechnicalSheet()` → `List<TechnicalSheetItem>`

### Domain — Customer
- `Customer.incrementTotalOrders()` → void

### Domain — DeliveryDriver
- `DeliveryDriver.incrementDeliveries()` → void

### Domain — FinancialRecord
- `FinancialRecord(storeId, orderId, totalValue, paymentMethod)` — construtor
- `FinancialRecord.reconstitute(...)` — factory para reconstrução do banco
- `FinancialRecord.getStoreId()` → `UUID`
- Implementa `StoreGuard.StoreScoped`

### Application — Repositories (Ports)
- `OrderRepository.findActiveKanbanOrdersByStoreId(UUID, LocalDateTime)` → `List<Order>`
- `OrderRepository.findAllById(List<UUID>)` → `List<Order>`
- `ProductRepository.findAllById(List<UUID>)` → `List<Product>` ⚠️ NÃO usar findById em batch
- `CustomerRepository.findAllByStoreId(UUID, int, int)` → PagedResult
- `DeliveryDriverRepository.findAllByStoreId(UUID, int, int)` → PagedResult
- `FinancialRecordRepository.findByStoreIdAndDateRange(UUID, LocalDate, LocalDate)` → `List<FinancialRecord>`

### Infrastructure — Padrão Dual-Class
```
*PanacheRepo implements PanacheRepositoryBase<Entity, UUID>  ← helper Panache puro
Panache*Repository implements DomainRepository               ← implementa port, injeta helper
```

### Presentation — BaseResource
```java
public abstract class BaseResource {
    @Inject protected JsonWebToken jwt;

    protected UUID getStoreId() {
        Object raw = jwt.getClaim("storeId");    // ⚠️ retorna Object, NÃO String
        String storeIdClaim = raw.toString().replace("\"", "");
        return UUID.fromString(storeIdClaim);
    }
}
```

### Security — JwtPermissionsAugmentor
- `jwt.getClaim("permissions")` retorna `List<?>` (pode ser `List<JsonString>`)
- ADMIN recebe todas as permissões via `Permission.allValues()`
- Demais roles recebem apenas do claim JWT

### Security — Permission enum
```
DASHBOARD, PEDIDOS, CLIENTES, INGREDIENTES, PRODUTOS,
ENTREGADORES, ACERTO_ENTREGADORES, USUARIOS, INTEGRACOES, FINANCEIRO
```

---

## 3. Convenções de Segurança por Resource (P10)

| Resource | `@RolesAllowed` (classe) | `@PermissionsAllowed` (método) |
|----------|--------------------------|-------------------------------|
| StoreResource | ADMIN | — |
| AuthResource | — (público) | — |
| UserResource | ADMIN | `usuarios` |
| FinancialResource | ADMIN, GERENTE | `financeiro` |
| CmvResource | ADMIN, GERENTE | `financeiro` |
| DashboardResource | ADMIN, GERENTE, CAIXA | `dashboard` |
| OrderResource | ADMIN, GERENTE, CAIXA | `pedidos` |
| ProductResource | ADMIN, GERENTE | `produtos` |
| IngredientResource | ADMIN, GERENTE | `ingredientes` |
| DriverResource | ADMIN, GERENTE | `entregadores` |
| CustomerResource | ADMIN, GERENTE, CAIXA | `clientes` |

---

## 4. Exception Hierarchy (verificada)

```
DomainException (abstract) → getHttpStatus(), getErrorType()
├── EntityNotFoundException (404)
│   ├── ProductNotFoundException
│   ├── OrderNotFoundException
│   ├── FinancialRecordNotFoundException
│   └── ...
├── ConflictException (409)
│   └── *AlreadyExistsException
├── UnauthorizedException (401)
├── BusinessRuleException (422)
│   ├── InvalidOrderTransitionException
│   └── ConcurrentModificationException
└── ForbiddenAccessException (403)
```

### GlobalExceptionMapper — ordem de tratamento
1. `DomainException` → polimórfico (getHttpStatus)
2. `ConstraintViolationException` → 400
3. `IllegalArgumentException` → 400
4. Security exceptions (por class name, sem import) → 401/403
5. `WebApplicationException` (JAX-RS) → status nativo
6. Fallback → 500

---

## 5. Flyway Migrations (existentes)

| Arquivo | Conteúdo |
|---------|----------|
| `V1__foundation.sql` | `store`, `users` |
| `V2__*` até `V8__*` | Catálogo, Orders, Customers, Drivers |
| `V9__financial.sql` | `financial_record`, `financial_record_product` |

Seed: `admin` / `admin123` (store_id = `00000000-0000-0000-0000-000000000001`)

---

## 6. Testes — Padrões Verificados

### Unitários (surefire:test)
- **Domain**: JUnit 5 puro, sem mocks
- **Application**: `@ExtendWith(MockitoExtension.class)`, `@Mock` nos repos/ports
- ⚠️ Quando mock precisa mutar argumento → usar `doAnswer`
- ⚠️ UC usa `findAllById()` para batch → mock esse método, NÃO `findById()`

### Integração (@QuarkusTest)
- Requer Docker (PostgreSQL via Dev Services)
- `AuthResourceTest.getAdminToken()` → helper para obter token admin
- REST Assured para assertions HTTP

### ArchUnit (CleanArchitectureTest)
- 17 testes verificando dependências entre camadas
- Comando: `./mvnw surefire:test -Dtest="CleanArchitectureTest"`
