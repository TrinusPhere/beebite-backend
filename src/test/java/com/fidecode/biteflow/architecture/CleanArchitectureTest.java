package com.fidecode.biteflow.architecture;

import com.fidecode.biteflow.presentation.rest.BaseResource;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * Testes de Arquitetura com ArchUnit — BiteFlow Backend.
 *
 * Garante que a Clean Architecture não sofra erosão. Esses testes são a última linha de defesa: se um dev violar a
 * arquitetura, o build quebra ANTES do code review.
 *
 * ───────────────────────────────────────────────────────────────────────── Grupo 1 — Regra de Dependência (Clean
 * Architecture) Garante que o fluxo de dependências seja sempre: Presentation → Application → Domain ← Infrastructure
 *
 * Grupo 2 — DIP (Dependency Inversion Principle) Garante que Use Cases dependam de abstrações (ports), nunca de
 * implems.
 *
 * Grupo 3 — Pureza do Domínio Garante que entidades de domínio sejam POJOs sem framework.
 *
 * Grupo 4 — Convenções da Presentation Layer Garante que Resources herdem BaseResource e não acessem infra diretamente.
 *
 * Grupo 5 — Naming Conventions Garante consistência de nomes para manutenibilidade.
 * ─────────────────────────────────────────────────────────────────────────
 */
@AnalyzeClasses(packages = "com.fidecode.biteflow", importOptions = {
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class })
public class CleanArchitectureTest {

    // ═══════════════════════════════════════════════════════════════════════
    // GRUPO 1 — Regra de Dependência (Clean Architecture)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * O Domínio é o núcleo: não conhece nenhuma camada externa.
     */
    @ArchTest
    static final ArchRule domainShouldNotDependOnOtherLayers = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.domain..").should().dependOnClassesThat()
            .resideInAnyPackage("com.fidecode.biteflow.application..", "com.fidecode.biteflow.infrastructure..",
                    "com.fidecode.biteflow.presentation..")
            .allowEmptyShould(true).because("A camada de Domínio deve ser pura e não depender de outras camadas.");

    /**
     * Application conhece apenas Domain. Nunca Infrastructure ou Presentation. Esta regra teria capturado o bug
     * histórico onde PasswordHasher (infra) era importado diretamente nos Use Cases.
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnInfrastructureOrPresentation = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.application..").should().dependOnClassesThat()
            .resideInAnyPackage("com.fidecode.biteflow.infrastructure..", "com.fidecode.biteflow.presentation..")
            .allowEmptyShould(true)
            .because("Use Cases não devem conhecer implementações de Infraestrutura ou camada de Apresentação.");

    /**
     * Presentation não acessa persistência diretamente — apenas via Use Cases.
     */
    @ArchTest
    static final ArchRule presentationShouldNotDependOnInfrastructure = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.presentation..").should().dependOnClassesThat()
            .resideInAPackage("com.fidecode.biteflow.infrastructure.persistence..").allowEmptyShould(true)
            .because("Resources não devem acessar repositórios do Panache ou JPA diretamente.");

    /**
     * Domain e Application são agnósticos a HTTP — sem JAX-RS.
     */
    @ArchTest
    static final ArchRule domainAndApplicationShouldNotDependOnJaxRs = noClasses().that()
            .resideInAnyPackage("com.fidecode.biteflow.domain..", "com.fidecode.biteflow.application..").should()
            .dependOnClassesThat().resideInAPackage("jakarta.ws.rs..").allowEmptyShould(true)
            .because("Domain e Application não devem depender de JAX-RS (ex: WebApplicationException, Response).");

    /**
     * Resources não usam Entidades de Domínio — apenas DTOs. Garante que a presentation layer permaneça desacoplada da
     * lógica de domínio.
     */
    @ArchTest
    static final ArchRule presentationShouldNotDependOnDomainEntities = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.presentation.rest..").should().dependOnClassesThat()
            .resideInAPackage("com.fidecode.biteflow.domain..entity..").allowEmptyShould(true).because(
                    "REST Resources não devem usar Entidades de Domínio diretamente — use DTOs da camada application.");

