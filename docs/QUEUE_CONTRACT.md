# Contrato de Filas SQS - Billing Service

Este documento descreve os contratos **reais** usados pela aplicação para publicação e consumo de mensagens SQS, alinhado ao padrão do OS Service e à infraestrutura existente.

## Configuração

As filas são configuradas via variáveis de ambiente (ConfigMap/Secrets no K8s):

- **Consumo (inbound):** `SQS_QUEUE_SERVICE_ORDER_EVENTS` – fila onde o Billing Service consome eventos de OS criada
- **Publicação (outbound):** `SQS_QUEUE_BILLING_EVENTS` – fila FIFO de eventos (Saga tracking / Execution Service)
- **Publicação (outbound):** `SQS_QUEUE_QUOTE_APPROVED` – fila standard para o OS Service (orçamento aprovado)
- **Publicação (outbound):** `SQS_QUEUE_PAYMENT_FAILED` – fila standard para o OS Service (compensação)

No `application.yml` (perfil production) essas variáveis mapeiam para:

- `aws.sqs.queues.service-order-events`
- `aws.sqs.queues.billing-events`

## Fila de entrada (consumo)

### Nome configurável (ex.: `os-order-events-queue.fifo`)

Consumida por `ServiceOrderEventConsumer` (poll a cada 5 segundos).

**Propósito:** Receber evento de ordem de serviço criada (ORDER_CREATED) publicada pelo **OS Service**, para criar orçamento automaticamente no Billing.

### Payload esperado (ServiceOrderCreatedEvent)

Para integração com o OS Service, o payload deve ser compatível com o que o OS publica ou com um adapter. Campos usados pelo Billing:

- `serviceOrderId` (String) – obrigatório
- `customerId` (String)
- `vehicleId` (String)
- `items` (Array) – obrigatório para montar o orçamento
  - `type` (String): **SERVICE** | **RESOURCE**
  - `itemCode` (String)
  - `description` (String)
  - `quantity` (Integer)
  - `unitPrice` (Number)

Exemplo mínimo esperado pelo Billing:

```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "serviceOrderId": "123",
  "customerId": "456",
  "vehicleId": "789",
  "items": [
    {
      "type": "SERVICE",
      "itemCode": "301",
      "description": "Troca de óleo",
      "quantity": 1,
      "unitPrice": 80.00
    },
    {
      "type": "RESOURCE",
      "itemCode": "401",
      "description": "Filtro de óleo",
      "quantity": 1,
      "unitPrice": 45.00
    }
  ],
  "totalPrice": 125.00,
  "timestamp": "2026-02-12T10:00:00"
}
```

> **Nota:** O OS Service publica `ORDER_CREATED` na fila standard `service-order-events` com payload enriquecido (itens, totalPrice, serviceOrderId como String). O consumer do Billing parseia os itens e cria o Budget automaticamente.

## Filas de saída (publicação)

### Nome configurável (ex.: `billing-events.fifo`)

Publicada por `SqsEventPublisher` / orquestradores.

**Propósito:** Notificar outros serviços (OS Service, Execution Service) sobre eventos de orçamento e pagamento.

### Eventos publicados

| Evento                  | Quando                         | Consumidor principal |
|-------------------------|--------------------------------|----------------------|
| BudgetApprovedEvent     | Orçamento aprovado            | OS Service (quote-approved) |
| BudgetRejectedEvent     | Orçamento rejeitado           | OS Service           |
| PaymentProcessedEvent   | Pagamento processado com sucesso | OS / Execution   |
| PaymentFailedEvent      | Falha no pagamento            | OS Service (payment-failed) |
| PaymentRefundedEvent    | Estorno realizado             | OS Service           |

### Integração com a infraestrutura do OS Service

As filas standard consumidas pelo OS Service já estão integradas:

- **`quote-approved-queue`** — `BudgetEventOrchestrator` publica `{ orderId, budgetId, totalAmount }` ao aprovar Budget
- **`payment-failed-queue`** — `PaymentProcessingOrchestrator` publica `{ orderId, reason }` ao detectar pagamento falho

Ambos os orquestradores publicam também na fila FIFO `billing-events.fifo` para tracking/auditoria.

### Exemplo de payload (BudgetApprovedEvent)

```json
{
  "eventId": "uuid",
  "eventType": "BudgetApproved",
  "budgetId": "BUDGET-001",
  "serviceOrderId": "123",
  "customerId": "456",
  "vehicleId": "789",
  "totalAmount": 260.00,
  "approvedAt": "2026-02-12T11:00:00",
  "timestamp": "2026-02-12T11:00:00"
}
```

Para o OS Service consumir como “quote approved”, o corpo na fila `quote-approved-queue` deve conter pelo menos:

```json
{ "orderId": 123 }
```

## Resumo – alinhamento com a infraestrutura

| Fila (infra existente)     | Uso no Billing | Uso no OS Service      |
|----------------------------|----------------|-------------------------|
| `os-order-events-queue.fifo` | **Consumir** (ORDER_CREATED) | Publicar                |
| `quote-approved-queue`    | **Publicar** (BudgetApproved) | Consumir               |
| `payment-failed-queue`    | **Publicar** (PaymentFailed) | Consumir               |
| `billing-events.fifo`     | **Publicar** (demais eventos) | —                      |

As filas `billing-events.fifo` e, se necessário, DLQs para o Billing podem ser criadas no Terraform do Billing ou integradas ao infra-database (ver DEPLOY_SETUP.md).
