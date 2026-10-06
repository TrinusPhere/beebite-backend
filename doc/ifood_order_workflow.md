# iFood Order Workflow — Guia de Integração e Ciclo de Vida de Pedidos

Este documento contém a documentação completa do fluxo de pedidos (*Order Workflow*) da API do iFood, incluindo arquitetura de eventos, transições de status, SLA, cancelamentos, payloads e endpoints.

---

## 🏛️ 1. Arquitetura e Comunicação

A API de Pedidos do iFood opera no modelo **Orientado a Eventos** (*Event-Driven Architecture*). O sistema integrador (ERP/PDV) **não deve realizar requisições contínuas de consulta (GET)** no pedido para checar status. Em vez disso, deve receber ou consumir eventos de mudança de estado.

### 🔌 Formas de Consumo
1. **Webhooks (Recomendado)**: O iFood envia uma requisição `POST` HTTP diretamente para um endpoint HTTPS configurado na aplicação a cada evento ocorrido.
2. **Polling**: Consulta periódica ao endpoint `GET /order/v1.0/orders:polling`.
   * **Intervalo recomendado**: A cada 30 segundos.
   * **Retenção de Eventos**: Os eventos ficam disponíveis no polling por até **8 horas**.

### ✉️ Confirmação de Leitura (Acknowledgment)
Após receber e processar os eventos (seja via Polling ou Webhook), o integrador **obrigatoriamente deve confirmar o recebimento** enviando uma requisição `POST` para `/order/v1.0/orders:acknowledgment` contendo o array com os `id`s dos eventos.
* Sem a confirmação, o iFood re-enviará os mesmos eventos nas chamadas subsequentes.

---

## 🔄 2. Ciclo de Vida do Pedido (Lifecycle & Transições)

### Diagrama do Fluxo Principal

```
       [PLACED] (Pedido Realizado no App iFood)
          │
          ▼  (Até 8 minutos — SLA)
     [CONFIRMED] (Aceito pelo Restaurante / ERP)
          │
          ▼
[PREPARATION_STARTED] (Início do Preparo na Cozinha)
          │
     ┌────┴──────────────────────────┐
     ▼                               ▼
[TAKEOUT]                       [DELIVERY]
     │                               │
     ▼                               ├──────────────────────────┐
[READY_FOR_PICKUP]                   ▼                          ▼
     │                      (Logística Própria)        (Logística iFood)
     │                       deliveredBy: MERCHANT      deliveredBy: IFOOD
     │                               │                          │
     │                               ▼                          ▼
     │                          [DISPATCHED]            [ASSIGN_DRIVER]
     │                               │                          │
     │                               │                          ▼
     │                               │                  [GOING_TO_ORIGIN]
     │                               │                          │
     │                               │                          ▼
     │                               │                 [ARRIVED_AT_ORIGIN]
     │                               │                          │
     │                               │                          ▼
     │                               │                     [COLLECTED]
     │                               │                          │
     │                               └───────────┬──────────────┘
     │                                           │
     ▼                                           ▼
 [CONCLUDED] (Retirado)                   [CONCLUDED] (Entregue ao Cliente)
```

---

## ⏱️ 3. SLA de Confirmação (Regra dos 8 Minutos)

* **Prazo Limite**: Todo pedido recebido deve ser confirmado (`CONFIRMED`) em até **8 minutos**.
* **Pedidos Imediatos (`IMMEDIATE`)**: O SLA de 8 minutos começa a contar a partir do campo `createdAt` do pedido.
* **Pedidos Agendados (`SCHEDULED`)**: O SLA de 8 minutos começa a contar a partir de `preparationStartDateTime`.
* **Consequência do Descumprimento**: O iFood realiza o **cancelamento automático** do pedido por falta de confirmação e aplica penalidades no índice de desempenho da loja.

---

## 🚦 4. Detalhamento dos Status e Endpoints

### 🟢 Fluxo Operacional

| Status / Código | Descrição | Endpoint Chamado pelo ERP |
| :--- | :--- | :--- |
| **`PLACED`** | Pedido criado pelo cliente no iFood. | *Nenhum (Gerado pelo iFood)* |
| **`CONFIRMED`** | Pedido aceito e confirmado pelo estabelecimento. | `POST /order/v1.0/orders/{id}/confirm` |
| **`PREPARATION_STARTED`** | Início da separação/preparo dos alimentos. | `POST /order/v1.0/orders/{id}/startPreparation` |
| **`READY_FOR_PICKUP`** | Pedido pronto para retirada pelo cliente (`TAKEOUT`). | `POST /order/v1.0/orders/{id}/readyToPickup` |
| **`DISPATCHED`** | Entregador próprio saiu para entregar (`DELIVERY` + `MERCHANT`). | `POST /order/v1.0/orders/{id}/dispatch` |
| **`CONCLUDED`** | Pedido entregue ou retirado com sucesso. | *Gerado pelo sistema ao finalizar a entrega* |

