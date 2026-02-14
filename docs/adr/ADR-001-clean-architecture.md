# ADR-001: Clean Architecture

## Status

**Aceita** - 2024-01-15

## Contexto

O Billing Service faz parte de uma arquitetura de microserviços e precisa de uma arquitetura que:

- Facilite a manutenção e evolução do código
- Permita substituição de componentes de infraestrutura (DynamoDB, SQS) sem impactar regras de negócio
- Facilite testes unitários isolados das dependências externas
- Siga o mesmo padrão do OS Service para consistência no ecossistema

## Decisão

Adotamos a **Clean Architecture** (Arquitetura Limpa) proposta por Robert C. Martin, organizando o código em camadas concêntricas com dependências apontando para dentro, alinhado ao OS Service.

### Estrutura de Camadas

```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                      │
│  - Controllers REST                                          │
│  - Repositórios DynamoDB                                     │
│  - Mensageria AWS SQS (consumer + publisher)                 │
│  - Simulador de gateway de pagamento                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  - Use Cases (CreateBudget, ApproveBudget, ProcessPayment…)  │
│  - Gateways (BudgetGateway, PaymentGateway)                   │
│  - Presenters (BudgetPresenter, PaymentPresenter)            │
│  - DTOs (objetos de transferência de dados)                  │
│  - Entities (Budget, Payment, BudgetItem, Price…)             │
│  - Exceptions (InvalidDataException, NotFoundException)      │
└─────────────────────────────────────────────────────────────┘
```

### Organização de Pacotes

```
br.com.techchallenge.fiap.billingservice/
├── application/
│   ├── controller/      # Controllers da Clean Arch (entrada)
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # Entidades de domínio (Budget, Payment, Price, BudgetStatus…)
│   ├── exception/       # Exceções de negócio
│   ├── gateway/         # Interfaces (BudgetGateway, PaymentGateway)
│   ├── presenter/       # Formatadores de resposta
│   └── usecase/         # Casos de uso (budget, payment)
├── infrastructure/
│   ├── config/          # Configurações Spring e AWS
│   ├── controller/      # Controllers REST (API pública)
│   ├── messaging/       # Consumer SQS, Publisher SQS, eventos
│   ├── orchestration/   # Orquestradores (BudgetEventOrchestrator, PaymentProcessingOrchestrator)
│   ├── payment/         # Implementação do gateway de pagamento (simulador)
│   └── repository/
│       └── dynamodb/    # Implementações DynamoDB (Budget, Payment)
└── BillingServiceApplication.java
```

### Regras Principais

1. **Entities** não dependem de nada externo
2. **Use Cases** dependem apenas de Entities e Gateways (interfaces)
3. **Infrastructure** implementa os Gateways (DynamoDB, SQS, Payment)
4. **Inversão de Dependência** através de interfaces (BudgetGateway, PaymentGateway)

## Consequências

### Positivas

- ✅ **Testabilidade**: Use cases podem ser testados isoladamente com mocks dos gateways
- ✅ **Manutenibilidade**: Mudanças em DynamoDB ou SQS não afetam regras de negócio
- ✅ **Flexibilidade**: Fácil trocar persistência ou message broker
- ✅ **Clareza**: Separação clara entre orçamento, pagamento e mensageria
- ✅ **Alinhamento**: Mesmo padrão do OS Service

### Negativas

- ❌ **Complexidade inicial**: Mais arquivos e indireções
- ❌ **Curva de aprendizado**: Desenvolvedores precisam entender o padrão
- ❌ **Boilerplate**: DTOs, Gateways e implementações adicionam código

## Alternativas Consideradas

1. **MVC Tradicional**: Rejeitado por acoplar lógica de negócio aos controllers
2. **Hexagonal Architecture**: Similar; Clean Architecture é o padrão adotado no ecossistema (OS Service)
3. **DDD Layered**: Considerado; Clean Architecture oferece isolamento suficiente para o escopo atual

## Referências

- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- OS Service: [ADR-001 Clean Architecture](../fiap-techchallenge-microservice-os-service/docs/adr/ADR-001-clean-architecture.md)