    // ═══════════════════════════════════════════════════════════════════════
    // GRUPO 2 — DIP: Application depende de Abstrações, nunca de Implems
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Use Cases NÃO podem referenciar PasswordHasher diretamente. Devem usar o port PasswordEncoder.
     *
     * HISTÓRICO: Esta regra protege contra o bug corrigido na refatoração do architectural review onde LoginUseCase,
     * CreateUserUseCase e UpdateUserUseCase importavam PasswordHasher (infraestrutura) diretamente.
     */
    @ArchTest
    static final ArchRule applicationMustNotDependOnPasswordHasher = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.application..").should().dependOnClassesThat()
            .haveSimpleName("PasswordHasher").allowEmptyShould(true)
            .because("Use Cases devem depender do port PasswordEncoder (application.auth.port), "
                    + "nunca da implementação concreta BCrypt (PasswordHasher).");

    /**
     * Use Cases NÃO podem referenciar JwtTokenProvider diretamente. Devem usar o port TokenProvider.
     */
    @ArchTest
    static final ArchRule applicationMustNotDependOnJwtTokenProvider = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.application..").should().dependOnClassesThat()
            .haveSimpleName("JwtTokenProvider").allowEmptyShould(true)
            .because("Use Cases devem depender do port TokenProvider, nunca da implementação JwtTokenProvider.");

    /**
     * Application NÃO pode usar JsonWebToken do SmallRye. Claims JWT são responsabilidade da camada de apresentação.
     * Use Cases recebem dados já extraídos (ex: storeId como UUID).
     */
    @ArchTest
    static final ArchRule applicationMustNotUseJsonWebToken = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.application..").should().dependOnClassesThat()
            .haveSimpleName("JsonWebToken").allowEmptyShould(true).because("JsonWebToken é contexto de Apresentação. "
                    + "Use Cases recebem storeId/userId como parâmetros simples, não o token inteiro.");

    /**
     * Panache entities (JPA) não devem ser usadas fora da infraestrutura. Garante que nenhum Use Case ou Resource
     * acesse entities JPA diretamente.
     */
    @ArchTest
    static final ArchRule panacheEntitiesMustStayInInfrastructure = noClasses().that()
            .resideInAnyPackage("com.fidecode.biteflow.domain..", "com.fidecode.biteflow.application..",
                    "com.fidecode.biteflow.presentation..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.fidecode.biteflow.infrastructure.persistence.panache.entity..")
            .allowEmptyShould(true)
            .because("Entidades JPA (Panache) são detalhe de implementação — confinadas à infraestrutura. "
                    + "Use as entidades de domínio puras (domain.*/entity).");

    // ═══════════════════════════════════════════════════════════════════════
    // GRUPO 3 — Pureza do Domínio
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Entidades de domínio são POJOs puros — sem anotações JPA. A anotação @ApplicationScoped em domain services é a
     * única exceção aceitável (e está no pacote .service, não .entity).
     */
    @ArchTest
    static final ArchRule domainEntitiesMustNotHaveJpaAnnotations = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.domain..entity..").should().dependOnClassesThat()
            .resideInAPackage("jakarta.persistence..").allowEmptyShould(true)
            .because("Entidades de domínio (domain.*/entity) devem ser POJOs puros, "
                    + "sem anotações JPA. As anotações JPA pertencem às Panache entities (infrastructure).");

    /**
     * Value Objects do domínio também devem ser puros — sem CDI, JPA, JAX-RS.
     */
    @ArchTest
    static final ArchRule domainValueObjectsMustBePure = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.domain..valueobject..").should().dependOnClassesThat()
            .resideInAnyPackage("jakarta.persistence..", "jakarta.enterprise..", "jakarta.ws.rs..", "io.quarkus..")
            .allowEmptyShould(true)
            .because("Value Objects de domínio devem ser tipos puros Java — sem dependências de framework.");

    /**
     * Exceções de domínio não devem depender de HTTP ou framework. Devem ser classes Java puras que o
     * GlobalExceptionMapper converte.
     */
    @ArchTest
    static final ArchRule domainExceptionsMustBePure = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.domain..exception..").should().dependOnClassesThat()
            .resideInAnyPackage("jakarta.ws.rs..", "jakarta.persistence..", "jakarta.enterprise..", "io.quarkus..")
            .allowEmptyShould(true).because("Exceções de domínio devem ser RuntimeException puras. "
                    + "A conversão para HTTP status é responsabilidade do GlobalExceptionMapper.");

