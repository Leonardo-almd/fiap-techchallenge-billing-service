# Configuração do CD Pipeline e IRSA - Billing Service

Este documento descreve a configuração necessária para deploy do Billing Service, **replicando o mesmo padrão** do OS Service (microserviço 1) e da infraestrutura existente.

## Visão geral

| Componente              | Método de autenticação        |
|-------------------------|-------------------------------|
| Pipeline CD (GitHub Actions) | Credenciais estáticas (IAM User) |
| Pod (billing-service)   | IRSA (token OIDC automático)  |

## Recursos AWS necessários

Todos os recursos de infraestrutura (DynamoDB, SQS, IRSA) são provisionados pelo repositório **fiap-techchallenge-infra-database**, no mesmo padrão do OS Service. **Não existe pasta `terraform/` neste repositório.**

- **AWS EKS** (cluster e node group) – fiap-techchallenge-infra-kubernetes
- **AWS DynamoDB** – tabelas `billing-service-budgets` e `billing-service-payments` (Terraform em **infra-database**: `terraform/dynamodb.tf`)
- **AWS SQS** – consumo em `os-order-events-queue.fifo` (já existente); publicação em `billing-events.fifo` (Terraform em **infra-database**: `terraform/sqs.tf`)
- **IRSA** – IAM Role do Billing (Terraform em **infra-database**: `terraform/iam.tf`)
- **ECR** – imagem Docker do Billing (infra-kubernetes)

## Ordem de deploy (Kubernetes)

Igual ao OS Service:

```bash
# 1. Namespace
kubectl apply -f k8s/namespace.yaml

# 2. Service Account (IRSA)
kubectl apply -f k8s/service-account.yaml

# 3. Substituir placeholders em secrets e configmap (via CD ou sed) e aplicar
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml

# 4. Aplicação
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml

# 5. HPA
kubectl apply -f k8s/hpa.yaml
```

## Placeholders nos manifests K8s

O pipeline de CD deve substituir os placeholders (por exemplo com `sed` ou ferramenta equivalente), de forma análoga ao OS Service:

| Arquivo              | Placeholder                         | Origem / valor |
|----------------------|-------------------------------------|----------------|
| configmap.yaml       | `__DYNAMODB_TABLE_BUDGETS__`        | Nome da tabela DynamoDB (ex.: billing-service-budgets) |
| configmap.yaml       | `__DYNAMODB_TABLE_PAYMENTS__`       | Nome da tabela DynamoDB (ex.: billing-service-payments) |
| configmap.yaml       | `__SQS_QUEUE_SERVICE_ORDER_EVENTS__` | Nome da fila de consumo (ex.: os-order-events-queue.fifo) |
| configmap.yaml       | `__SQS_QUEUE_BILLING_EVENTS__`      | Nome da fila de publicação (ex.: billing-events.fifo) |
| secrets.yaml         | `__PLACEHOLDER_B64__`               | Qualquer valor base64 (ex.: `echo -n 'x' \| base64`) quando só IRSA é usado |
| service-account.yaml | `__IRSA_ROLE_ARN__`                 | ARN da IAM Role IRSA do Billing (Terraform output) |
| app-deployment.yaml  | `__IMAGE_URI__`                     | URI da imagem ECR (ex.: 123456789.dkr.ecr.us-east-1.amazonaws.com/cargarage-app:billing-service-latest) |

## Outputs do Terraform (infra-database)

O Terraform em **fiap-techchallenge-infra-database** expõe os seguintes outputs para o Billing Service (preencher placeholders do CD):

| Output | Descrição |
|--------|-----------|
| `billing_service_irsa_role_arn` | ARN da role IRSA (ServiceAccount) |
| `billing_dynamodb_budgets_table_name` | Nome da tabela DynamoDB de orçamentos |
| `billing_dynamodb_payments_table_name` | Nome da tabela DynamoDB de pagamentos |
| `sqs_os_order_events_queue_name` | Nome da fila que o Billing **consome** |
| `sqs_billing_events_queue_name` | Nome da fila que o Billing **publica** |
| `sqs_billing_events_queue_url` | URL da fila billing-events |
| `billing_service_k8s_config` | Objeto com DYNAMODB_* e SQS_* para ConfigMap |

## IRSA (IAM Roles for Service Accounts)

1. **Terraform** em **infra-database** (`terraform/iam.tf`) cria a IAM Role `billing-service-irsa-role` com trust policy para o OIDC Provider do EKS.
2. **ServiceAccount** `billing-service-sa` no namespace `billing-service` é anotado com `eks.amazonaws.com/role-arn: <ARN>`.
3. O pod usa essa ServiceAccount; o EKS injeta o token OIDC e o AWS SDK assume a role (sem credenciais estáticas no Secret).

Permissões mínimas sugeridas para a role:

- **DynamoDB:** GetItem, PutItem, UpdateItem, DeleteItem, Query, BatchGetItem, BatchWriteItem, DescribeTable (nas tabelas do Billing).
- **SQS:** SendMessage, ReceiveMessage, DeleteMessage, GetQueueUrl, GetQueueAttributes (nas filas usadas pelo Billing).

## Verificação após deploy

```bash
kubectl get pods -n billing-service
kubectl get svc -n billing-service
kubectl logs -f deployment/billing-service -n billing-service
kubectl describe serviceaccount billing-service-sa -n billing-service
```

Health e métricas (context path alinhado ao OS Service):

```bash
kubectl port-forward svc/billing-service 8080:8080 -n billing-service
curl http://localhost:8080/api/billing-service/actuator/health
curl http://localhost:8080/api/billing-service/actuator/prometheus
```

## Pipelines CI/CD (GitHub Actions)

O deploy automatizado usa os workflows **ci.yml** e **cd.yml**, alinhados ao OS Service:

- **CI (ci.yml):** build, testes, checkstyle, spotbugs, OWASP, SonarCloud, build Docker (sem push), BDD Cucumber. Tudo na raiz do repositório (sem `./app`).
- **CD (cd.yml):** disparado em push em `main` ou tags `v*` (e manual via `workflow_dispatch`). O job **build-and-push** gera a imagem ECR (ex.: `cargarage-app:billing-service-0.1.0`). O job **deploy**:
  1. Obtém os outputs do Terraform via **Terraform Cloud API** (workspace `fiap-techchallenge-infra-database`).
  2. Substitui os placeholders nos manifestos com `sed` (configmap, service-account, secrets, app-deployment).
  3. Aplica os recursos na ordem: namespace → service-account → configmap → secrets → app-deployment → app-service → hpa.
  4. Faz smoke test em `/api/billing-service/actuator/health`.

**Secrets necessários para o CD:** `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `TF_API_TOKEN`. Ver também `.github/workflows/README.md`.

## Referências

- **Infraestrutura (DynamoDB, SQS, IRSA):** repositório **fiap-techchallenge-infra-database** (terraform: `dynamodb.tf`, `sqs.tf`, `iam.tf`, `outputs.tf`)
- **Workflows CI/CD:** `.github/workflows/README.md`
- **OS Service (mesmo padrão):** `fiap-techchallenge-microservice-os-service/docs/DEPLOY_SETUP.md`
- **Contratos de filas:** `docs/QUEUE_CONTRACT.md`
- **Análise de integração OS + Infra:** `ANALISE-OS-SERVICE-E-INFRA.md`