---

## ❌ 5. Fluxo de Cancelamento (Cancellation Workflow)

O cancelamento de um pedido pode ocorrer por iniciativa do restaurante ou solicitação do cliente.

```
                  [SOLICITAÇÃO DE CANCELAMENTO]
                                │
       ┌────────────────────────┴────────────────────────┐
       ▼                                                 ▼
[Iniciado pelo Restaurante]                     [Iniciado pelo Cliente]
       │                                                 │
       ▼                                                 ▼
Obtém motivos dinâmicos                         Recebe evento:
`GET /cancellationReasons`                      `CANCELLATION_REQUESTED`
       │                                                 │
       ▼                                        ┌────────┴────────┐
Envia solicitação                               ▼                 ▼
`POST /requestCancellation`                 [ACEITAR]         [RECUSAR]
       │                              `/acceptCancellation` `/denyCancellation`
       │                                        │                 │
       └────────────────────────┬───────────────┘                 │
                                ▼                                 ▼
                           [CANCELLED]                [CANCELLATION_REQUEST_FAILED]
```

### Endpoints de Cancelamento:
1. **Listar Motivos Válidos**: `GET /order/v1.0/orders/{id}/cancellationReasons`
   > ⚠️ **Atenção**: Os motivos de cancelamento **não podem ser hardcoded**. É obrigatório buscar dinamicamente os códigos (`code`) válidos retornados por este endpoint.
2. **Solicitar Cancelamento (Restaurante)**: `POST /order/v1.0/orders/{id}/requestCancellation`
   * Body: `{ "reason": "Descrição", "cancellationCode": "501" }`
3. **Aceitar Cancelamento (do Cliente)**: `POST /order/v1.0/orders/{id}/acceptCancellation`
4. **Recusar Cancelamento (do Cliente)**: `POST /order/v1.0/orders/{id}/denyCancellation`

---

## 📦 6. Modelos de Pedido (`orderType`) e Logística (`deliveredBy`)

| Modelo (`orderType`) | Logística (`deliveredBy`) | Responsável pela Entrega | Ação Requerida no ERP |
| :--- | :--- | :--- | :--- |
| `DELIVERY` | `MERCHANT` | Entregador Próprio | Deve chamar `/dispatch` quando o entregador sair. |
| `DELIVERY` | `IFOOD` | Entregador iFood | iFood aloca o motorista automaticamente. **Não** chamar `/dispatch`. |
| `TAKEOUT` | `TAKEOUT` | Cliente (Retirada) | Chamar `/readyToPickup` quando o pedido estiver embalado. |
| `DINE_IN` / `INCLINE` | `NONE` | Consumo no Local | Gerenciar o preparo e servimento no balcão/mesa. |

---

## 📋 7. Estrutura dos Payloads de Eventos (Event Payload)

### Resposta de `GET /order/v1.0/orders:polling`:

```json
{
  "events": [
    {
      "id": "993a4b12-70b1-4f11-9a71-334208a1c920",
      "code": "CONFIRMED",
      "fullCode": "ORDER_CONFIRMED",
      "orderId": "6c45d311-8e99-4d62-a38f-51bc210f98ab",
      "createdAt": "2026-08-21T21:15:00.000Z",
      "metadata": {
        "id": "6c45d311-8e99-4d62-a38f-51bc210f98ab",
        "status": "CONFIRMED",
        "category": "FOOD",
        "orderType": "DELIVERY",
        "deliveredBy": "MERCHANT"
      }
    }
  ]
}
```

### Exemplo de Envio em `POST /order/v1.0/orders:acknowledgment`:

```json
[
  {
    "id": "993a4b12-70b1-4f11-9a71-334208a1c920"
  }
]
```

---

## 💡 8. Boas Práticas de Integração

1. **Chave de Idempotência (`idempotencyKey`)**: Enviar sempre o header HTTP `idempotencyKey` com UUID único ao chamar endpoints de ação (`confirm`, `dispatch`, `requestCancellation`) para evitar duplicação em retentativas.
2. **Tratamento de Erros e Backoff Exponencial**: Em casos de falha temporária ou respostas `404` / `409`, implementar retentativa com backoff exponencial (1s, 2s, 4s, 8s, 16s... até 10min).
3. **Ordenação por Timestamp**: Ordenar sempre os eventos processados utilizando o campo `createdAt` UTC para evitar incoerências caso cheguem fora de ordem.
4. **Persistência Local de Dados**: Os detalhes estendidos do pedido ficam acessíveis por **7 dias** na API do iFood. O sistema ERP deve persistir todos os dados no banco de dados local para relatórios e conciliação financeira.
