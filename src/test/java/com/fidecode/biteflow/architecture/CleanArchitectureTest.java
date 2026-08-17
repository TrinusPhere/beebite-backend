package com.fidecode.biteflow.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.fidecode.biteflow", importOptions = { com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class })
public class CleanArchitectureTest {

    @ArchTest
    static final ArchRule domainShouldNotDependOnOtherLayers =
        noClasses()
            .that().resideInAPackage("com.fidecode.biteflow.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.fidecode.biteflow.application..",
                "com.fidecode.biteflow.infrastructure..",
                "com.fidecode.biteflow.presentation.."
            )
            .allowEmptyShould(true)
            .because("A camada de Domínio deve ser pura e não depender de outras camadas da aplicação.");

    @ArchTest
    static final ArchRule applicationShouldNotDependOnInfrastructureOrPresentation =
        noClasses()
            .that().resideInAPackage("com.fidecode.biteflow.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.fidecode.biteflow.infrastructure..",
                "com.fidecode.biteflow.presentation.."
            )
            .allowEmptyShould(true)
            .because("A camada de Aplicação (Casos de Uso) não deve depender da Infraestrutura ou Apresentação.");

    @ArchTest
    static final ArchRule presentationShouldNotDependOnInfrastructure =
        noClasses()
            .that().resideInAPackage("com.fidecode.biteflow.presentation..")
            .should().dependOnClassesThat().resideInAPackage("com.fidecode.biteflow.infrastructure.persistence..")
            .allowEmptyShould(true)
            .because("A camada de Apresentação não deve acessar repositórios do Panache ou persistência diretamente.");

    @ArchTest
    static final ArchRule domainAndApplicationShouldNotDependOnJaxRs =
        noClasses()
            .that().resideInAnyPackage("com.fidecode.biteflow.domain..", "com.fidecode.biteflow.application..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.ws.rs..")
            .allowEmptyShould(true)
            .because("As camadas de Domain e Application não devem depender do JAX-RS (ex: WebApplicationException).");

    @ArchTest
    static final ArchRule presentationShouldNotDependOnEntities =
        noClasses()
            .that().resideInAPackage("com.fidecode.biteflow.presentation.rest..")
            .should().dependOnClassesThat().resideInAPackage("com.fidecode.biteflow.domain..entity..")
            .allowEmptyShould(true)
            .because("O REST Resource não deve usar Entidades de Domínio diretamente, use DTOs.");
}
