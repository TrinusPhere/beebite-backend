# BiteFlow Backend — Regras do Projeto

---

## 🎯 PERSONA OBRIGATÓRIA — Leia antes de escrever qualquer código

> **Ao desenvolver qualquer fase ou analisar qualquer código deste projeto, você DEVE atuar
> simultaneamente como:**
>
> **Arquiteto de Software Sênior** + **Especialista Java/Quarkus**
>
> Isso não é um papel opcional. É o filtro pelo qual toda decisão de código passa.

### O que essa persona significa na prática

Antes de criar qualquer arquivo, método ou anotação, pergunte-se:

**Como Arquiteto de Software Sênior:**
- Esta classe tem apenas uma razão para mudar? (SRP)
- Esta dependência aponta na direção certa? (DIP — Use Cases dependem de portas, não de infra)
- Esta adição vai exigir modificar classes existentes no futuro? (OCP)
- Este código vai se repetir em outro lugar? (DRY)
- Estou criando algo que não foi pedido? (YAGNI)
- O domínio está puro — zero `@Entity`, `@Table`, `@Transactional`, `@Inject` em `domain/`?

**Como Especialista Java/Quarkus:**
- Coleções `@ElementCollection` usadas fora da transação são `EAGER`? (P8)
- Campos `@Version` fazem round-trip domain ↔ entity? (P9)
- Controle de acesso é declarativo (`@RolesAllowed` na classe, `@PermissionsAllowed` no método)? (P10)
- `@Transactional` está no repositório (infra), não no Use Case (application)?
- Exceções de infra (JPA, Hibernate) são convertidas pelo repositório para exceções de domínio?
- O mapper `GlobalExceptionMapper` resolve tudo via polimorfismo sem `instanceof` encadeado?

### Critérios de revisão aplicados a TODA entrega

| Pilar | O que verificar |
|-------|----------------|
| **SOLID** | SRP: 1 Use Case = 1 operação. DIP: Use Cases injetam ports, não implementações. OCP: nova exceção não modifica o mapper |
| **Clean Architecture** | domain = zero dependências externas. application = zero `jakarta.ws.rs.*`. presentation = zero `jakarta.persistence.*` |
| **Quarkus/JPA** | Coleções LAZY acessadas fora da sessão → LazyInitializationException. `@Version` sem round-trip → OptimisticLockException |
| **Segurança** | `@RolesAllowed` (classe) + `@PermissionsAllowed` (método). Nunca os dois no mesmo elemento |
| **Qualidade** | Fail-fast, Early Returns, sem God Classes, sem strings mágicas (usar enum `Permission`) |

---

## ⛔ INVIOLÁVEL — O Contrato com o Arquiteto

> **LEIA ISTO ANTES DE QUALQUER OUTRA COISA.**
> Estas regras existem porque violações reais aconteceram e causaram bugs, brechas de segurança e
> dívida técnica. Elas foram escritas com custo — não são sugestões.

### A regra que nunca pode ser quebrada por conveniência

**Quando uma decisão arquitetural existe, ela deve ser seguida MESMO QUE pareça mais fácil não seguir.**

Exemplo real que aconteceu nesta sessão:

> O repositório `PanacheOrderRepository` lançava `OptimisticLockException` (JPA/infra).
> A primeira solução implementada foi mapear essa exceção diretamente no `GlobalExceptionMapper`
> (presentation), fazendo `presentation → infrastructure` — violando a arquitetura em camadas.
> A justificativa foi: *"o mapper já depende de jakarta.ws.rs.*, então é pragmaticamente aceitável."*
> **Isso estava errado.** O fato de uma violação já existir não justifica adicionar outra.
> A correção correta foi feita: o **repositório** (infra) captura `OptimisticLockException` e
> converte para `ConcurrentModificationException extends BusinessRuleException` (domínio).
> O `GlobalExceptionMapper` nunca soube que JPA existia.

### Padrão obrigatório: exceções de infraestrutura → domínio

Toda vez que a **infraestrutura** (JPA, Hibernate, JDBC, HTTP client, etc.) lança uma exceção
que precisa chegar ao usuário como um erro de negócio:

```
❌ PROIBIDO:
presentation/GlobalExceptionMapper
  ├── catch OptimisticLockException  ← presentation conhece JPA = violação arquitetural

✅ OBRIGATÓRIO:
infrastructure/PanacheXxxRepository.save()
  ├── catch OptimisticLockException  ← infra trata o que é dela
  └── throw new ConcurrentModificationException(...)  ← domínio puro

presentation/GlobalExceptionMapper
  └── DomainException (polimorfismo) ← resolve sem saber o que causou
```

