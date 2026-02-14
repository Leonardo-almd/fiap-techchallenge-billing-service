# Contrato de Filas SQS - Billing Service

Este documento descreve os contratos **reais** usados pela aplicação para publicação e consumo de mensagens SQS, alinhado ao padrão do OS Service e à infraestrutura existente.

## Configuração

As filas são configuradas via variáveis de ambiente (ConfigMap/Secrets no K8s):

- **Consumo (inbound):** `SQS_QUEUE_SERVICE_ORDER_EVENTS` – fila onde o Billing Service consome eventos de OS criada
- **Publicação (outbound):** `SQS_QUEUE_BILLING_EVENTS` – fila onde o Billing Service publica eventos (orçamento aprovado/rejeitado, pagamento processado/falhou, estorno)

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
  - `type` (String): LABOR | PART
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
      "type": "LABOR",
      "itemCode": "SVC-001",
      "description": "Troca de óleo",
      "quantity": 1,
      "unitPrice": 80.00
    }
  ],
  "timestamp": "2026-02-12T10:00:00"
}
```

**Nota:** O OS Service atualmente publica `orderId` (Long) e não envia `items`. Para integração direta, é necessário alinhar o contrato (ver ANALISE-OS-SERVICE-E-INFRA.md) ou implementar um adapter.

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

Na infra existente (infra-database) estão definidas as filas **Standard** que o OS Service **consome**:

- `quote-approved-queue` – OS atualiza status para IN_EXECUTION
- `payment-failed-queue` – OS executa compensação (cancelar)

Para o Billing integrar com o OS Service:

- **BudgetApprovedEvent** deve ser publicado na fila `quote-approved-queue` (payload com `orderId` numérico, conforme QUEUE_CONTRACT do OS Service).
- **PaymentFailedEvent** deve ser publicado na fila `payment-failed-queue` (payload com `orderId` e opcionalmente `reason`).

Atualmente o Billing publica todos os eventos em uma única fila (`billing-events`). A extensão para publicar em `quote-approved-queue` e `payment-failed-queue` pode ser feita no publisher/orchestrator (ver EVENT-CONTRACTS.md e ANALISE-OS-SERVICE-E-INFRA.md).

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
