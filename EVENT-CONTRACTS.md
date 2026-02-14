# 📨 Event Contracts - Car Garage Microservices

**Contratos de Eventos para Comunicação entre Microsserviços**

Tech Challenge 12SOAT - Fase 4  
Data: 12 de Fevereiro de 2026

---

## 🎯 Objetivo

Este documento define os **contratos de eventos** trocados entre os microsserviços para implementação do **Saga Pattern**.

**Microsserviços:**
1. **OS Service** (Pessoa 1) - Ordem de Serviço
2. **Billing Service** (Pessoa 2) - Orçamento e Pagamento
3. **Execution Service** (Pessoa 3) - Execução e Produção

**Mensageria:** AWS SQS (FIFO queues)

---

## 🔄 Fluxo do Saga

```
[OS Service] → ServiceOrderCreatedEvent → [Billing Service]
                                                    ↓
                                         CreateBudget (PENDING)
                                                    ↓
                                         (Admin aprova manualmente)
                                                    ↓
[OS Service] ← BudgetApprovedEvent ←────────────────┘
    ↓
(Cliente solicita pagamento)
    ↓
[Billing Service] → ProcessPayment
    ↓
[Billing Service] → PaymentProcessedEvent → [OS/Execution Service]
                                                    ↓
                                         [Execution Service] inicia execução
                                                    ↓
                                         ExecutionCompletedEvent
                                                    ↓
                                         [OS Service] finaliza OS

Fluxo Compensação (falha):
[Billing Service] → PaymentFailedEvent → [OS Service]
                                              ↓
                                         Reverter status
```

---

## 📤 Eventos do OS Service (Pessoa 1)

### 1. ServiceOrderCreatedEvent

**Publicado por:** OS Service  
**Consumido por:** Billing Service  
**Fila:** `os-service-events.fifo`  
**Trigger:** Nova OS criada

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "ServiceOrderCreated",
  "timestamp": "2026-02-12T10:30:00Z",
  "serviceOrderId": "OS-12345",
  "customerId": "CUST-001",
  "vehicleId": "VEH-001",
  "vehiclePlate": "ABC-1234",
  "description": "Troca de óleo e revisão",
  "items": [
    {
      "type": "LABOR",
      "description": "Troca de óleo",
      "quantity": 1,
      "unitPrice": 80.00
    },
    {
      "type": "PART",
      "description": "Óleo sintético 5W30",
      "quantity": 4,
      "unitPrice": 45.00
    }
  ],
  "status": "RECEIVED"
}
```

**Ação no Billing:**
- Criar budget automaticamente
- Status: PENDING_APPROVAL

---

### 2. ServiceOrderCancelledEvent (Compensação)

**Publicado por:** OS Service  
**Consumido por:** Billing Service, Execution Service  
**Fila:** `os-service-events.fifo`  
**Trigger:** OS cancelada pelo cliente/sistema

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "ServiceOrderCancelled",
  "timestamp": "2026-02-12T10:35:00Z",
  "serviceOrderId": "OS-12345",
  "cancellationReason": "Cliente desistiu",
  "cancelledAt": "2026-02-12T10:35:00Z"
}
```

**Ação no Billing:**
- Cancelar budget pendente
- Estornar pagamento (se já pago)

---

## 📤 Eventos do Billing Service (Pessoa 2 - VOCÊ)

### 3. BudgetApprovedEvent

**Publicado por:** Billing Service  
**Consumido por:** OS Service  
**Fila:** `billing-service-events.fifo`  
**Trigger:** Orçamento aprovado pelo admin

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "BudgetApproved",
  "timestamp": "2026-02-12T11:00:00Z",
  "budgetId": "BUDGET-12345",
  "serviceOrderId": "OS-12345",
  "customerId": "CUST-001",
  "vehicleId": "VEH-001",
  "totalAmount": 260.00,
  "approvedAt": "2026-02-12T11:00:00Z"
}
```

**Ação no OS:**
- Atualizar status para WAITING_PAYMENT
- Notificar cliente

---

### 4. BudgetRejectedEvent

**Publicado por:** Billing Service  
**Consumido por:** OS Service  
**Fila:** `billing-service-events.fifo`  
**Trigger:** Orçamento rejeitado pelo admin

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "BudgetRejected",
  "timestamp": "2026-02-12T11:05:00Z",
  "budgetId": "BUDGET-12345",
  "serviceOrderId": "OS-12345",
  "rejectionReason": "Valor muito alto",
  "rejectedAt": "2026-02-12T11:05:00Z"
}
```

**Ação no OS:**
- Atualizar status
- Notificar cliente
- Permitir nova tentativa

---

### 5. PaymentProcessedEvent