### O que cada camada PODE e NÃO PODE importar

Esta tabela é a referência para decidir se um `import` é válido:

| Camada | Pode importar | NÃO pode importar |
|--------|--------------|-------------------|
| `domain` | Java puro, nada externo | `jakarta.*`, `org.hibernate.*`, `io.quarkus.*` |
| `application` | `domain.*`, Java puro, `jakarta.enterprise.*` (CDI) | `jakarta.ws.rs.*`, `jakarta.persistence.*`, `infrastructure.*` |
| `infrastructure` | `domain.*`, `application.*`, `jakarta.persistence.*`, `io.quarkus.panache.*` | `presentation.*` |
| `presentation` | `domain.*`, `application.*`, `jakarta.ws.rs.*` | `jakarta.persistence.*`, `org.hibernate.*`, `infrastructure.*` (direto) |

**Esclarecimento importante:**
- `GlobalExceptionMapper` em `presentation` depende de `jakarta.ws.rs.*` → **CORRETO** — é um adaptador HTTP, deve conhecer JAX-RS
- `GlobalExceptionMapper` em `presentation` depender de `jakarta.persistence.*` → **VIOLAÇÃO** — presentation não deve conhecer JPA
- P7 (Pureza do Domínio) protege a camada `domain` — não impede que `presentation` use `jakarta.ws.rs.*`

### O que fazer quando parece "mais fácil" violar uma regra

1. **Pare.** Reconheça que está buscando um atalho.
2. **Consulte a tabela acima** — identifique qual dependência está errada.
3. **Implemente a solução correta**, mesmo que tome mais tempo.
4. **Nunca documente a violação como "pragmaticamente aceitável"** — documente a solução correta.

---


## 🧭 Princípios Adicionais — Aprendizados das Fases 1-4 (revisados)

> Estes princípios complementam P1-P7. Foram extraídos de erros reais que ocorreram
> na implementação e revisão das fases 1-4 desta sessão.

### 📐 P8 — Coleções JPA devem ser EAGER quando usadas fora da transação

**Princípio:** "Uma coleção `@ElementCollection` ou `@OneToMany` que precisa ser acessada
fora do escopo da transação do repositório DEVE ser `FetchType.EAGER`."

**Por que:** O Hibernate fecha a sessão ao final do método `@Transactional` do repositório.
Se o Use Case ou Mapper acessa a coleção depois disso, ocorre `LazyInitializationException`.

**Regra:** Coleções de IDs simples (`List<UUID>`, `List<String>`) ou listas pequenas que
sempre são necessárias na resposta → `EAGER`. Entidades relacionadas grandes → `LAZY` com
`JOIN FETCH` explícito na query quando necessário.

---

### 📐 P9 — O campo `@Version` é parte do aggregate e deve fazer round-trip completo

**Princípio:** "Toda entidade JPA com `@Version` tem um campo técnico de controle de concorrência.
Esse campo deve existir no domain object correspondente e ser preservado em cada conversão
domain → entity → save."

**Por que:** Se o Mapper não transfere `version` do `OrderEntity` para `Order` e de volta,
o Hibernate recebe `version = null` no `merge()` e lança `OptimisticLockException` em toda atualização —
não porque há conflito real, mas porque o contrato de `@Version` foi quebrado.

**Regra:**
- Todo `reconstitute()` de domain object deve receber e armazenar o `version`
- Todo `toEntity()` de infrastructure mapper deve setar `entity.version = domain.getVersion()`
- A exceção `OptimisticLockException` do JPA é convertida pelo repositório em `ConcurrentModificationException`
  (domínio) — nunca chega ao `GlobalExceptionMapper` como exceção de infraestrutura

---

### 📐 P10 — Controle de acesso é declarativo: roles na classe, permissões nos métodos

**Princípio:** "Segurança não deve ser lógica imperativa dentro do método de negócio.
Deve ser declarada como anotação, separando autenticação/autorização do código de negócio."

**O modelo do BiteFlow:**
```
JWT claim 'groups'      → @RolesAllowed  (ADMIN, GERENTE, CAIXA)
JWT claim 'permissions' → @PermissionsAllowed  (pedidos, produtos, etc.)
```

