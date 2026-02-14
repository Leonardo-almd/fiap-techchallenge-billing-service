# ADR-002: AWS SQS para Messaging

## Status

**Aceita** - 2024-02-01

## Contexto

O Billing Service precisa de comunicação assíncrona para:

- **Consumir** eventos de ordem de serviço criada (ORDER_CREATED) publicados pelo OS Service, para criar orçamento automaticamente
- **Publicar** eventos de orçamento (BudgetApproved, BudgetRejected) e pagamento (PaymentProcessed, PaymentFailed, PaymentRefunded) para outros serviços (OS Service, Execution Service)
- Garantir entrega confiável e suportar a coreografia do Saga Pattern
- Manter paridade com a infraestrutura AWS (EKS) e com o OS Service

## Decisão

Adotamos **AWS SQS (Simple Queue Service)** como message broker, utilizando o **AWS SDK for Java v2** (SqsClient) para integração, com filas FIFO onde necessário para ordenação e deduplicação.

### Filas Configuradas

| Fila | Direção | Propósito |
|------|---------|-----------|
| `os-order-events-queue.fifo` | Inbound | Consumir ORDER_CREATED (OS Service publica) |
| `billing-events.fifo` | Outbound | Publicar BudgetApproved, BudgetRejected, PaymentProcessed, PaymentFailed, PaymentRefunded |

### Implementação

- **Consumer**: `ServiceOrderEventConsumer` — poll a cada 5 segundos (`@Scheduled`), deserializa para `ServiceOrderCreatedEvent`, chama `CreateBudgetUseCase`, deleta mensagem após sucesso
- **Publisher**: `SqsEventPublisher` — publica eventos de orçamento e pagamento na fila `billing-events.fifo` (configurável por nome; a aplicação resolve a URL via `GetQueueUrl`)

### Configuração

As filas são configuradas por **nome** via variáveis de ambiente (ConfigMap no K8s), preenchidas pelo CD a partir dos outputs do Terraform (infra-database):

- `SQS_QUEUE_SERVICE_ORDER_EVENTS` → `aws.sqs.queues.service-order-events` (consumo)
- `SQS_QUEUE_BILLING_EVENTS` → `aws.sqs.queues.billing-events` (publicação)

Ver `docs/QUEUE_CONTRACT.md` para payloads e contratos.

## Consequências

### Positivas

- ✅ **Integração com OS Service**: Mesma fila de saída do OS (`os-order-events-queue.fifo`) para ORDER_CREATED
- ✅ **Gerenciado**: Sem broker próprio; infra no Terraform (infra-database)
- ✅ **Escalável**: SQS suporta alto throughput
- ✅ **Durável**: Mensagens persistidas; DLQ para falhas
- ✅ **IRSA**: Pods no EKS usam IAM Roles for Service Accounts (sem credenciais estáticas)
- ✅ **Alinhamento**: Mesmo padrão de messaging do ecossistema

### Negativas

- ❌ **Vendor Lock-in**: Acoplamento com AWS
- ❌ **Desenvolvimento Local**: Requer LocalStack ou mock
- ❌ **Contrato ORDER_CREATED**: Payload do OS (orderId Long, sem items) ainda não alinhado ao esperado pelo Billing (serviceOrderId String, items); ver CHECKLIST-DEPLOY e QUEUE_CONTRACT

## Alternativas Consideradas

### 1. RabbitMQ

**Prós**: Open source, desenvolvimento local simples

**Contras**: Infraestrutura adicional; ecossistema já padronizado em SQS (OS Service, infra-database)

**Decisão**: Rejeitado para manter consistência

### 2. Apache Kafka

**Prós**: Alta throughput, replay

**Contras**: Complexidade operacional; SQS atende o caso de uso atual

**Decisão**: Rejeitado

### 3. Chamadas HTTP síncronas (OS → Billing)

**Prós**: Contrato explícito, sem fila

**Contras**: Acoplamento síncrono, menor resiliência; Saga do ecossistema é assíncrona

**Decisão**: Rejeitado — eventos assíncronos são a escolha do desenho

## Referências

- [AWS SQS Documentation](https://docs.aws.amazon.com/sqs/)
- [AWS SDK for Java 2.x - SQS](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-sqs.html)
- `docs/QUEUE_CONTRACT.md`
- OS Service: [ADR-002 AWS SQS Messaging](https://github.com/.../ADR-002-aws-sqs-messaging.md)
