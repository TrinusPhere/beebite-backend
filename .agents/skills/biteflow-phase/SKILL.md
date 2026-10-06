---
name: biteflow-phase
description: >
  Implementar uma fase do projeto BiteFlow seguindo TDD.
  Ativada quando o usuário pede para implementar ou executar uma fase do BiteFlow
  (ex: "implemente a fase 2", "execute o plano da fase 3").
---

# Skill: Implementar Fase BiteFlow (TDD)

## Pré-requisitos — leia ANTES de escrever qualquer código

1. **Leia OBRIGATORIAMENTE** `references/anti-patterns-and-lessons.md` — erros reais que JÁ COMETI e NÃO DEVO REPETIR
2. **Leia** `references/code-context-cache.md` — assinaturas, convenções e contexto verificado (EVITA re-leitura de arquivos)
3. **Leia** `doc/biteflow_development_phases.md` — seção da fase solicitada
4. **Leia** `doc/biteflow_api_backend_spec_v2.md` — entidades e rotas da fase
5. **Verifique** `src/main/resources/db/migration/` — qual foi a última migration `V{N}`
6. **Inspecione** a estrutura existente em `src/main/java/com/fidecode/biteflow/` para não duplicar

## Fluxo de Execução

### Passo 0 — Crie o task.md
Crie ou atualize o arquivo de tasks com todos os passos da fase.

### Passo 1 — Setup (sem testes)
- Adicionar dependências novas ao `pom.xml` se necessário
- Criar migration `V{N+1}__<descricao>.sql` em `src/main/resources/db/migration/`
- Criar pacotes de diretórios necessários

### Passo 2 — 🔴🟢 Domain
```
src/test/java/com/fidecode/biteflow/domain/<NomeDaEntidade>Test.java
src/main/java/com/fidecode/biteflow/domain/<modulo>/entity/<Entidade>.java
src/main/java/com/fidecode/biteflow/domain/<modulo>/repository/<Entidade>Repository.java
src/main/java/com/fidecode/biteflow/domain/<modulo>/exception/...
```

### Passo 3 — 🔴🟢 Application
```
src/test/java/com/fidecode/biteflow/application/<NomeUseCase>Test.java
src/main/java/com/fidecode/biteflow/application/<modulo>/dto/...
src/main/java/com/fidecode/biteflow/application/<modulo>/<NomeUseCase>.java
```

### Passo 4 — 🔴🟢 Infrastructure
```
src/test/java/com/fidecode/biteflow/infrastructure/<NomeRepositoryTest>.java
src/main/java/com/fidecode/biteflow/infrastructure/persistence/panache/entity/<Entidade>Entity.java
src/main/java/com/fidecode/biteflow/infrastructure/persistence/panache/repository/Panache<Nome>Repository.java
src/main/java/com/fidecode/biteflow/infrastructure/persistence/panache/mapper/<Nome>Mapper.java
```

### Passo 5 — 🔴🟢 Presentation
```
src/test/java/com/fidecode/biteflow/presentation/<NomeResource>Test.java
src/main/java/com/fidecode/biteflow/presentation/rest/<NomeResource>.java
```

### Passo 6 — Verificação
```bash
mvn compile                    # deve compilar sem erros
mvn surefire:test -Dtest="..."  # testes unitários (sem banco)
mvn test                       # todos os testes (requer Docker)
```

## Convenções Obrigatórias

- **Repositórios**: padrão Repository do Panache — criar `*PanacheRepo implements PanacheRepositoryBase<Entity, UUID>` e injetá-lo na classe `Panache*Repository` que implementa o port de domínio. Isso resolve o conflito de assinaturas (`findById`, `findAll`) sem abrir mão do Panache Repository pattern
- **DTOs**: Java records com validação `jakarta.validation`
- **Use Cases**: `@ApplicationScoped` + construtor `@Inject`
- **Exceptions**: em `domain/<modulo>/exception/` — nunca `WebApplicationException` nos use cases
- **Migration**: toda nova tabela precisa de `store_id UUID NOT NULL REFERENCES store(id)`
- **PKs**: sempre `UUID DEFAULT gen_random_uuid()`
- **Testes Mockito**: `@ExtendWith(MockitoExtension.class)` — evite `UnnecessaryStubbing`

## Atualização de Status

Ao finalizar, atualize a tabela de fases no `AGENTS.md` marcando a fase como ✅ COMPLETA.