**Regra de aplicação:**
- `@RolesAllowed` vai na **classe** — controla quais roles podem acessar o Resource inteiro
- `@PermissionsAllowed` vai nos **métodos** — controla qual permissão granular cada operação exige
- **NUNCA** coloque `@RolesAllowed` e `@PermissionsAllowed` no mesmo método — o Quarkus rejeita
  múltiplas anotações de segurança por elemento
- `checkPermission()` imperativo no corpo do método → **PROIBIDO** nas novas fases

**Mapeamento de roles por módulo:**
| Resource | `@RolesAllowed` (classe) | `@PermissionsAllowed` (método) |
|----------|--------------------------|-------------------------------|
| Orders | ADMIN, GERENTE, CAIXA | `pedidos` |
| Products/Ingredients/Categories/Modifiers | ADMIN, GERENTE | `produtos`/`ingredientes` |
| Drivers | ADMIN, GERENTE | `entregadores` |
| Customers | ADMIN, GERENTE, CAIXA | `clientes` |
| Users | ADMIN | `usuarios` |
| Store | ADMIN | — |
| Financeiro (Fase 5) | ADMIN, GERENTE | `financeiro`/`acerto_entregadores` |

**O `JwtPermissionsAugmentor`** em `infrastructure/security/` lê o claim `permissions` do JWT
e injeta no `SecurityIdentity` — ADMIN recebe todas as permissões automaticamente.
Novas permissões devem ser adicionadas ao enum `Permission` em `domain/auth/valueobject/`.

---


**Projeto**: BiteFlow Backend — API REST para gestão de lanchonetes (PDV, pedidos, entregadores, financeiro).
**Stack**: Quarkus 3.33 · Java 25 · PostgreSQL · Flyway · SmallRye JWT (RS256) · Hibernate ORM Panache · JUnit 5 · Mockito · REST Assured
**Package raiz**: `com.fidecode.biteflow`
**Localização do projeto**: `bite-flow/` (dentro do workspace)
**Spec completa**: `doc/biteflow_api_backend_spec_v2.md`
**Plano de fases**: `doc/biteflow_development_phases.md`

---

## 🏛️ Arquitetura (Clean Architecture — 4 camadas)

```
com.fidecode.biteflow/
├── domain/          # POJOs puros — ZERO dependência de framework
│   ├── auth/
│   │   ├── entity/        # User.java
│   │   ├── valueobject/   # Role.java, Permission.java
│   │   ├── repository/    # UserRepository (interface/port)
│   │   └── exception/     # InvalidCredentialsException, UserAlreadyExistsException, UserNotFoundException
│   ├── shared/
│   │   ├── exception/     # DomainException (base), EntityNotFoundException, ConflictException, etc.
│   │   └── StoreGuard.java
│   └── store/
│       ├── entity/        # Store.java
│       ├── repository/    # StoreRepository (interface/port)
│       └── exception/     # StoreNotFoundException
│
├── application/     # Casos de uso INDIVIDUAIS + DTOs (records) + ports + DtoMappers
│   ├── auth/
│   │   ├── port/          # TokenProvider, PasswordEncoder (interfaces)
│   │   ├── dto/           # LoginRequest, LoginResponse, CreateUserRequest, etc.
│   │   └── *.java         # LoginUseCase, CreateUserUseCase, etc. (1 classe = 1 operação)
│   └── catalog/
│       ├── dto/           # AdditionalRequest, AdditionalResponse, etc.
│       ├── AdditionalDtoMapper.java   # toResponse() estático compartilhado
│       └── *.java         # CreateAdditionalUseCase, UpdateAdditionalUseCase, etc.
│
├── infrastructure/  # Adaptadores — Panache, JWT, BCrypt
│   ├── persistence/panache/
│   │   ├── entity/        # StoreEntity.java, UserEntity.java (JPA)
│   │   ├── repository/    # PanacheStoreRepository, PanacheUserRepository
│   │   ├── mapper/        # StoreMapper, UserMapper (domain ↔ entity)
│   │   └── converter/     # StringListConverter (List<String> ↔ text[])
│   └── security/
│       ├── JwtTokenProvider.java   # implements TokenProvider
│       └── PasswordHasher.java     # implements PasswordEncoder
│
└── presentation/    # JAX-RS Resources + Exception Mapper
    ├── rest/
    │   ├── BaseResource.java       # Classe abstrata base — getStoreId(), checkPermission()
    │   ├── AuthResource.java       # POST /api/v1/auth/login, /logout, GET /me
    │   ├── UserResource.java       # CRUD /api/v1/users
    │   └── StoreResource.java      # GET/PUT /api/v1/store
    └── exception/
        ├── GlobalExceptionMapper.java  # Polimórfico via DomainException
        └── ErrorResponse.java          # record padrão de erro
```

