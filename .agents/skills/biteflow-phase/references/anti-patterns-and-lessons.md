# BiteFlow — Lições Aprendidas e Anti-Padrões Proibidos

> **ESTE DOCUMENTO É DE LEITURA OBRIGATÓRIA antes de qualquer modificação no projeto.**
> Cada regra aqui foi escrita porque eu (a IA) cometi o erro descrito. São lições de sessões reais.

---

## 🚫 ANTI-PADRÃO 1 — "Já existe, então é aceitável"

### O que aconteceu
Ao tratar exceções de segurança do Quarkus (`io.quarkus.security.ForbiddenException`) no `GlobalExceptionMapper`,
eu justifiquei o import direto com: *"O mapper já depende de `jakarta.ws.rs.*` — então depender de
`io.quarkus.security.*` também é aceitável."*

### Por que está errado
Esse é o MESMO raciocínio que causou a violação original documentada no AGENTS.md:
> *"O fato de uma violação já existir não justifica adicionar outra."*

Cada dependência deve ser avaliada **individualmente** pela tabela de imports permitidos.
"X já existe" **NUNCA** é justificativa para "Y também pode existir".

### A solução correta
Usar verificação pelo nome da classe (`exception.getClass().getName().equals(...)`) ao invés
de `instanceof` com import direto. Assim `presentation` não importa classes de framework.

```java
// ❌ PROIBIDO — acopla presentation a io.quarkus.security
if (exception instanceof io.quarkus.security.ForbiddenException) { ... }

// ✅ CORRETO — zero imports, zero acoplamento
String className = exception.getClass().getName();
if (className.equals("io.quarkus.security.ForbiddenException")) { ... }
```

---

## 🚫 ANTI-PADRÃO 2 — Código antes do Teste (violação TDD)

### O que aconteceu
Na Fase 5, criei as classes de domínio (`FinancialRecord`, `FinancialStatus`, etc.) ANTES de
escrever os testes. O TDD do projeto exige 🔴 RED primeiro.

### A regra
```
🔴 RED:   Escrever o TESTE que falha (arquivo de teste PRIMEIRO)
🟢 GREEN: Escrever o código de produção MÍNIMO para o teste passar
🔵 REFACTOR: Melhorar sem quebrar testes
```

### Checklist antes de criar qualquer arquivo .java em src/main/
- [ ] Existe um teste correspondente em src/test/ que FALHA?
- [ ] Eu rodei esse teste e vi ele FALHAR (🔴)?
- Se não → PARE e crie o teste primeiro.

---

## 🚫 ANTI-PADRÃO 3 — Verificação imperativa de segurança

### O que aconteceu
O `StoreResource.updateStore()` tinha:
```java
if (!jwt.getGroups().contains("ADMIN")) {
    throw new ForbiddenException("...");
}
```
Isso viola P10 — segurança é declarativa, não imperativa.

### A regra (P10)
- `@RolesAllowed` na **classe** → quais roles acessam
- `@PermissionsAllowed` no **método** → qual permissão granular
- **NUNCA** `jwt.getGroups().contains(...)` ou `checkPermission()` no corpo do método

---

## 🚫 ANTI-PADRÃO 4 — Mock incompleto em testes unitários

### O que aconteceu
`OrderUseCasesTest` mockava `orderDomainService.advanceStatus()` sem `doAnswer`,
fazendo com que o mock não mutasse o estado do `Order`. Resultado: o teste falhava
porque o status permanecia inalterado.

### A regra
Quando um mock precisa **mutar o estado** do argumento (side effect), use `doAnswer`:
```java
// ❌ ERRADO — mock default (void) não faz nada, order.status fica inalterado
// (nenhum when/doAnswer configurado)

// ✅ CORRETO — configura o side effect esperado
doAnswer(inv -> {
    Order o = inv.getArgument(0);
    o.changeStatus(OrderStatus.PREPARO);
    return null;
}).when(orderDomainService).advanceStatus(any(Order.class));
```

### Outro erro comum: mock do método errado
```java
// ❌ ERRADO — UC usa findAllById(), não findById()
when(productRepository.findById(id)).thenReturn(Optional.of(product));

// ✅ CORRETO — mock o método que o UC realmente chama
when(productRepository.findAllById(List.of(id))).thenReturn(List.of(product));
```

---

## 🚫 ANTI-PADRÃO 5 — Assumir tipos de retorno do SmallRye JWT

### O que aconteceu
`jwt.getClaim("storeId")` retorna `jakarta.json.JsonString` (impl: `org.eclipse.parsson.JsonStringImpl`),
NÃO `java.lang.String`. Isso causa `ClassCastException` silencioso.

