# ADR-004: LocalStack para Desenvolvimento Local

## Status

**Aceita** - 2024-02-01

## Contexto

Com a adoção de AWS SQS (ADR-002) e DynamoDB (ADR-003), precisamos de uma forma de:

- Desenvolver e testar localmente sem depender de recursos AWS reais
- Evitar custos de desenvolvimento em ambiente cloud
- Permitir execução offline
- Manter paridade com o ambiente de produção (SQS e DynamoDB)

## Decisão

Adotamos **LocalStack** como emulador de serviços AWS para desenvolvimento local, no mesmo padrão do OS Service (ADR-004).

### Serviços Utilizados

- **SQS**: filas FIFO para consumo (`os-order-events-queue.fifo`) e publicação (`billing-events.fifo`)
- **DynamoDB**: tabelas `billing-service-budgets` e `billing-service-payments` (ou equivalentes locais com nomes configuráveis)

### Configuração da Aplicação

O perfil `local` (ou equivalente) deve apontar:

- Endpoint SQS: `http://localhost:4566`
- Endpoint DynamoDB: `http://localhost:4566`
- Região: `us-east-1`
- Credenciais: `test` / `test` (LocalStack aceita qualquer valor)

As variáveis de ambiente ou `application-local.yml` / `application-local.properties` devem sobrescrever as URLs e nomes de filas/tabelas para o contexto local.

### Script de Inicialização (exemplo)

Um script de init (ex.: em `localstack/init-aws.sh` ou no `docker-compose`) pode criar:

- Filas SQS FIFO: `os-order-events-queue.fifo`, `billing-events.fifo`
- Tabelas DynamoDB com partition key conforme definido no Terraform (budgetId, paymentId)

### Execução

```bash
# Iniciar LocalStack (se usar docker-compose)
docker-compose up -d localstack

# Verificar filas
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Iniciar aplicação com perfil local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Consequências

### Positivas

- ✅ **Desenvolvimento offline**: Desenvolvedores não dependem de AWS real
- ✅ **Zero custo**: Sem gastos com AWS em desenvolvimento
- ✅ **Paridade**: APIs compatíveis com SQS e DynamoDB reais
- ✅ **Reprodutibilidade**: Ambiente idêntico para todos os devs
- ✅ **Alinhamento**: Mesmo padrão do OS Service (ADR-004)

### Negativas

- ❌ **Não é 100% AWS**: Pode haver diferenças sutis (ex.: FIFO, limites)
- ❌ **Manutenção**: Scripts de init devem ser mantidos alinhados ao Terraform
- ❌ **Testes unitários**: Continuam usando mocks; LocalStack é para integração local

## Alternativas Consideradas

### 1. AWS real com perfil de dev

**Prós**: 100% compatível

**Contras**: Custo, internet, IAM

**Decisão**: Rejeitado para desenvolvimento diário

### 2. Apenas mocks in-memory

**Prós**: Simples, rápido em testes

**Contras**: Não valida integração real com SQS/DynamoDB

**Decisão**: Usado em testes unitários; LocalStack complementa para dev local

### 3. ElasticMQ + DynamoDB Local

**Prós**: Leve

**Contras**: Dois stacks; LocalStack unifica SQS + DynamoDB

**Decisão**: Rejeitado

## Referências

- [LocalStack Documentation](https://docs.localstack.cloud/)
- [LocalStack SQS](https://docs.localstack.cloud/user-guide/aws/sqs/)
- [LocalStack DynamoDB](https://docs.localstack.cloud/user-guide/aws/dynamodb/)
- OS Service: [ADR-004 LocalStack](https://github.com/.../ADR-004-localstack-development.md)
