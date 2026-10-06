# iFood Merchant API — Guia de Gerenciamento de Lojas (Introdução e Workflow)

Este documento centraliza as informações da documentação oficial do iFood sobre a **Merchant API**, cobrindo o cadastro, monitoramento de status da loja, ciclo de vida operacional e gerenciamento de interrupções (pausas).

---

## 🏬 1. Introdução à Merchant API

A **Merchant API** permite que o software integrador (PDV/ERP) consulte as configurações da loja, os modelos logísticos aceitos e controle a disponibilidade do restaurante na plataforma iFood em tempo real.

### Endpoints Básicos de Descoberta:
* **Listar Lojas Vinculadas:**
  `GET /merchant/v1.0/merchants`
  *Retorna a lista de todos os `merchantId`s aos quais o seu aplicativo tem acesso.*
* **Obter Detalhes da Loja:**
  `GET /merchant/v1.0/merchants/{merchantId}`
  *Traz os detalhes de cadastro do restaurante, incluindo as `operations` ativas (ex: `DELIVERY`, `TAKEOUT`) e canais de venda.*

---

## 🚦 2. Status Operacional da Loja (Status Lifecycle)

Para garantir que a loja não receba pedidos quando está incapaz de operar, é obrigatório monitorar o status.

* **Endpoint de Status Geral:**
  `GET /merchant/v1.0/merchants/{merchantId}/status`
* **Endpoint de Status por Operação:**
  `GET /merchant/v1.0/merchants/{merchantId}/status/{operation}` *(ex: operação `DELIVERY`)*

### Possíveis Estados Retornados:

| Status | Significado | Ação / Comportamento |
| :--- | :--- | :--- |
| **`OK`** | Loja Aberta | A loja está operando normalmente e apta a receber novos pedidos. |
| **`WARNING`** | Loja com Restrições | A loja está online, mas possui alertas (ex: entregador atrasado ou configuração pendente). |
| **`CLOSED`** | Loja Fechada | A loja não está recebendo pedidos. Ocorre fora do horário de funcionamento ou devido a uma Interrupção manual (Pausa). |
| **`ERROR`** | Erro Operacional | Problema técnico impeditivo (analise a propriedade `message` no retorno para corrigir). |

> 🕒 **Frequência (Heartbeat)**: O iFood recomenda realizar o polling do endpoint de `/status` a cada **30 segundos**. Este comportamento funciona como um "Heartbeat", indicando para o iFood que o seu sistema PDV está online. Se o PDV parar de se comunicar, a plataforma poderá pausar a loja por segurança (evitando cancelamentos automáticos).

---

## ⏸️ 3. Workflow de Interrupções (Pausas Temporárias)

Interrupções são usadas para fechar temporariamente a loja para novos pedidos (ex: cozinha sobrecarregada, falta de luz, sistema fora do ar). **As interrupções não devem ser usadas para ajustar horários de fechamento permanente.**

### Gerenciar Interrupções:

1. **Criar uma Interrupção (Pausar a Loja)**:
   `POST /merchant/v1.0/merchants/{merchantId}/interruptions`
   * **Body**: Enviar uma descrição clara do motivo (ex: "Falta de energia" ou "Cozinha sobrecarregada").

2. **Listar Interrupções Ativas**:
   `GET /merchant/v1.0/merchants/{merchantId}/interruptions`

3. **Remover uma Interrupção (Reabrir a Loja)**:
   `DELETE /merchant/v1.0/merchants/{merchantId}/interruptions/{interruptionId}`
   * Restaura o status da loja para `OK` (desde que esteja dentro do horário de funcionamento configurado).

### Boas Práticas com Interrupções:
* **Não crie interrupções sobrepostas.**
* **Remova a pausa imediatamente** assim que o problema operacional for resolvido.
* Não utilize interrupções com durações superiores a **7 dias**.

---

## 🕒 4. Gerenciamento de Horários (Opening Hours)

Para alterações permanentes nos horários de funcionamento, o fluxo correto é através dos endpoints de `opening-hours`, não de `interruptions`.

