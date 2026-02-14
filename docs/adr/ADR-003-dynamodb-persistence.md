# ADR-003: DynamoDB para Persistência

## Status

**Aceita** - 2024-02-01

## Contexto

O Billing Service precisa persistir:

- **Orçamentos (Budgets)**: identificador, ordem de serviço, cliente, veículo, itens (tipo, código, descrição, quantidade, preço), valor total, status, datas
- **Pagamentos (Payments)**: identificador, orçamento, valor, método, status, datas

Requisitos:

- Escalabilidade sob demanda (ambiente AWS/EKS)
- Sem gestão de servidor de banco (managed)
- Consistência adequada para orçamento e pagamento (não exigimos transações distribuídas entre entidades distintas no mesmo serviço)
- Alinhamento com a infraestrutura já provisionada em Terraform (infra-database)

## Decisão

Adotamos **Amazon DynamoDB** como banco de dados principal para Budget e Payment, com tabelas dedicadas provisionadas pelo repositório **fiap-techchallenge-infra-database** (Terraform).

### Tabelas

| Tabela | Partition Key | Sort Key (se houver) | Uso |
|--------|----------------|----------------------|-----|
| `billing-service-budgets` | `budgetId` (String) | — | Um item por orçamento; consultas por `serviceOrderId` via GSI |
| `billing-service-payments` | `paymentId` (String) | — | Um item por pagamento; consultas por `serviceOrderId` via GSI |

Os nomes exatos das tabelas vêm dos outputs do Terraform e são injetados no ConfigMap no deploy (variáveis `DYNAMODB_TABLE_BUDGETS`, `DYNAMODB_TABLE_PAYMENTS`).

### Camada de Acesso

- **Application**: interfaces `BudgetGateway` e `PaymentGateway`
- **Infrastructure**: `BudgetDynamoDBRepository` e `PaymentDynamoDBRepository` implementam os gateways
- **Modelos**: `BudgetDynamoModel`, `BudgetItemDynamoModel`, `PaymentDynamoModel` para serialização DynamoDB; mappers convertem entre entidades de domínio e modelos

### Autenticação

No EKS, os pods usam **IRSA** (IAM Roles for Service Accounts); a role do Billing tem permissão para acessar as tabelas DynamoDB e as filas SQS definidas no Terraform (iam.tf).

## Consequências

### Positivas

- ✅ **Managed**: Sem servidor nem patches; escalabilidade automática
- ✅ **Performance**: Baixa latência para acesso por chave; GSIs para consultas por serviceOrderId
- ✅ **Custo**: Pay-per-request; adequado para carga variável
- ✅ **Infra centralizada**: Tabelas criadas e nomeadas no infra-database; Billing só consome nomes via config
- ✅ **IRSA**: Sem credenciais de DB em Secret; uso de IAM

### Negativas

- ❌ **Modelagem rígida**: Mudanças em chaves/GSIs exigem evolução de schema e possivelmente migração
- ❌ **Sem JOINs**: Consultas complexas entre Budget e Payment são feitas na aplicação (múltiplas leituras)
- ❌ **Vendor lock-in**: DynamoDB é AWS; migração para outro NoSQL exigiria adaptação da camada de repositório

## Alternativas Consideradas

### 1. Amazon RDS (PostgreSQL/MySQL)

**Prós**: SQL, transações ACID, JOINs

**Contras**: Gestão de instância; OS Service já usa RDS; Billing com modelo mais simples (documento por orçamento/pagamento) se beneficia de DynamoDB

**Decisão**: Rejeitado para este serviço — DynamoDB atende e reduz operação de banco relacional

### 2. MongoDB (DocumentDB ou self-hosted)

**Prós**: Modelo documento, flexível

**Contras**: Mais uma tecnologia no ecossistema; DynamoDB já previsto na infra e integrado ao IAM/IRSA

**Decisão**: Rejeitado

### 3. Apenas em memória / sem persistência

**Prós**: Simplicidade

**Contras**: Perda de dados em restart; inaceitável para orçamento e pagamento

**Decisão**: Rejeitado

## Referências

- [Amazon DynamoDB](https://docs.aws.amazon.com/dynamodb/)
- [AWS SDK for Java 2.x - DynamoDB](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb.html)
- Infraestrutura: repositório **fiap-techchallenge-infra-database** (`terraform/dynamodb.tf`, `terraform/iam.tf`)
