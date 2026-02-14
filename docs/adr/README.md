# Architecture Decision Records (ADRs)

Este diretório contém os Architecture Decision Records (ADRs) do microserviço Billing Service.

## O que são ADRs?

ADRs são documentos curtos que capturam decisões arquiteturais importantes junto com seu contexto e consequências. Eles ajudam a:

- Documentar decisões para referência futura
- Comunicar decisões para novos membros da equipe
- Fornecer histórico sobre por que certas escolhas foram feitas
- Evitar retrabalho em discussões já resolvidas

## Índice de ADRs

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-001](ADR-001-clean-architecture.md) | Clean Architecture | Aceita | 2024-01-15 |
| [ADR-002](ADR-002-aws-sqs-messaging.md) | AWS SQS para Messaging | Aceita | 2024-02-01 |
| [ADR-003](ADR-003-dynamodb-persistence.md) | DynamoDB para Persistência | Aceita | 2024-02-01 |
| [ADR-004](ADR-004-localstack-development.md) | LocalStack para Desenvolvimento Local | Aceita | 2024-02-01 |
| [ADR-005](ADR-005-saga-pattern-participation.md) | Participação no Saga Pattern | Aceita | 2024-02-05 |
| [ADR-006](ADR-006-testing-strategy.md) | Estratégia de Testes | Aceita | 2024-02-10 |

## Template

Para criar um novo ADR, use o [template](template.md) fornecido.

## Status

- **Proposta**: Decisão ainda em discussão
- **Aceita**: Decisão aprovada e em uso
- **Substituída**: Decisão substituída por outra ADR
- **Descontinuada**: Decisão não mais relevante
