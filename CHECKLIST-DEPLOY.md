# Checklist: Billing Service está pronto para subir?

## Resposta direta

| Pergunta | Resposta |
|----------|----------|
| **K8s e documentação estão adaptados?** | ✅ Sim |
| **Podemos subir “dessa forma”?** | ⚠️ Só depois de garantir filas + DynamoDB (e, para integração com OS, ajuste de contrato) |
| **Precisa alterar código?** | ⚠️ Sim, **para integrar com o OS Service** (contrato do evento “OS criada”) |
| **Precisa alterar infraestrutura (novas filas)?** | ✅ Não – DynamoDB, SQS e IRSA do Billing foram **migrados para infra-database** (terraform: `dynamodb.tf`, `sqs.tf`, `iam.tf`). Basta rodar `terraform apply` no repo infra-database. |

---

## 1. O que já está adaptado (pode subir no mesmo padrão do OS)

- **Kubernetes:** manifestos no padrão do OS Service (namespace, service-account, configmap, secrets, app-deployment, app-service, hpa), com placeholders para o CD.
- **Configuração:** context path `/api/billing-service`, variáveis de ambiente via ConfigMap/Secret, uso de IRSA.
- **Documentação:** `docs/QUEUE_CONTRACT.md`, `docs/DEPLOY_SETUP.md`, `k8s/README.md` no mesmo estilo do OS.

Ou seja: o **formato** de deploy e a **estrutura** do serviço estão adaptados; o que falta é garantir **recursos na AWS** e, se quiser integração com o OS, **código + filas**.

---

## 2. O que ainda é necessário

### 2.1 Infraestrutura (SQS e DynamoDB) – em infra-database

**Não existe mais pasta `terraform/` neste repositório (Billing Service).** Toda a infraestrutura do Billing está no repositório **fiap-techchallenge-infra-database**, no mesmo padrão do OS Service:

- **DynamoDB:** `terraform/dynamodb.tf` – tabelas `billing-service-budgets` e `billing-service-payments`
- **SQS:** `terraform/sqs.tf` – fila `billing-events.fifo` e DLQ `billing-events-dlq.fifo` (Billing publica); Billing consome de `os-order-events-queue.fifo` (já existente)
- **IRSA:** `terraform/iam.tf` – role `billing-service-irsa-role` (DynamoDB + SQS + ECR)
- **Outputs:** `terraform/outputs.tf` – `billing_service_irsa_role_arn`, `billing_dynamodb_*_table_name`, `sqs_billing_events_queue_*`, `billing_service_k8s_config`

Antes de subir o Billing, rodar `terraform apply` no **infra-database** para criar DynamoDB, filas e IRSA. O CD do Billing deve usar os outputs desse Terraform para preencher os placeholders dos manifestos K8s.

### 2.2 Infraestrutura (DynamoDB)

- As tabelas do Billing são criadas pelo Terraform em **infra-database** (`dynamodb.tf`). Nomes: `billing-service-budgets`, `billing-service-payments`.

### 2.3 Código (só se quiser integração com o OS Service)

Hoje:

- O **OS Service** publica em `os-order-events-queue.fifo` um payload com `orderId` (Long), `customerId`, `vehicleId`, **sem** array `items`.
- O **Billing Service** espera um `ServiceOrderCreatedEvent` com `serviceOrderId` (String), `customerId`, `vehicleId` e **`items[]`** (com type, itemCode, description, quantity, unitPrice).

Se o Billing subir e o OS enviar um evento nesse formato atual, o consumer do Billing vai **falhar** ao deserializar (campos diferentes, `items` ausente). Ou seja: **para integração ponta a ponta (OS cria ordem → Billing cria orçamento) é necessário ajuste**, em um dos lados:

- **Opção A – Ajustar o OS Service (recomendado):**  
  Passar a publicar um payload compatível com o Billing (ex.: `serviceOrderId` como string, `items` preenchido a partir dos services/resources da OS). Assim o Billing não precisa mudar o contrato.

- **Opção B – Ajustar o Billing Service:**  
  Criar um DTO/adapter que aceite o payload atual do OS (`orderId`, sem `items`) e converta para o que o `CreateBudgetUseCase` precisa (ex.: orçamento sem itens ou com itens default). Assim o OS não precisa mudar.

Sem um desses ajustes, o serviço **sobe**, mas o fluxo “OS criada → Billing cria orçamento” **não** funciona.

---

## 3. Resumo: podemos subir “dessa forma”?

- **Sim, para “subir o serviço” no EKS no mesmo padrão do OS:**  
  Desde que:
  1. O **infra-database** tenha sido aplicado (`terraform apply`) – assim existem DynamoDB, filas SQS (`billing-events.fifo`, consumo em `os-order-events-queue.fifo`) e IRSA do Billing.
  2. O **ConfigMap/Secret** no K8s estejam preenchidos com os outputs do Terraform (nomes de tabelas, nomes de filas, ARN da role IRSA).

- **Não, para “subir e já integrar com o OS Service sem mais nada”:**  
  O **contrato do evento “OS criada”** continua diferente; sem alteração no código (OS ou Billing) ou adapter, o fluxo “OS criada → Billing cria orçamento” não funciona.

---

## 4. Ações práticas antes de subir

| # | Onde | O quê |
|---|------|--------|
| 1 | **Infra (infra-database)** | Rodar `terraform apply` no repositório **fiap-techchallenge-infra-database**. Isso cria DynamoDB (budgets, payments), SQS (billing-events.fifo + DLQ) e IRSA do Billing. |
| 2 | **ConfigMap do Billing** | Preencher placeholders com os outputs do Terraform: `billing_dynamodb_budgets_table_name`, `billing_dynamodb_payments_table_name`, `sqs_os_order_events_queue_name`, `sqs_billing_events_queue_name`. |
| 3 | **ServiceAccount** | Preencher `__IRSA_ROLE_ARN__` com o output `billing_service_irsa_role_arn` do Terraform. |
| 4 | **Código (se quiser integração com OS)** | Ajustar contrato do evento “OS criada” (no OS ou no Billing), conforme `ANALISE-OS-SERVICE-E-INFRA.md` e `docs/QUEUE_CONTRACT.md`. |

Com os itens 1, 2 e 3 feitos, **podem subir dessa forma** (serviço no ar, API e publicação funcionando). Com o item 4, o fluxo OS → Billing fica integrado de ponta a ponta.