## 🧭 Princípios de Design — Guias para TODA Decisão

> **CONTEXTO**: Nas fases 1-4, a IA gerou código que violava princípios fundamentais de engenharia
> de software. As violações foram identificadas em uma auditoria arquitetural e corrigidas.
> Estes princípios devem ser **internalizados** — não são um checklist, são a lente através
> da qual toda decisão de código deve ser avaliada.

### 📐 P1 — Dependency Inversion Principle (DIP)

**Princípio:** "Módulos de alto nível não devem depender de módulos de baixo nível. Ambos devem depender de abstrações."

**O que isso significa neste projeto:**
- Use Cases (application) são alto nível. Infra (Panache, BCrypt, JWT) é baixo nível.
- Um Use Case NUNCA importa uma classe de infraestrutura — ele importa uma **interface (port)** que a infra implementa.

**Violação real que aconteceu (fases 1-4):**
`LoginUseCase` e `CreateUserUseCase` importavam `PasswordHasher` (infraestrutura BCrypt) diretamente, ao invés do port `PasswordEncoder`.

**Como aplicar na prática:**
- Antes de adicionar um `import` num Use Case, pergunte: "essa classe pertence a `infrastructure.*` ou `presentation.*`?"
- Se sim → crie ou use um port em `application/*/port/`
- Se `JsonWebToken`, `Response`, `@Entity` aparece num Use Case → está errado

### 📐 P2 — Single Responsibility Principle (SRP)

**Princípio:** "Uma classe deve ter apenas um motivo para mudar."

**O que isso significa neste projeto:**
- Um Use Case faz UMA operação de negócio. Se ele tem métodos `create()`, `update()`, `delete()`, `list()`, `getById()` — ele tem 5 motivos para mudar.

**Violação real que aconteceu (fases 2-4):**
Foram criadas "God Classes" como `AdditionalUseCases` (68 linhas, 5 responsabilidades), `ModifierGroupUseCases` (108 linhas), `DriverUseCases` (97 linhas).

**Como aplicar na prática:**
- 1 classe = 1 operação: `CreateXUseCase`, `UpdateXUseCase`, `DeleteXUseCase`, `ListXUseCase`, `GetXUseCase`
- O `toResponse()` compartilhado fica num `*DtoMapper` separado (classe `final` com métodos `static`)

### 📐 P3 — Open/Closed Principle (OCP)

**Princípio:** "Aberto para extensão, fechado para modificação."

**O que isso significa neste projeto:**
- Adicionar uma nova exceção de domínio não deve exigir modificação do `GlobalExceptionMapper`.
- Adicionar uma nova entidade não deve exigir modificação de classes existentes.

**Violação real que aconteceu (fases 1-4):**
Exceções herdavam de `RuntimeException` diretamente. Cada nova exceção exigia um novo bloco `if (exception instanceof X)` no `GlobalExceptionMapper` — que chegou a ter 13 blocos!

**Como aplicar na prática:**
- Toda exceção herda da hierarquia `DomainException` → o mapper resolve via polimorfismo
- Novas entidades implementam `StoreGuard.StoreScoped` → o StoreGuard funciona sem alteração

### 📐 P4 — Interface Segregation Principle (ISP)

**Princípio:** "Nenhum cliente deve ser forçado a depender de métodos que não utiliza."

**Violação real que aconteceu (fases 1-4):**
`TokenProvider` tinha um método `extractLogin()` que nenhum Use Case chamava — existia "por conveniência".

**Como aplicar na prática:**
- Interfaces de ports contêm APENAS métodos que algum Use Case realmente consome
- Se nenhum UC chama o método, ele não deveria existir na interface

### 📐 P5 — Encapsulamento e Proteção de Invariantes

**Princípio:** "Um objeto deve proteger seu estado interno e expor comportamento, não dados."

**Violação real que aconteceu (fases 1-4):**
`Order` tinha 21 setters públicos — qualquer camada podia fazer `order.setStatus(FINALIZADO)` sem passar pelas regras de transição do `OrderDomainService`. O aggregate root era um "data bag" anêmico.