### Onde isso afeta
1. **`BaseResource.getStoreId()`** — `jwt.getClaim()` retorna `Object`, não `String`
2. **`JwtPermissionsAugmentor.extractPermissions()`** — `jwt.getClaim("permissions")` retorna `List<JsonString>`, não `List<String>`

### A solução padrão
```java
// Para claims simples:
Object raw = jwt.getClaim("storeId");
String value = raw.toString().replace("\"", "");

// Para claims array:
List<?> perms = jwt.getClaim("permissions");
Set<String> result = perms.stream()
    .map(Object::toString)
    .map(s -> s.replace("\"", ""))
    .collect(Collectors.toUnmodifiableSet());
```

---

## 📋 Checklist Pré-Implementação (aplicar ANTES de cada tarefa)

1. **Dependências** — O import que vou adicionar está na tabela permitida do AGENTS.md?
2. **TDD** — O teste existe e FALHA antes de eu criar o código de produção?
3. **Segurança** — Estou usando anotações declarativas, não checks imperativos?
4. **Mocks** — O mock configura side effects quando o método mutata o argumento?
5. **JWT Claims** — Estou tratando o retorno como `Object`, não como `String`?
6. **Justificativa** — Minha justificativa é "X já existe, então Y é ok"? Se sim → PARE.

---

## 🚫 ANTI-PADRÃO 6 — Mapeamento Incorreto de ID vs `ingredientId` no Vue

### O que aconteceu
Ao renderizar o `v-for` e o `v-model` dos ingredientes (`technicalSheet` ou `removableItems`), a tela de `ConfiguracaoModificadores.vue` estava usando `ingredient.id`. A API retornava `ingredientId`. Com isso, todas as variáveis possuíam o valor `undefined`. Ao clicar em um checkbox, o Vue marcava *todos*, pois todos compartilhavam o "id" `undefined`.

### A regra
Sempre inspecione o DTO do Backend (`ProductResponse.java`) para checar os nomes exatos das propriedades. Sub-itens geralmente possuem nomes como `ingredientId`, `productId`, e não apenas `id`. 

---

## 🚫 ANTI-PADRÃO 7 — Array de Objetos vs Array de Strings (IDs)

### O que aconteceu
A tela tentava fazer um `v-model` ou um `.includes()` para checkar se um item estava selecionado usando um array de IDs (`product.removableItemIds`). Porém, a API enviava um array de *objetos* (`product.removableItems = [{ingredientId: "...", name: "..."}]`). O front-end tentava comparar maçãs (Strings) com laranjas (Objetos), causando falha silenciosa de seleção.

### A regra
Quando o Backend enviar uma coleção de objetos, mas a tela precisar apenas dos IDs para controles de formulário (checkboxes, toggles), **crie um mapeamento explícito no `onMounted`** após o *fetch* da API:
```javascript
// ✅ CORRETO:
item.removableItemIds = (item.removableItems || []).map(r => r.ingredientId)
```

---

## 🚫 ANTI-PADRÃO 8 — Case Sensitivity de Enums (Backend vs Frontend)

### O que aconteceu
O backend Java envia os enums em caixa alta (ex: `RADIO`, `CHECKBOX`). O código do frontend esperava comparar contra letras minúsculas (`group.type === 'radio'`). Resultado: `RADIO` nunca será igual a `radio`, e a interface não conseguia religar os modificadores pré-selecionados ao abrir o modal de edição de carrinho.

### A regra
Sempre equalize os cases ao comparar strings vindas do servidor com literais do frontend:
```javascript
// ❌ ERRADO
if (group.type === 'radio') 

// ✅ CORRETO
const type = group.type?.toLowerCase() || 'radio';
if (type === 'radio') 
```

---

## 🚫 ANTI-PADRÃO 9 — Passar Ícones do Lucide como Strings no `<script setup>`

### O que aconteceu
No `Dashboard.vue`, a lista de ícones foi definida como texto: `{ icon: 'DollarSign' }`. Ao usar `<component :is="stat.icon" />`, o Vue 3 procurou por um componente registrado *globalmente* com a string "DollarSign". O componente não apareceu, quebrando o layout do CSS que esperava o ícone para esticar o flex.

### A regra
Com a Composition API e `<script setup>`, os componentes importados localmente não são registrados globalmente por nome. O valor passado para o `:is` deve ser o **objeto importado**, não uma string!
```javascript
import { DollarSign } from 'lucide-vue-next';

// ❌ ERRADO
const config = { icon: 'DollarSign' }

// ✅ CORRETO
const config = { icon: DollarSign }
```
