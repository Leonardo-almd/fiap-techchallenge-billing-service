# Kubernetes Deployment Guide - Billing Service (AWS EKS)

## Overview

This directory contains Kubernetes manifests for deploying the **Billing Service** microservice on **AWS EKS**, with **AWS DynamoDB** and **AWS SQS**. The structure and deployment flow replicate the same pattern used by the **OS Service** (microservice 1) and the existing infrastructure.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │    AWS EKS      │  │   DynamoDB      │  │    AWS SQS      │  │
│  │  (Kubernetes)   │  │  (Budgets,      │  │   (Queues)      │  │
│  │                 │  │   Payments)     │  │                 │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │            │
│           └────────────────────┼────────────────────┘            │
│                                │                                  │
│                        IAM + IRSA                                │
└─────────────────────────────────────────────────────────────────┘
```

## Prerequisites

- AWS EKS cluster (1.25+)
- kubectl configured for EKS
- AWS DynamoDB tables created (budgets, payments)
- AWS SQS queues created (inbound: e.g. os-order-events-queue.fifo; outbound: e.g. billing-events.fifo)
- IRSA (IAM Roles for Service Accounts) configured for the billing-service namespace
- Docker image pushed to ECR

## AWS Resources Required

### SQS Queues

- **Inbound (Billing consumes):** same queue where OS Service publishes – e.g. `os-order-events-queue.fifo`
- **Outbound (Billing publishes):** e.g. `billing-events.fifo`; for direct integration with OS Service, Billing may also publish to `quote-approved-queue` and `payment-failed-queue` (already created for OS Service)

See `docs/QUEUE_CONTRACT.md` for payload contracts.

### DynamoDB Tables

- Table for budgets (e.g. `billing-service-budgets`)
- Table for payments (e.g. `billing-service-payments`)

Defined in the Billing Service Terraform or in the shared infra-database when integrated.

### IAM Role for IRSA

Create an IAM role with DynamoDB and SQS permissions and trust relationship for the EKS OIDC provider. See `docs/DEPLOY_SETUP.md`.

## Deployment Order

Deploy resources in the same order as the OS Service:

```bash
# 1. Create namespace
kubectl apply -f namespace.yaml

# 2. Create service account (IRSA)
kubectl apply -f service-account.yaml

# 3. Update secrets.yaml and configmap.yaml with your values (or use CD to replace placeholders)
#    Then apply secrets and configmaps
kubectl apply -f secrets.yaml
kubectl apply -f configmap.yaml

# 4. Deploy application
kubectl apply -f app-deployment.yaml
kubectl apply -f app-service.yaml

# 5. Configure HPA
kubectl apply -f hpa.yaml
```

## Quick Deploy

Deploy all core resources at once (after replacing placeholders):

```bash
kubectl apply -f namespace.yaml
kubectl apply -f service-account.yaml
kubectl apply -f secrets.yaml
kubectl apply -f configmap.yaml
kubectl apply -f app-deployment.yaml
kubectl apply -f app-service.yaml
kubectl apply -f hpa.yaml
```

## Verify Deployment

```bash
# Check all pods
kubectl get pods -n billing-service

# Check services
kubectl get svc -n billing-service

# Check HPA
kubectl get hpa -n billing-service

# View logs
kubectl logs -f deployment/billing-service -n billing-service
```

## Access the Service

```bash
# Get the external IP (LoadBalancer)
kubectl get svc billing-service -n billing-service

# Port-forward for local access
kubectl port-forward svc/billing-service 8080:8080 -n billing-service
```

Then:

- Health: `http://localhost:8080/api/billing-service/actuator/health`
- Swagger: `http://localhost:8080/api/billing-service/swagger-ui.html` (if enabled)

## Scaling

```bash
# Manual scaling
kubectl scale deployment/billing-service --replicas=2 -n billing-service

# Check HPA status
kubectl describe hpa billing-service-hpa -n billing-service
```

## Configuration

### Environment Variables (ConfigMap)

| Variable | Description | Default / placeholder |
|----------|-------------|------------------------|
| SERVER_PORT | Application port | 8080 |
| SERVER_SERVLET_CONTEXT_PATH | Context path | /api/billing-service |
| AWS_REGION | AWS Region | us-east-1 |
| DYNAMODB_TABLE_BUDGETS | DynamoDB budgets table name | __DYNAMODB_TABLE_BUDGETS__ |
| DYNAMODB_TABLE_PAYMENTS | DynamoDB payments table name | __DYNAMODB_TABLE_PAYMENTS__ |
| SQS_QUEUE_SERVICE_ORDER_EVENTS | Inbound queue name | __SQS_QUEUE_SERVICE_ORDER_EVENTS__ |
| SQS_QUEUE_BILLING_EVENTS | Outbound queue name | __SQS_QUEUE_BILLING_EVENTS__ |

### Secrets

Secrets are base64 encoded when using `data`. For IRSA-only setups, only a placeholder may be needed. See `docs/DEPLOY_SETUP.md`.

## Monitoring

Prometheus metrics at `/api/billing-service/actuator/prometheus`:

```bash
kubectl port-forward svc/billing-service 8080:8080 -n billing-service
curl http://localhost:8080/api/billing-service/actuator/prometheus
```

## Troubleshooting

### Pod not starting

```bash
kubectl describe pod <pod-name> -n billing-service
kubectl logs <pod-name> -n billing-service --previous
```

### DynamoDB or SQS access issues

Check IAM role permissions (IRSA) and that table/queue names (or URLs) in ConfigMap/Secrets are correct. Verify IRSA:

```bash
kubectl describe serviceaccount billing-service-sa -n billing-service
```

## Cleanup

```bash
kubectl delete namespace billing-service
```

## References

- **OS Service (same pattern):** `fiap-techchallenge-microservice-os-service/k8s/README.md`
- **Queue contracts:** `docs/QUEUE_CONTRACT.md`
- **Deploy and IRSA setup:** `docs/DEPLOY_SETUP.md`