**Publicado por:** Billing Service  
**Consumido por:** OS Service, Execution Service  
**Fila:** `billing-service-events.fifo`  
**Trigger:** Pagamento processado com sucesso

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "PaymentProcessed",
  "timestamp": "2026-02-12T11:15:00Z",
  "paymentId": "PAY-12345",
  "budgetId": "BUDGET-12345",
  "serviceOrderId": "OS-12345",
  "amount": 260.00,
  "method": "CREDIT_CARD",
  "externalId": "EXT-987654",
  "authorizationCode": "AUTH-ABC123",
  "processedAt": "2026-02-12T11:15:00Z"
}
```

**Ação no OS:**
- Atualizar status para IN_EXECUTION

**Ação no Execution:**
- Adicionar OS na fila de execução

---

### 6. PaymentFailedEvent (Compensação)

**Publicado por:** Billing Service  
**Consumido por:** OS Service  
**Fila:** `billing-service-events.fifo`  
**Trigger:** Falha no processamento do pagamento

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "PaymentFailed",
  "timestamp": "2026-02-12T11:20:00Z",
  "paymentId": "PAY-12345",
  "budgetId": "BUDGET-12345",
  "serviceOrderId": "OS-12345",
  "amount": 260.00,
  "failureReason": "Cartão recusado - saldo insuficiente",
  "failedAt": "2026-02-12T11:20:00Z"
}
```

**Ação no OS (Compensação):**
- Reverter status para WAITING_PAYMENT
- Notificar cliente
- Permitir nova tentativa

---

### 7. PaymentRefundedEvent (Compensação)

**Publicado por:** Billing Service  
**Consumido por:** OS Service  
**Fila:** `billing-service-events.fifo`  
**Trigger:** Estorno de pagamento (falha em etapa posterior)

**Schema:**
```json
{
  "eventId": "string (UUID)",
  "eventType": "PaymentRefunded",
  "timestamp": "2026-02-12T12:00:00Z",
  "paymentId": "PAY-12345",
  "budgetId": "BUDGET-12345",
  "serviceOrderId": "OS-12345",
  "amount": 260.00,
  "refundReason": "Falha na execução do serviço",
  "refundedAt": "2026-02-12T12:00:00Z"
}
```

**Ação no OS:**
- Atualizar status
- Notificar cliente sobre estorno

---

## 📤 Eventos do Execution Service (Pessoa 3)

### 8. ExecutionStartedEvent

**Publicado por:** Execution Service  
**Consumido por:** OS Service  
**Fila:** `execution-service-events.fifo`  
**Trigger:** Execução iniciada

**Schema (proposta):**
```json
{
  "eventId": "string (UUID)",
  "eventType": "ExecutionStarted",
  "timestamp": "2026-02-12T11:20:00Z",
  "executionId": "EXEC-12345",
  "serviceOrderId": "OS-12345",
  "estimatedCompletionTime": "2026-02-12T15:00:00Z",
  "startedAt": "2026-02-12T11:20:00Z"
}
```

**Ação no OS:**
- Atualizar status para IN_EXECUTION

---

### 9. ExecutionCompletedEvent

**Publicado por:** Execution Service  
**Consumido por:** OS Service  
**Fila:** `execution-service-events.fifo`  
**Trigger:** Execução finalizada

**Schema (proposta):**
```json
{
  "eventId": "string (UUID)",
  "eventType": "ExecutionCompleted",
  "timestamp": "2026-02-12T14:30:00Z",
  "executionId": "EXEC-12345",
  "serviceOrderId": "OS-12345",
  "completedAt": "2026-02-12T14:30:00Z",
  "notes": "Serviço executado conforme orçamento"
}
```

**Ação no OS:**
- Atualizar status para FINISHED
- Notificar cliente

---

### 10. ExecutionFailedEvent (Compensação)

**Publicado por:** Execution Service  
**Consumido por:** OS Service, Billing Service  
**Fila:** `execution-service-events.fifo`  
**Trigger:** Falha na execução

**Schema (proposta):**
```json
{
  "eventId": "string (UUID)",
  "eventType": "ExecutionFailed",
  "timestamp": "2026-02-12T12:00:00Z",
  "executionId": "EXEC-12345",
  "serviceOrderId": "OS-12345",
  "failureReason": "Peça não disponível",
  "failedAt": "2026-02-12T12:00:00Z"
}
```

**Ação no Billing (Compensação):**
- Estornar pagamento
- Publicar `PaymentRefundedEvent`

**Ação no OS:**
- Reverter status
- Notificar cliente

---

## 🔄 Matriz de Eventos

| Evento | Origem | Destino | Fila | Tipo |
|--------|--------|---------|------|------|
| `ServiceOrderCreatedEvent` | OS | Billing | `os-service-events.fifo` | Normal |
| `ServiceOrderCancelledEvent` | OS | Billing, Execution | `os-service-events.fifo` | Compensação |
| `BudgetApprovedEvent` | Billing | OS | `billing-service-events.fifo` | Normal |
| `BudgetRejectedEvent` | Billing | OS | `billing-service-events.fifo` | Normal |
| `PaymentProcessedEvent` | Billing | OS, Execution | `billing-service-events.fifo` | Normal |
| `PaymentFailedEvent` | Billing | OS | `billing-service-events.fifo` | Compensação |
| `PaymentRefundedEvent` | Billing | OS | `billing-service-events.fifo` | Compensação |
| `ExecutionStartedEvent` | Execution | OS | `execution-service-events.fifo` | Normal |
| `ExecutionCompletedEvent` | Execution | OS | `execution-service-events.fifo` | Normal |
| `ExecutionFailedEvent` | Execution | OS, Billing | `execution-service-events.fifo` | Compensação |