**Como aplicar na prática:**
- Aggregate Roots expõem **métodos de comportamento**: `changeStatus()`, `assignDriver()`, `updateCustomerInfo()`
- Reconstrução a partir do banco usa **factory method `reconstitute()`** — não setters
- `storeId` é `final` — imutável após criação
- Setters públicos apenas para: `setId()` (persistência) e `setDisplayId()` (gerado no UC)

### 📐 P6 — DRY (Don't Repeat Yourself)

**Princípio:** "Cada peça de conhecimento deve ter uma representação única e não ambígua no sistema."

**Violação real que aconteceu (fases 1-4):**
- `getStoreId()` era reimplementado em 10+ Resources (cada um com um fallback silencioso para UUID padrão — bug de segurança!)
- `checkPermission()` era copiado em cada Resource
- `getStoreId().equals(entity.getStoreId())` aparecia em 40+ locais nos Use Cases

**Como aplicar na prática:**
- `BaseResource` centraliza `getStoreId()` e `checkPermission()` — Resources herdam
- `StoreGuard.enforce(entity, storeId)` centraliza a validação de multi-tenancy

### 📐 P7 — Pureza do Domínio (Clean Architecture)

**Princípio:** "O domínio é o núcleo do sistema. Ele não conhece nenhuma camada externa — nem framework, nem banco, nem HTTP."

**Como aplicar na prática:**
- Classes em `domain/*/entity/` e `domain/*/valueobject/` são POJOs puros — sem `@Entity`, `@Table`, `@Column`, `@Inject`
- Exceções de domínio são classes Java puras — sem `jakarta.ws.rs.*`
- A ÚNICA exceção é `@ApplicationScoped` em Domain Services (necessário para CDI, mas não afeta lógica)

---

## 🚨 Regras Derivadas — Implementação Concreta dos Princípios

> Estas regras são a **materialização** dos princípios acima **neste projeto específico**.
> São verificadas automaticamente por 17 testes ArchUnit em `CleanArchitectureTest.java`.

### R1 — Hierarquia de exceções (deriva de P3-OCP)

Toda exceção de domínio DEVE herdar da hierarquia `DomainException`:
```
DomainException (abstract) → getHttpStatus(), getErrorType()
├── EntityNotFoundException (404) — para *NotFoundException
├── ConflictException (409) — para *AlreadyExistsException
├── UnauthorizedException (401) — para credenciais inválidas
├── BusinessRuleException (422) — para regras de negócio violadas
└── ForbiddenAccessException (403) — para acesso negado
```

Exemplo para uma nova fase:
```java
// ✅ CORRETO
public class CashRegisterNotFoundException extends EntityNotFoundException {
    public CashRegisterNotFoundException(String id) { super("Caixa não encontrado: " + id); }
}

// ❌ ERRADO — herda de RuntimeException (GlobalExceptionMapper não vai mapear)
public class CashRegisterNotFoundException extends RuntimeException { ... }
```

### R2 — Use Cases individuais (deriva de P2-SRP)

Padrão obrigatório para cada módulo:
```
Create*UseCase.java    — criação
Update*UseCase.java    — atualização
Delete*UseCase.java    — remoção
List*UseCase.java      — listagem paginada
Get*UseCase.java       — busca por ID
*DtoMapper.java        — final class, static toResponse()
```

### R3 — BaseResource obrigatório (deriva de P6-DRY)

Todo `*Resource.java` em `presentation/rest/` DEVE:
1. `extends BaseResource`
2. Usar `getStoreId()` herdado (fail-fast, sem fallback)
3. Usar `checkPermission("permissao")` herdado
4. NUNCA injetar `JsonWebToken` diretamente

### R4 — StoreGuard para multi-tenancy (deriva de P6-DRY)

Toda operação por ID DEVE validar ownership:
```java
Entity entity = repository.findById(id)
    .orElseThrow(() -> new EntityNotFoundException(id.toString()));
StoreGuard.enforce(entity, storeId);
```
Toda entidade com `storeId` implementa `StoreGuard.StoreScoped`.

### R5 — Aggregate Roots protegem invariantes (deriva de P5-Encapsulamento)

- Métodos de comportamento ao invés de setters
- Factory `reconstitute()` para o Mapper da infra
- `storeId` é `final`