    // ═══════════════════════════════════════════════════════════════════════
    // GRUPO 4 — Convenções da Presentation Layer
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Todos os REST Resources devem herdar de BaseResource.
     *
     * HISTÓRICO: Antes da refatoração, getStoreId() e checkPermission() estavam clonados em 10 Resources (violação DRY
     * massiva). BaseResource centraliza e padroniza esses comportamentos.
     *
     * Se um dev criar um novo Resource sem herdar BaseResource, este teste falha.
     */
    @ArchTest
    static final ArchRule allResourcesMustExtendBaseResource = classes().that()
            .resideInAPackage("com.fidecode.biteflow.presentation.rest..").and().haveSimpleNameEndingWith("Resource")
            .and().areNotInterfaces().and().areNotAnnotations().should().beAssignableTo(BaseResource.class)
            .allowEmptyShould(true).because("Todos os JAX-RS Resources devem herdar BaseResource para garantir "
                    + "extração segura de storeId (sem fallback silencioso) e verificação de permissão padronizada.");

    /**
     * Exception Mappers ficam no pacote presentation.exception, não em presentation.rest. Garante separação entre
     * roteamento HTTP e tratamento de erros.
     */
    @ArchTest
    static final ArchRule exceptionMappersMustBeInExceptionPackage = classes().that()
            .haveSimpleNameEndingWith("ExceptionMapper").and().resideInAPackage("com.fidecode.biteflow.presentation..")
            .should().resideInAPackage("com.fidecode.biteflow.presentation.exception..").allowEmptyShould(true)
            .because("Exception Mappers devem estar em presentation.exception, "
                    + "não misturados com os REST Resources em presentation.rest.");

    // ═══════════════════════════════════════════════════════════════════════
    // GRUPO 5 — Naming Conventions (Manutenibilidade)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Interfaces de ports na application devem terminar com o nome funcional sem sufixo "Impl" — implementações vivem
     * na infra.
     *
     * Garante que ports sejam nomeados pelo papel (PasswordEncoder, TokenProvider) e não pela tecnologia
     * (BCryptEncoder, JwtProvider).
     */
    @ArchTest
    static final ArchRule portsMustNotHaveImplSuffix = noClasses().that()
            .resideInAPackage("com.fidecode.biteflow.application..port..").should().haveSimpleNameEndingWith("Impl")
            .allowEmptyShould(true).because("Ports (interfaces) não devem ter sufixo 'Impl'. "
                    + "Apenas as implementações na infraestrutura podem usar esse sufixo.");

    /**
     * Repositórios de domínio (ports) devem terminar com "Repository". Garante consistência do contrato e facilita
     * descoberta.
     */
    @ArchTest
    static final ArchRule domainRepositoriesMustFollowNamingConvention = classes().that()
            .resideInAPackage("com.fidecode.biteflow.domain..repository..").and().areInterfaces().should()
            .haveSimpleNameEndingWith("Repository").allowEmptyShould(true)
            .because("Interfaces de repositório de domínio devem terminar com 'Repository' "
                    + "para distingui-las de outros tipos e facilitar a descoberta.");

    /**
     * Mappers de infraestrutura devem ficar no pacote infrastructure.persistence.panache.mapper. Garante que a
     * conversão domain ↔ JPA entity não vaze para outras camadas.
     */
    @ArchTest
    static final ArchRule infrastructureMappersMustBeInMapperPackage = classes().that()
            .resideInAPackage("com.fidecode.biteflow.infrastructure..").and().haveSimpleNameEndingWith("Mapper")
            .should().resideInAPackage("com.fidecode.biteflow.infrastructure.persistence.panache.mapper..")
            .allowEmptyShould(true).because("Mappers de infra (domain ↔ JPA entity) devem estar em "
                    + "infrastructure.persistence.panache.mapper, não espalhados pela infraestrutura.");

}