---

## 🏗️ Filas SQS (AWS)

### Estrutura Proposta

```
# OS Service
os-service-events.fifo
os-service-events_dlq.fifo

# Billing Service (Pessoa 2)
billing-service-events.fifo
billing-service-events_dlq.fifo

# Execution Service (Pessoa 3)
execution-service-events.fifo
execution-service-events_dlq.fifo
```

**Total:** 6 filas (3 main + 3 DLQ)

### Configuração SQS

```hcl
# Exemplo Terraform
resource "aws_sqs_queue" "billing_service_events" {
  name                        = "billing-service-events.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  
  message_retention_seconds   = 1209600  # 14 dias
  visibility_timeout_seconds  = 300       # 5 minutos
  receive_wait_time_seconds   = 5         # Long polling
  
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.billing_service_events_dlq.arn
    maxReceiveCount     = 3
  })
}
```

---

## 📏 Padrões de Eventos

### Estrutura Base (Todos os Eventos)

```json
{
  "eventId": "string (UUID v4)",
  "eventType": "string (PascalCase)",
  "timestamp": "string (ISO 8601)",
  "serviceOrderId": "string (prefixo OS-)",
  // ... campos específicos do evento
}
```

### Convenções

**Event IDs:**
- Formato: UUID v4
- Exemplo: `550e8400-e29b-41d4-a716-446655440000`

**Event Types:**
- PascalCase: `ServiceOrderCreated`, `BudgetApproved`, etc.
- Sufixo: `Event` (opcional)

**Timestamps:**
- Formato: ISO 8601 com timezone
- Exemplo: `2026-02-12T10:30:00Z`

**Entity IDs:**
- Prefixos:
  - OS: `OS-`
  - Budget: `BUDGET-`
  - Payment: `PAY-`
  - Execution: `EXEC-`
  - Customer: `CUST-`
  - Vehicle: `VEH-`

---

## 🛡️ Tratamento de Erros

### Retry Policy

- **MaxReceiveCount:** 3 tentativas
- **Backoff:** Exponencial (SQS automático)
- **Dead Letter Queue:** Após 3 falhas

### Idempotência

- Todos os eventos DEVEM ser idempotentes
- Uso de `eventId` para deduplicação
- Content-based deduplication (SQS FIFO)

### Monitoramento

- Métricas CloudWatch:
  - `ApproximateNumberOfMessagesVisible`
  - `ApproximateNumberOfMessagesNotVisible`
  - `ApproximateAgeOfOldestMessage`
- Alarmes para DLQ (> 0 mensagens)

---

## 📝 Implementação

### Consumer (Exemplo - Billing Service)

```java
@Component
@Slf4j
public class ServiceOrderEventConsumer {
    
    @Scheduled(fixedDelay = 5000)
    public void consumeEvents() {
        var request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(5)
            .build();
        
        var response = sqsClient.receiveMessage(request);
        
        for (var message : response.messages()) {
            try {
                var event = objectMapper.readValue(
                    message.body(), 
                    ServiceOrderCreatedEvent.class
                );
                
                // Processar evento (idempotente!)
                processEvent(event);
                
                // Deletar mensagem após sucesso
                deleteMessage(message.receiptHandle());
                
            } catch (Exception e) {
                log.error("Error processing message", e);
                // Mensagem vai para DLQ após 3 tentativas
            }
        }
    }
}
```

### Publisher (Exemplo - Billing Service)

```java
@Component
@Slf4j
public class SqsEventPublisher {
    
    public <T> void publishEvent(T event) {
        try {
            var messageBody = objectMapper.writeValueAsString(event);
            
            var request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageGroupId(event.getClass().getSimpleName())
                .messageDeduplicationId(generateDeduplicationId(event))
                .build();
            
            sqsClient.sendMessage(request);
            
        } catch (Exception e) {
            throw new MessagingException("Error publishing event", e);
        }
    }
}
```

---

## ✅ Checklist de Alinhamento

- [ ] Todos os serviços usam mesma nomenclatura de filas
- [ ] Schemas de eventos acordados
- [ ] Prefixos de IDs padronizados
- [ ] Retry policies alinhadas
- [ ] DLQs configuradas
- [ ] Idempotência garantida
- [ ] Monitoramento configurado
- [ ] Terraform das filas no `infra-kubernetes`

---

## 🔗 Referências

- [AWS SQS FIFO](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues.html)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Event-Driven Architecture](https://aws.amazon.com/event-driven-architecture/)

---

**Status:** PROPOSTA - Aguardando revisão do grupo  
**Próximo passo:** Reunião de alinhamento com Pessoa 1 e 3