* **Endpoint**: `PUT /merchant/v1.0/merchants/{merchantId}/opening-hours`
* **Importante**: Ao enviar uma requisição `PUT`, você deve enviar a **grade semanal completa** de horários, pois este comando substitui toda a configuração anterior da loja.

---

## 🛡️ 5. Salvaguardas e Limites (Rate Limits)

1. **Taxa de Erro**: O iFood bloqueia integrações cuja taxa de chamadas resultando em erros (HTTP 4xx e 5xx) seja maior que **2%** (e $\le$ **1%** para endpoints de status/eventos). Se você receber erro `5xx`, implemente *Exponential Backoff*.
2. **Rate Limit 429**: Respeite o polling de 30 segundos no `GET /status`. Abusar da API causará bloqueios via HTTP `429 Too Many Requests`.
3. **Reabertura Bloqueada**: Se o status retornar `ERROR` e o campo `reopenable` for `false`, **não tente forçar a reabertura** (isso geralmente indica suspensão manual pelo time do iFood por excesso de cancelamentos ou penalidades).

---

## 🍽️ 6. Gerenciamento de Operações (`DELIVERY` e `TAKEOUT`)

Uma "Operação" define como a loja entrega o pedido ao cliente. Elas são gerenciadas de forma independente, permitindo horários e status distintos.

* **DELIVERY (Entrega)**: Pedido entregue no endereço do cliente.
* **TAKEOUT (Retirada)**: O cliente busca o pedido na loja (pode ser configurado como balcão `DEFAULT` ou drive-thru `PICKUP_AREA`).
  * ⚠️ **Atenção**: Para pedidos `TAKEOUT`, o sistema **não** deve chamar o endpoint de `/dispatch` (despacho), mas sim o endpoint de `/readyToPickup` assim que o pedido estiver pronto para o cliente buscar.

Para gerenciar operações em lote (múltiplos merchants), é possível usar o header `x-polling-merchants` quando for necessário gerenciar grandes volumes.

---

## 🛑 7. Troubleshooting de Erros Comuns (Merchant API)

| Código | Causa Comum | Ação Recomendada |
| :--- | :--- | :--- |
| **400 Bad Request** | Faltam campos obrigatórios ou o payload é inválido (ex: formato JSON incorreto). | Verifique a validação do body da requisição conforme a doc. |
| **401 Unauthorized** | Token expirado ou ausente. | Faça o *refresh* do token e tente novamente. |
| **403 Forbidden** | Loja não autorizou seu app ou falta de escopo. | Confirme no Portal do Parceiro se o lojista concedeu permissão. |
| **404 Not Found** | Recurso inexistente (ex: pedido ou loja). | Pode ocorrer se você tentar consultar um pedido recém-criado antes dele propagar completamente. Use *Exponential Backoff* (até 10min) para tentar novamente. |
| **429 Too Many Requests**| Você excedeu a cota de acessos por segundo. | Implemente/Aumente o *Exponential Backoff* nas retentativas. Evite loops de polling com menos de 30s. |

---

## 🧪 8. Homologação e Ambiente de Testes (Sandbox)

Diferente de outras APIs que possuem uma URL dedicada para Sandbox, no iFood a homologação ocorre **na mesma URL de produção** (`https://merchant-api.ifood.com.br/`), mas utilizando **credenciais de teste**.

1. **Loja de Teste**: Ao criar uma aplicação no [iFood Developer Portal](https://developer.ifood.com.br/), você recebe credenciais atreladas a uma loja fictícia (Loja de Teste).
2. **Realização de Testes**: Para testar, você deve fazer pedidos na loja de teste usando o App/Site do iFood e validar se o seu sistema recebe os eventos, confirma os pedidos em menos de 8 minutos, e processa cancelamentos corretamente.
3. **Processo de Homologação**:
   * Não é permitido integrar em produção sem passar pela homologação.
   * O iFood exige CNPJ e avaliará o seu produto funcional completo (como seu PDV processa o fluxo de ponta a ponta).
   * Você deve solicitar a homologação via chamado no portal do desenvolvedor antes de liberar a integração para restaurantes reais.