### R6 — Maven Wrapper obrigatório

```bash
./mvnw compile          # compilar
./mvnw surefire:test    # testes unitários
./mvnw test             # testes de integração (requer Docker)
```

### R7 — Testes ArchUnit como gate de qualidade

Antes de considerar qualquer fase como "concluída":
```bash
./mvnw surefire:test -Dtest="com.fidecode.biteflow.architecture.CleanArchitectureTest"
```
Se algum dos 17 testes falhar → a fase NÃO está concluída.


---

## 🔴🟢🔵 Metodologia TDD — OBRIGATÓRIA

**Regra inegociável**: Nenhum arquivo de produção é criado sem um teste que falha (🔴) antes.

### Ordem por camada:
1. 🔴 **Domain** → JUnit 5 puro
2. 🔴 **Application** → JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
3. 🔴 **Infrastructure** → `@QuarkusTest` (requer Docker para banco via Dev Services)
4. 🔴 **Presentation** → `@QuarkusTest` + REST Assured

### Comandos de teste:
```bash
# Domain / Use Cases (sem banco)
./mvnw surefire:test -Dtest="NomeDoTest"

# Integração completa (requer Docker)
./mvnw test
```

---

## 📋 Status das Fases

| Fase | Descrição | Status |
|------|-----------|--------|
| **Fase 1** | Fundação: Auth + Store + Users | ✅ COMPLETA |
| **Fase 2** | Catálogo: Ingredientes + Produtos + Categorias | ✅ COMPLETA |
| **Fase 3** | Pedidos: Order + OrderItem + Customer + Kanban | ✅ COMPLETA |
| **Fase 4** | Entregadores: Deliverer + atribuição de pedidos | ✅ COMPLETA |
| **Fase 5** | Financeiro: Caixa + acerto entregadores | ✅ COMPLETA |
| Fase 6 | Integrações: iFood + WhatsApp | ⬜ |
| Fase 7 | Operacional: Dashboard + relatórios | ⬜ |
| Fase 8 | Polish & Deploy | ⬜ |

---

## 🗄️ Banco de Dados

- Migrations Flyway em `src/main/resources/db/migration/`
  - `V1__foundation.sql` — tabelas `store` + `users` ✅
- **UUID PKs** em todas as tabelas
- **`store_id`** obrigatório em TODAS as tabelas (multi-tenancy)
- **`unit_price` snapshot** obrigatório em `ORDER_ITEM`
- Seed: `admin` / `admin123` (store_id=`00000000-0000-0000-0000-000000000001`)

---

## 🔐 Segurança / JWT

- RS256 — chaves em `src/main/resources/META-INF/resources/`
- Claims do token: `sub` (login), `groups` (role), `userId`, `name`, `permissions` (List)
- Permissão granular verificada via claim `permissions` no `BaseResource.checkPermission()`
- ADMIN tem todas as permissões via `User.createAdmin()`

---

## ⚙️ Convenções Críticas

### Repositórios
- **Padrão de duas classes** (resolve conflito de assinaturas mantendo o Repository Pattern):
  - `*PanacheRepo implements PanacheRepositoryBase<Entity, UUID>` → classe auxiliar pura Panache (sem implementar port de domínio)
  - `Panache*Repository implements DomainRepository` → implementa o port, **injeta** a classe auxiliar Panache
- Isso evita o conflito de `findById(UUID)` e `findAll()` que têm tipos de retorno incompatíveis entre a porta de domínio e o `PanacheRepositoryBase`
- Métodos disponíveis via `repo.*`: `repo.findByIdOptional()`, `repo.find()`, `repo.listAll()`, `repo.count()`, `repo.persist()`, `repo.deleteById()`, `repo.getEntityManager()`
- `@Transactional` em métodos de escrita

### Use Cases
- `@ApplicationScoped` + construtor `@Inject`
- Cada Use Case é uma classe individual (ver R2)
- `*DtoMapper` com `static toResponse()` compartilhado entre use cases do mesmo módulo
- Senha só re-hasheada quando `password` não-nulo/não-vazio no update

### Exceptions
- Toda exceção de domínio HERDA de `DomainException` (ver R4)
- `GlobalExceptionMapper` resolve via polimorfismo — NUNCA adicionar novos blocos `instanceof`
- Jamais lançar `WebApplicationException` nos use cases

