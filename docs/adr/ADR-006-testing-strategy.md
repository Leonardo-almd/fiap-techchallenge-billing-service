# ADR-006: Estratégia de Testes

## Status

**Aceita** - 2024-02-10

## Contexto

O Billing Service precisa de uma estratégia de testes que:

- Garanta qualidade do código e correção das regras de negócio (orçamento, pagamento)
- Permita refatorações seguras
- Seja executável em CI/CD (pipelines alinhados ao OS Service)
- Cubra diferentes níveis (unitário, integração, BDD)
- Teste comportamento de negócio, não apenas implementação

## Decisão

Adotamos uma estratégia de testes em múltiplas camadas, combinando testes unitários, de integração e BDD, no mesmo padrão do OS Service (ADR-006).

### Pirâmide de Testes

```
        ╱╲
       ╱  ╲
      ╱ E2E╲         (Poucos - Cucumber BDD)
     ╱──────╲
    ╱        ╲
   ╱Integration╲     (Médio - @SpringBootTest quando aplicável)
  ╱────────────╲
 ╱              ╲
╱   Unit Tests   ╲   (Muitos - JUnit 5 + Mockito)
╲────────────────╱
```

### 1. Testes Unitários

**Foco**: Use Cases (CreateBudgetUseCase, ApproveBudgetUseCase, ProcessPaymentUseCase…), Entities, Value Objects (Price, BudgetStatus)

**Framework**: JUnit 5 + Mockito

**Exemplo** (CreateBudgetUseCase):
- Mock de `BudgetGateway`
- Entrada: serviceOrderId, customerId, vehicleId, lista de itens
- Verificação: budget criado com total calculado, status PENDING_APPROVAL, chamada a `gateway.save`

Outros use cases: ApproveBudget, RejectBudget, FindBudget, ProcessPayment, RefundPayment, FindPayment.

### 2. Testes de Integração

**Foco**: Controllers REST, repositórios DynamoDB (com DynamoDB Local ou mocks de cliente), configuração Spring

**Perfil**: `test` — desabilitar auto-configuração AWS real (SQS, DynamoDB) quando não for necessário; usar mocks ou containers de teste conforme necessário.

**Exemplo**: Testes de API para criar orçamento, aprovar, processar pagamento, consultar por serviceOrderId.

### 3. Testes BDD (Behavior-Driven Development)

**Foco**: Fluxos de negócio completos (criação de orçamento, aprovação, pagamento, rejeição)

**Framework**: Cucumber com Gherkin em português

**Estrutura**:
- `src/test/java/.../bdd/CucumberTest.java` (runner)
- `src/test/resources/.../features/` — arquivos `.feature` (cenários em pt-BR)
- Step definitions para orçamento e pagamento

**Vantagens**: Documentação executável, linguagem ubíqua, integração real dos componentes no teste.

### 4. Cobertura de Código

**Ferramenta**: JaCoCo (configurado no `pom.xml`)

**Métricas**: Limites de instrução e branch conforme definido no projeto (ex.: 80% instruction, 70% branch); falha do build se abaixo do mínimo.

### Execução

```bash
# Todos os testes
./mvnw test

# Apenas testes unitários (excluindo BDD)
./mvnw test -Dtest=*Test

# Apenas testes BDD (Cucumber)
./mvnw test -Dcucumber.filter.tags="not @ignore"

# Com relatório de cobertura
./mvnw test jacoco:report
```

O pipeline CI (`ci.yml`) executa build, unit tests, integration (verify -DskipUnitTests), code quality, security, SonarCloud e BDD, alinhado ao OS Service.

## Consequências

### Positivas

- ✅ **Confiança**: Mudanças verificadas automaticamente no CI
- ✅ **Documentação**: BDD como documentação viva dos fluxos
- ✅ **Qualidade**: Cobertura mínima garante baseline
- ✅ **Alinhamento**: Mesma pirâmide e ferramentas do OS Service
- ✅ **Isolamento**: Testes unitários com mocks; integração com perfis que evitam AWS real quando possível

### Negativas

- ❌ **Tempo**: Testes de integração e BDD são mais lentos
- ❌ **Manutenção**: Features e steps Cucumber devem ser mantidos
- ❌ **Cobertura**: Métrica sozinha não garante bons testes

## Alternativas Consideradas

### 1. Apenas testes unitários

**Prós**: Rápidos, isolados

**Contras**: Não cobrem integração com SQS, DynamoDB, controllers

**Decisão**: Rejeitado

### 2. E2E com ferramentas de UI (Selenium/Playwright)

**Prós**: Cobrem sistema completo

**Contras**: Lentos, frágeis; Billing é API; BDD em nível de API atende

**Decisão**: Rejeitado para o escopo atual

### 3. Contract Testing (Pact)

**Prós**: Garantir compatibilidade de contratos com OS Service

**Contras**: Complexidade adicional; contratos documentados em QUEUE_CONTRACT e EVENT-CONTRACTS

**Decisão**: Considerado para evolução futura

## Referências

- [Testing Pyramid - Martin Fowler](https://martinfowler.com/bliki/TestPyramid.html)
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/)
- OS Service: [ADR-006 Estratégia de Testes](https://github.com/.../ADR-006-testing-strategy.md)
