# iFood Catalog API — Guia de Gerenciamento de Catálogo (v2.0)

Este documento compila as melhores práticas, fluxos e regras arquiteturais da API de Catálogo do iFood, englobando todas as documentações oficiais recentes.

---

## 🏗️ 1. Fundamentos e Hierarquia (v2.0)

A versão 2.0 da API de Catálogo adota o modelo de **Catálogo Unificado (Multisetup)**. Diferente da v1, onde os restaurantes duplicavam itens para canais diferentes (ex: um para Entrega, outro para Retirada ou Cardápio Digital), a v2.0 trabalha com um único cardápio onde variações de preço e status são geridas por **Contextos**.

### Hierarquia Principal:
1. **Catálogo (`Catalog`)**: Contêiner raiz do cardápio da loja.
2. **Categoria (`Category`)**: Agrupamento lógico (ex: "Bebidas", "Lanches").
3. **Item (`Item`)**: O produto em si.
4. **Grupos de Complementos (`OptionGroup`)**: Sub-agrupamentos dentro de um item (ex: "Escolha seu molho").
5. **Complemento (`Option`)**: As opções selecionáveis dentro do grupo (ex: "Ketchup").

---

## 🔄 2. Fluxo de Integração (Workflow)

Ao integrar ou sincronizar o catálogo do seu PDV para o iFood, o fluxo correto é:

1. **Obter IDs Bases**: `GET /catalog/v2.0/merchants/{merchantId}/catalogs`
2. **Sincronizar Categorias**: `POST /catalog/v2.0/merchants/{merchantId}/categories` (Criar/Atualizar categorias)
3. **Sincronizar Itens**: `PUT /catalog/v2.0/merchants/{merchantId}/items` (Cria e atualiza o item, seus grupos e complementos na mesma requisição).
4. **Atualizar Preço e Status (Operações Leves)**: 
   * `PATCH /catalog/v2.0/merchants/{merchantId}/items/price`
   * `PATCH /catalog/v2.0/merchants/{merchantId}/items/status`
5. **Monitorar Assincronismo**: Muitas chamadas na API de catálogo geram um processamento em lote (*Batch*). O integrador deve usar o `GET /batch/{batchId}` para verificar o status de processamento da alteração.

---

## 🍕 3. Padrões de Itens: Pizza e Combos

A API v2.0 extinguiu o tipo especial `PIZZA` da v1 e remodelou a forma de lidar com itens complexos.

### Pizza (Novo Modelo)
Agora, uma Pizza é enviada como um item do tipo `DEFAULT`. A inteligência da montagem fica nos **OptionGroups (Grupos de Opção)**, que devem obrigatoriamente seguir tags específicas para habilitar a interface de montagem de pizza no app do iFood:
* `SIZE` (Tamanho - ex: Pequena, Grande).
* `CRUST` (Massa - ex: Fina, Pan).
* `EDGE` (Borda - ex: Recheada com Catupiry).
* `TOPPING` (Cobertura/Sabor - ex: Calabresa, Marguerita). Pode aceitar frações (ex: 1/2 a 1/2).

### Combos
Criados com o tipo `COMBO_V2`. Toda a estrutura do combo deve ser montada através de `optionGroups` (ex: Grupo "Escolha o Lanche", Grupo "Escolha a Bebida"). Os componentes do combo não precisam ser itens soltos no catálogo (podem ser estruturados direto nas *Options* do Combo).

---

## ⚙️ 4. Gestão de Modificadores (OptionGroups)

Os *add-ons* (complementos) da v1 agora são **Option Groups**. Eles devem ser classificados por tipo (`optionGroupType`):
* `OFFER_UNIT`: Produtos de venda cruzada (ex: Refrigerante extra, Sobremesa).
* `SPECIFICATION`: Detalhes de preparo (ex: "Ao ponto", "Sem cebola").
* `INGREDIENTS`: Modificação do conteúdo do prato (ex: "Adicional de Bacon").
* `CUTLERY`: Solicitação de talheres.

---

## 🌐 5. Multisetup e ContextModifiers

Se o restaurante tem canais diferentes (ex: Entrega via iFood e Cardápio Digital próprio ou Retirada) e quer praticar **preços diferentes**, ele NÃO deve duplicar o item.

Usa-se o **`contextModifiers`** no Payload do Item:
```json
{
  "item": {
    "id": "123",
    "price": { "value": 25.0 },
    "status": "AVAILABLE",
    "contextModifiers": [
      {
        "catalogContext": "WHITELABEL", 
        "price": { "value": 20.0 }, 
        "status": "AVAILABLE"
      }
    ]
  }
}
```
*Valores no `contextModifiers` sempre sobrescrevem o valor raiz do item para aquele canal específico.*

---

## 📦 6. Disponibilidade (Availability) e Estoque

1. **Status Simples**: O item (ou complemento) pode estar `AVAILABLE` (Visível e comprável) ou `UNAVAILABLE` (Oculto/Pausado).
2. **Estoque (Inventory)**: Usado para limitar a venda de um item com quantidade limitada.
   * `POST /catalog/v2.0/merchants/{merchantId}/inventory`
   * Permite definir a cota máxima vendável para evitar ruptura de estoque (*Overselling*).

---

## 🛑 7. Erros e Homologação (Troubleshooting)

### Códigos de Erro Comuns:
| HTTP | Significado | Ação / Solução |
| :--- | :--- | :--- |
| **400** | Validação | Estrutura de Combo/Pizza incorreta, preço negativo, ou JSON inválido. Valide o payload contra os Schemas da API. |
| **403** | Escopo / Permissão | O token do aplicativo não tem a permissão `catalog` ou a loja não liberou o acesso. |
| **422** | Regra de Negócio | Tentativa de atualizar o preço de um item num contexto não ativado para a loja. |
| **429** | Rate Limit | Excessos de chamadas. Implemente *Exponential Backoff* antes de retentar. |

### Processo de Homologação de Catálogo
* A homologação é unificada com os outros fluxos (Pedidos e Lojas).
* O iFood exige que a sua aplicação integradora seja capaz de **montar e atualizar o cardápio via API**.
* Durante a homologação no portal (usando os dados de uma Loja de Teste), será avaliado se as chamadas de preço e status respeitam os `contextModifiers` (para lojas Multisetup) sem prejudicar a vitrine.