### Entidades de Domínio (Aggregate Roots)
- Métodos de comportamento ao invés de setters públicos (ver R6)
- Factory method `reconstitute()` para reconstrução a partir do banco
- `storeId` é `final` e imutável
- Implementam `StoreGuard.StoreScoped` para multi-tenancy

---

## 📝 Início de sessão — checklist

1. Leia `doc/biteflow_development_phases.md` → identifique a fase atual
2. Leia `doc/biteflow_api_backend_spec_v2.md` → entidades e rotas da fase
3. Crie/atualize `task.md` para rastrear progresso
4. Siga sempre: 🔴 testes → 🟢 código → 🔵 refactor
5. **NUNCA** crie arquivo de produção sem teste falhando antes
6. **OBRIGATÓRIO:** Toda fase *deve obrigatoriamente* incluir a integração do front-end com os endpoints criados e a realização de um teste End-to-End (E2E) interativo com o usuário (Front + Back funcionando).
7. **OBRIGATÓRIO:** Antes de marcar uma fase como concluída, rodar `./mvnw surefire:test -Dtest="com.fidecode.biteflow.architecture.CleanArchitectureTest"` e garantir 17/17 testes passando.
---

## 🧪 Convenção de Testes — Economia de Tokens

> **Regra estabelecida pelo usuário em 2026-08-18.**

### O que o agente deve fazer

Ao implementar uma funcionalidade, rodar **apenas os testes da funcionalidade que está sendo implementada**:

```bash
# ✅ CORRETO — testa só o que foi alterado
./mvnw surefire:test -Dtest="com.fidecode.biteflow.application.order.OrderUseCasesTest"

# ✅ CORRETO — testa um teste específico
./mvnw surefire:test -Dtest="com.fidecode.biteflow.application.order.OrderUseCasesTest#deveLancarExcecaoAoEditarPedidoEmRota"

# ❌ PROIBIDO pelo agente — suite completa gasta tokens desnecessários
./mvnw surefire:test
./mvnw test
```

### O que o usuário faz

O usuário roda a suíte completa por conta própria quando achar necessário:
```bash
./mvnw surefire:test   # unitários
./mvnw test            # integração (requer Docker)
```

### Exceção permitida

Rodar a suite completa **somente** quando:
1. O usuário pedir explicitamente
2. Antes de marcar uma **fase** como concluída (obrigatório pelo checklist acima)
3. Após mudanças estruturais que possam afetar múltiplos módulos (ex: mudança em `domain/shared/`, `GlobalExceptionMapper`, `BaseResource`)

---

## 🖥️ Front-End (Vue 3) — Anti-Padrões e Lições Aprendidas

> **Aviso:** Nas interações anteriores, bugs silenciosos graves ocorreram devido a pequenos descuidos na forma como o Vue 3 interage com as respostas do Backend (Java).

### FE-1 — Mapeamento Incorreto de ID vs `ingredientId`
- A API retorna sub-itens frequentemente com `ingredientId` ou `productId` em vez de apenas `id`.
- **Regra:** Sempre confira o DTO do Backend (`ProductResponse.java`) antes de fazer um `v-for` ou `v-model`. Se a tela tentar usar `item.id` e ele for `undefined`, múltiplos componentes Vue compartilharão o mesmo estado (ex: todos os checkboxes ativando juntos).

### FE-2 — Array de Objetos vs Array de Strings (IDs)
- A tela geralmente usa um Array de Strings (IDs) para checkar se um item está selecionado em um grupo de toggles. A API envia um Array de Objetos.
- **Regra:** Crie um mapeamento explícito no `onMounted` ao invés de usar o array de objetos diretamente no controle visual:
  `item.removableItemIds = (item.removableItems || []).map(r => r.ingredientId)`

### FE-3 — Case Sensitivity de Enums
- O Backend Java envia enums em caixa alta (ex: `RADIO`, `CHECKBOX`).
- **Regra:** Ao testar igualdade no Javascript/Vue, SEMPRE neutralize a caixa:
  `if (group.type?.toLowerCase() === 'radio')`

### FE-4 — Ícones do Lucide no `<script setup>`
- Componentes dinâmicos `<component :is="...">` no Vue 3 *não* resolvem componentes importados localmente se você passar uma string com o nome do componente.
- **Regra:** O valor do `:is` deve ser a **referência direta ao objeto** do componente.
  - ❌ `const config = { icon: 'DollarSign' }`
  - ✅ `const config = { icon: DollarSign }`

