# Adaptação do Billing Service ao OS Service e à Infraestrutura Existente

Este documento descreve as alterações feitas no **Billing Service** para replicar o mesmo padrão do **microserviço OS Service (Pessoa 1)** e da infraestrutura (infra-database, infra-kubernetes).

## Resumo das mudanças

### 1. Kubernetes (k8s/)

- **Estrutura alinhada ao OS Service:**
  - `namespace.yaml` – labels `app`, `environment` (igual ao OS)
  - `service-account.yaml` – ServiceAccount com anotação `eks.amazonaws.com/role-arn: "__IRSA_ROLE_ARN__"` para IRSA
  - `configmap.yaml` – ConfigMap com placeholders `__VAR__` para CD (DYNAMODB_*, SQS_*), `SERVER_SERVLET_CONTEXT_PATH: "/api/billing-service"`, MANAGEMENT_*
  - `secrets.yaml` – Secret com placeholder para CD; nota de uso de IRSA (sem credenciais estáticas)
  - `app-deployment.yaml` – Deployment com `envFrom` (configMapRef + secretRef), `image: __IMAGE_URI__`, probes em `/api/billing-service/actuator/...`, recursos 256Mi/512Mi, 100m/300m CPU
  - `app-service.yaml` – Service tipo LoadBalancer, porta 8080 (igual ao OS)
  - `hpa.yaml` – HPA min 1, max 2, CPU 80%, memória 85% (igual ao OS)

- **Removidos (substituídos pelos acima):**
  - `deployment.yaml` (substituído por `app-deployment.yaml`)
  - `service.yaml` (substituído por `app-service.yaml`)

- **Kustomization:** `kustomization.yaml` atualizado para referenciar `service-account.yaml`, `secrets.yaml`, `app-deployment.yaml`, `app-service.yaml`. Ingress/RBAC/NetworkPolicy/PDB ficaram comentados no recurso padrão (deploy mínimo igual ao OS).

### 2. Aplicação (application.yml)

- **Context path configurável:**  
  `server.servlet.context-path: ${SERVER_SERVLET_CONTEXT_PATH:/api/v1}`  
  Em K8s o ConfigMap define `SERVER_SERVLET_CONTEXT_PATH=/api/billing-service` (padrão do OS: `/api/os-service`). Localmente continua podendo usar `/api/v1` se a variável não for definida.

- **Porta:**  
  `server.port: ${SERVER_PORT:8080}` para permitir override via ConfigMap.

### 3. Documentação (docs/)

- **docs/QUEUE_CONTRACT.md** – Contrato de filas SQS no mesmo estilo do OS Service: filas de entrada/saída, payloads, alinhamento com a infra (os-order-events-queue.fifo, quote-approved-queue, payment-failed-queue, billing-events).

- **docs/DEPLOY_SETUP.md** – Configuração de CD e IRSA no mesmo estilo do OS: ordem de deploy, placeholders, outputs do Terraform sugeridos, verificação e referências.

- **k8s/README.md** – Guia de deploy no EKS no mesmo estilo do OS: pré-requisitos, ordem de deploy, variáveis, monitoramento, troubleshooting.

## Como fazer o deploy (igual ao OS Service)

1. Substituir placeholders nos manifests (via pipeline de CD ou manualmente):
   - `configmap.yaml`: `__DYNAMODB_TABLE_BUDGETS__`, `__DYNAMODB_TABLE_PAYMENTS__`, `__SQS_QUEUE_SERVICE_ORDER_EVENTS__`, `__SQS_QUEUE_BILLING_EVENTS__`
   - `secrets.yaml`: `__PLACEHOLDER_B64__` (ou valores reais se não usar só IRSA)
   - `service-account.yaml`: `__IRSA_ROLE_ARN__`
   - `app-deployment.yaml`: `__IMAGE_URI__`

2. Aplicar na ordem (como no OS Service):
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/service-account.yaml
   kubectl apply -f k8s/secrets.yaml
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/app-deployment.yaml
   kubectl apply -f k8s/app-service.yaml
   kubectl apply -f k8s/hpa.yaml
   ```

## Integração com a infraestrutura existente

- **Filas SQS:** O Billing consome da mesma fila em que o OS Service publica (`os-order-events-queue.fifo`) e pode publicar nas filas que o OS já consome (`quote-approved-queue`, `payment-failed-queue`). Ver `docs/QUEUE_CONTRACT.md` e `ANALISE-OS-SERVICE-E-INFRA.md`.

- **DynamoDB:** Tabelas do Billing continuam no Terraform do Billing (ou podem ser movidas para o infra-database quando o grupo centralizar).

- **ECR:** Usar o mesmo repositório ECR do projeto (com tag por serviço) ou criar um repositório específico para o Billing, conforme definido no infra-kubernetes.

- **IRSA:** Criar IAM Role para o ServiceAccount `billing-service-sa` (permissões DynamoDB + SQS) no Terraform (infra-database ou do Billing), no mesmo padrão do OS Service.

## Referências

- OS Service (padrão replicado): `fiap-techchallenge-microservice-os-service/k8s/`, `docs/QUEUE_CONTRACT.md`, `docs/DEPLOY_SETUP.md`
- Análise de integração: `ANALISE-OS-SERVICE-E-INFRA.md`
