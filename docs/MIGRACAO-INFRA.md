# Migração da infraestrutura para repositórios centralizados

A pasta **`terraform/`** foi **removida** do repositório do Billing Service. Toda a infraestrutura (DynamoDB, SQS, IAM/IRSA) foi migrada para o repositório **fiap-techchallenge-infra-database**, seguindo o mesmo padrão do microserviço OS Service.

## Onde está a infraestrutura agora

| Recurso | Repositório | Arquivo(s) |
|--------|-------------|------------|
| DynamoDB (budgets, payments) | **fiap-techchallenge-infra-database** | `terraform/dynamodb.tf` |
| SQS (billing-events.fifo + DLQ) | **fiap-techchallenge-infra-database** | `terraform/sqs.tf` |
| IAM / IRSA (billing-service) | **fiap-techchallenge-infra-database** | `terraform/iam.tf` |
| Outputs (nomes de tabelas, filas, ARN) | **fiap-techchallenge-infra-database** | `terraform/outputs.tf` |

## Por que migrar

- **Um único Terraform** para banco de dados e mensageria (RDS + DynamoDB + SQS), alinhado ao OS Service.
- **Um único lugar** para aplicar e versionar infraestrutura (infra-database).
- **CD pipeline** pode buscar todos os outputs do Terraform Cloud (ou estado remoto) e preencher os manifestos K8s do Billing e do OS da mesma forma.

## O que fazer para deploy do Billing

1. No repositório **fiap-techchallenge-infra-database**, rodar `terraform apply` (ou via Terraform Cloud).
2. Usar os outputs do Terraform para preencher os placeholders dos manifestos K8s do Billing (ConfigMap, Secret, ServiceAccount). Ver `docs/DEPLOY_SETUP.md`.

## Referências

- **Deploy do Billing:** `docs/DEPLOY_SETUP.md`
- **Checklist antes de subir:** `CHECKLIST-DEPLOY.md`
- **Infraestrutura:** repositório **fiap-techchallenge-infra-database**
