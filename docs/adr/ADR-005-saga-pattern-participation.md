# ADR-005: Participação no Saga Pattern

## Status

**Aceita** - 2024-02-05

## Contexto

O Billing Service participa do fluxo de **Ordem de Serviço** junto com o OS Service e outros microserviços. O ecossistema adota o **Saga Pattern com Coreografia** (eventos assíncronos via SQS). Precisamos:

- Definir o papel do Billing na coreografia
- Consumir eventos que disparam criação de orçamento
- Publicar eventos que permitam ao OS (e outros) avançar ou compensar
- Garantir que falhas em pagamento/orçamento possam ser tratadas (compensação)

## Decisão

O Billing Service atua como **participante da coreografia**: consome eventos publicados pelo OS Service e publica eventos que outros serviços consomem, sem orquestrador central.

### Fluxo de Entrada (Billing consome)

| Evento | Fila | Origem | Ação do Billing |
|--------|------|--------|------------------|
| ORDER_CREATED | `os-order-events-queue.fifo` | OS Service | Criar orçamento automaticamente (`CreateBudgetUseCase`) e persistir no DynamoDB |

**Requisito de contrato**: O payload deve conter `serviceOrderId`, `customerId`, `vehicleId` e `items[]` para cálculo correto do orçamento. Hoje o OS publica `orderId` (Long) e sem `items` — alinhamento de contrato é necessário para integração ponta a ponta (ver CHECKLIST-DEPLOY e QUEUE_CONTRACT).

### Fluxo de Saída (Billing publica)

| Evento | Fila | Consumidor principal | Quando |
|--------|------|---------------------|--------|
| BudgetApprovedEvent | `billing-events.fifo` | OS Service (quote-approved) | Cliente aprova orçamento |
| BudgetRejectedEvent | `billing-events.fifo` | OS Service | Cliente rejeita orçamento |
| PaymentProcessedEvent | `billing-events.fifo` | OS / Execution | Pagamento processado com sucesso |
| PaymentFailedEvent | `billing-events.fifo` | OS Service (compensação) | Falha no pagamento |
| PaymentRefundedEvent | `billing-events.fifo` | OS Service | Estorno realizado |

### Orquestração interna

- **BudgetEventOrchestrator** / **PaymentProcessingOrchestrator**: coordenam use cases e publicação de eventos (aprovar orçamento → publicar BudgetApproved; processar pagamento → publicar PaymentProcessed ou PaymentFailed).

### Compensação

- O Billing **publica** PaymentFailedEvent e PaymentRefundedEvent; o OS Service (ou outro participante) é responsável por reagir (ex.: retornar OS para IN_DIAGNOSIS).
- O Billing não executa compensação de outros serviços; apenas emite eventos que sinalizam falha ou estorno.

## Consequências

### Positivas

- ✅ **Desacoplamento**: Billing não chama outros serviços via HTTP para o fluxo principal; apenas consome/publica eventos
- ✅ **Resiliência**: Falhas em um serviço não bloqueiam o Billing de processar mensagens da fila
- ✅ **Alinhamento**: Mesma coreografia do OS Service (ADR-005)
- ✅ **Auditabilidade**: Eventos na fila servem como registro do fluxo

### Negativas

- ❌ **Consistência eventual**: Janela de inconsistência entre serviços
- ❌ **Contrato ORDER_CREATED**: Até alinhamento de payload (OS ou adapter no Billing), o fluxo “OS criada → Billing cria orçamento” pode falhar ou gerar orçamento incompleto
- ❌ **Idempotência**: Handlers devem ser idempotentes (ex.: criar orçamento por serviceOrderId já existente)

## Alternativas Consideradas

### 1. Orquestração centralizada (Billing como orquestrador)

**Prós**: Fluxo explícito em um lugar

**Contras**: Ponto único de falha; ecossistema já definido como coreografia

**Decisão**: Rejeitado

### 2. Chamadas HTTP síncronas (OS chama Billing para criar orçamento)

**Prós**: Contrato simples

**Contras**: Acoplamento síncrono; menos resiliente que eventos

**Decisão**: Rejeitado — aderência ao Saga assíncrono

### 3. Billing não publicar eventos (apenas persistir)

**Prós**: Menos integração

**Contras**: OS e outros não saberiam de aprovação/falha de pagamento; Saga não completaria

**Decisão**: Rejeitado

## Referências

- [Saga Pattern - Microsoft](https://docs.microsoft.com/en-us/azure/architecture/reference-architectures/saga/saga)
- [Microservices Patterns - Saga](https://microservices.io/patterns/data/saga.html)
- `docs/QUEUE_CONTRACT.md`, `EVENT-CONTRACTS.md`
- OS Service: [ADR-005 Saga Pattern](https://github.com/.../ADR-005-saga-pattern.md)
