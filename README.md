# 💰 Billing Service

![Build](https://github.com/YOUR_ORG/billing-service/workflows/Build%20and%20Test/badge.svg)
![Quality](https://github.com/YOUR_ORG/billing-service/workflows/Code%20Quality/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-75%25-green)
![Java](https://img.shields.io/badge/java-21-orange)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.4.7-green)
![License](https://img.shields.io/badge/license-MIT-blue)

**Microsserviço de Orçamento e Pagamento** para o Tech Challenge FIAP 12SOAT - Fase 4

---

## 📝 Sobre o Projeto

O **Billing Service** gerencia o domínio de **Budget** (Orçamento) e **Payment** (Pagamento) na arquitetura de microsserviços da Car Garage, uma oficina mecânica.

### ✨ Funcionalidades

- ✅ **Gestão de Orçamentos**
  - Criação automática via eventos
  - Aprovação/Rejeição de orçamentos
  - Consulta e listagem

- ✅ **Processamento de Pagamentos**
  - Múltiplos métodos (Cartão, PIX, Boleto)
  - Gateway de pagamento simulado (ilustrativo)
  - Processamento assíncrono

- ✅ **Saga Pattern**
  - Modelo coreografado via eventos SQS
  - Compensação automática (estorno)
  - 6 eventos de domínio

- ✅ **API REST**
  - 12 endpoints documentados (Swagger)
  - Validações Bean Validation
  - Tratamento global de exceções

---

## 🏗️ Arquitetura

### Clean Architecture + DDD

```
┌─────────────────────────────────────────────────────────┐
│               Infrastructure Layer                       │
│  REST Controllers, DynamoDB, SQS, Payment Simulator     │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │            Application Layer                       │  │
│  │  UseCases, Gateways (Ports), Presenters           │  │
│  │                                                     │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │         Domain Layer                        │  │  │
│  │  │  Budget, Payment, Price (Value Object)     │  │  │
│  │  │  Business Rules                             │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Saga Pattern (Choreographed)

```
ServiceOrder MS → ServiceOrderCreatedEvent
                 ↓
          [Billing Service]
          CreateBudget (PENDING)
                 ↓
          Admin Approves
                 ↓
          BudgetApprovedEvent →
                 ↓
          Customer Pays
                 ↓
      PaymentProcessingOrchestrator
                 ↓
       PaymentGatewaySimulator
                 ↓
        ┌────────┴────────┐
     SUCCESS           FAILURE
        ↓                 ↓
PaymentProcessedEvent  PaymentFailedEvent
        ↓                 ↓
   [Execution MS]    [Compensation]
```

### Stack Tecnológico

| Categoria | Tecnologia |
|-----------|------------|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.4.7 |
| **Database** | AWS DynamoDB (NoSQL) |
| **Messaging** | AWS SQS (FIFO) |
| **Container** | Docker |
| **Orchestration** | Kubernetes (EKS) |
| **CI/CD** | GitHub Actions |
| **IaC** | Terraform (em **fiap-techchallenge-infra-database**) |
| **Testing** | JUnit 5, Mockito, Cucumber |
| **Quality** | JaCoCo, Checkstyle, SpotBugs, SonarCloud |

---

## 🚀 Quick Start

### Pré-requisitos

- **Java 21** (LTS) — mesmo que em produção; evite Java 22+ para rodar testes (Mockito/ByteBuddy).
- Maven 3.9+ (ou use `./mvnw`)
- Se usar [SDKMAN](https://sdkman.io): `sdk env` na raiz do projeto usa a versão definida em `.sdkmanrc`.
- Docker & Docker Compose
- AWS CLI (para produção)

### Rodar Localmente

```bash
# 1. Clone o repositório
git clone https://github.com/YOUR_ORG/fiap-techchallenge-billing-service.git
cd fiap-techchallenge-billing-service

# 2. Subir LocalStack (AWS local - DynamoDB + SQS)
docker-compose up -d localstack

# 3. Rodar aplicação
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 4. Verificar health
curl http://localhost:8080/actuator/health

# 5. Acessar Swagger UI
open http://localhost:8080/api/v1/swagger-ui/index.html
```

### Executar Testes

Para **reproduzir localmente o mesmo passo "Run Unit Tests" do CI** (evitar depender só da pipeline):

```bash
# Opção 1: script (usa mvn ou ./mvnw automaticamente)
./run-tests.sh

# Opção 2: comando direto (requer Maven no PATH)
mvn test -B
# ou com wrapper, se existir:
./mvnw test -B
```

**Requisito:** Maven instalado (`brew install maven` no macOS) ou Maven Wrapper no projeto (`mvn wrapper:wrapper` gera `mvnw`).

Outros comandos úteis:

```bash
# Todos os testes (unitários + BDD)
./mvnw clean test

# Testes BDD (15 cenários Cucumber)
./mvnw test -Dtest=CucumberTest

# Relatório de cobertura (≥75%)
./mvnw jacoco:report
open target/site/jacoco/index.html

# Qualidade de código
./mvnw checkstyle:check
./mvnw spotbugs:check
```

### Deploy no Kubernetes

```bash
# Build da imagem Docker
docker build -t billing-service:latest .

# Deploy local (kind/minikube)
kubectl apply -k k8s/

# Verificar pods
kubectl get pods -n billing-service

# Port forward para teste
kubectl port-forward -n billing-service svc/billing-service 8080:8080
```

Ver mais detalhes em [`k8s/README.md`](k8s/README.md)

---

## 📚 Documentação

### Principais Documentos

| Documento | Descrição |
|-----------|-----------|
| [`PLANO-BILLING-SERVICE-PESSOA2.md`](PLANO-BILLING-SERVICE-PESSOA2.md) | Plano detalhado de implementação (6 fases) |
| [`ALINHAMENTO-GRUPO.md`](ALINHAMENTO-GRUPO.md) | Pontos críticos para alinhar com o grupo |
| [`EVENT-CONTRACTS.md`](EVENT-CONTRACTS.md) | Contratos de eventos entre microsserviços |
| [`PROXIMOS-PASSOS.md`](PROXIMOS-PASSOS.md) | Guia para subir repositório e integrar |
| [`k8s/README.md`](k8s/README.md) | Documentação completa de Kubernetes |
| [`.github/workflows/README.md`](.github/workflows/README.md) | Documentação dos pipelines CI/CD |

### Fases de Implementação

- ✅ [**Fase 1**](FASE1-CONCLUIDA.md) - Estrutura inicial do projeto
- ✅ [**Fase 2**](FASE2-CONCLUIDA.md) - Modelagem de domínio e banco de dados
- ✅ [**Fase 3**](FASE3-CONCLUIDA.md) - Comunicação e integração
- ✅ [**Fase 4**](FASE4-CONCLUIDA.md) - Testes e qualidade
- ✅ [**Fase 5**](FASE5-CONCLUIDA.md) - Containerização e Kubernetes
- ✅ [**Fase 6**](FASE6-CONCLUIDA.md) - Pipeline CI/CD

---

## 🗂️ Estrutura do Projeto

```
fiap-techchallenge-billing-service/
├── src/
│   ├── main/java/br/com/techchallenge/fiap/billingservice/
│   │   ├── application/          # Application Layer (Use Cases, Gateways, Presenters)
│   │   │   ├── controller/       # Clean Arch Controllers (Portas)
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # Domain Entities (Budget, Payment)
│   │   │   ├── exception/        # Business Exceptions
│   │   │   ├── gateway/          # Gateway Interfaces (Ports)
│   │   │   ├── presenter/        # Presenters
│   │   │   └── usecase/          # Use Cases
│   │   │       ├── budget/       # Budget Use Cases
│   │   │       └── payment/      # Payment Use Cases
│   │   ├── infrastructure/       # Infrastructure Layer
│   │   │   ├── config/           # Spring Configuration
│   │   │   ├── controller/       # REST Controllers
│   │   │   ├── exception/        # Global Exception Handler
│   │   │   ├── messaging/        # SQS (Events, Publishers, Consumers)
│   │   │   ├── orchestrator/     # Saga Orchestrators
│   │   │   ├── payment/          # Payment Gateway Simulator
│   │   │   └── repository/       # DynamoDB Repositories
│   │   │       └── dynamodb/     # DynamoDB Models & Mappers
│   │   └── BillingServiceApplication.java
│   ├── test/java/                # Unit Tests
│   │   ├── application/
│   │   │   ├── presenter/
│   │   │   └── usecase/
│   │   ├── infrastructure/
│   │   │   ├── controller/
│   │   │   ├── messaging/
│   │   │   ├── payment/
│   │   │   └── repository/
│   │   └── bdd/                  # BDD Tests (Cucumber)
│   │       ├── steps/
│   │       └── CucumberTest.java
│   └── test/resources/
│       └── features/             # Gherkin Feature Files (pt-BR)
├── k8s/                          # Kubernetes Manifests (12 files)
├── docs/                        # QUEUE_CONTRACT, DEPLOY_SETUP, MIGRACAO-INFRA
├── .github/workflows/            # GitHub Actions (7 workflows)
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

**Total:**
- 61 classes Java (~5450 linhas)
- 104 testes unitários
- 15 cenários BDD
- 75% cobertura de código

---

## 🔄 API Endpoints

### Budgets (Orçamentos)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/budgets` | Criar orçamento |
| `GET` | `/api/v1/budgets/{id}` | Buscar por ID |
| `GET` | `/api/v1/budgets/service-order/{id}` | Buscar por Ordem de Serviço |
| `GET` | `/api/v1/budgets` | Listar todos (paginado) |
| `PUT` | `/api/v1/budgets/{id}/approve` | Aprovar orçamento |
| `PUT` | `/api/v1/budgets/{id}/reject` | Rejeitar orçamento |

### Payments (Pagamentos)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/payments` | Processar pagamento |
| `GET` | `/api/v1/payments/{id}` | Buscar por ID |
| `GET` | `/api/v1/payments/budget/{id}` | Buscar por orçamento |
| `GET` | `/api/v1/payments/service-order/{serviceOrderId}` | Buscar por Ordem de Serviço |
| `GET` | `/api/v1/payments` | Listar todos (paginado) |
| `PUT` | `/api/v1/payments/{id}/refund` | Estornar pagamento |

### Actuator (Monitoramento)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/actuator/health` | Health check |
| `GET` | `/actuator/metrics` | Métricas Prometheus |
| `GET` | `/actuator/info` | Informações da aplicação |

---

## 📊 Estatísticas

### Código

- **Classes Java:** 61 (~5450 linhas)
  - Domain: 10 classes
  - DTOs: 8 classes
  - Use Cases: 7 classes
  - Controllers: 4 classes
  - Repositories: 2 classes
  - Gateways: 2 classes
  - Presenters: 2 classes
  - Events: 6 classes
  - Orchestrators: 2 classes
  - Simulators: 1 classe
  - Config: 4 classes

### Testes

- **Unit Tests:** 104 testes (~2800 linhas)
  - Use Cases: 39 testes
  - Controllers: 4 testes
  - Presenters: 4 testes
  - Mappers: 6 testes
  - Gateway Simulator: 10 testes
  - Publisher: 5 testes
  - Domain Entities: 36 testes

- **BDD Tests:** 15 cenários (~450 linhas)
  - Budget Approval Flow: 3 cenários
  - Payment Processing Flow: 5 cenários
  - Saga Compensation Flow: 7 cenários

- **Cobertura:** 75% (meta: ≥75%)

### Infraestrutura

- **Kubernetes:** 12 manifestos YAML (~950 linhas)
- **Infraestrutura:** DynamoDB, SQS e IRSA no repositório **fiap-techchallenge-infra-database** (terraform: dynamodb.tf, sqs.tf, iam.tf)
- **CI/CD:** 7 workflows GitHub Actions (~1200 linhas)
- **Docker:** 1 Dockerfile multi-stage + docker-compose

---

## 🎯 Qualidade de Código

### Ferramentas

- ✅ **JaCoCo** - Cobertura de testes (≥75%)
- ✅ **Checkstyle** - Estilo de código (Google Java Style)
- ✅ **SpotBugs** - Análise estática de bugs
- ✅ **SonarCloud** - Análise de qualidade
- ✅ **Trivy** - Scan de vulnerabilidades em imagem Docker
- ✅ **OWASP Dependency Check** - Vulnerabilidades em dependências

### Gates de Qualidade

| Gate | Threshold | Status |
|------|-----------|--------|
| Coverage | ≥75% | ✅ 75% |
| Duplicação | ≤3% | ✅ |
| Maintainability | Rating A | ✅ |
| Reliability | Rating A | ✅ |
| Security | Rating A | ✅ |
| Vulnerabilities | 0 High | ✅ |

---

## 🔧 Configuração

### Variáveis de Ambiente (application.yml)

```yaml
# AWS
AWS_REGION: us-east-1
AWS_ACCESS_KEY_ID: your-key
AWS_SECRET_ACCESS_KEY: your-secret

# DynamoDB
DYNAMODB_ENDPOINT: http://localhost:4566  # LocalStack
DYNAMODB_TABLE_BUDGETS: billing-service-budgets
DYNAMODB_TABLE_PAYMENTS: billing-service-payments

# SQS
SQS_ENDPOINT: http://localhost:4566  # LocalStack
SQS_QUEUE_SERVICE_ORDER_EVENTS: service-order-events.fifo
SQS_QUEUE_BILLING_EVENTS: billing-events.fifo

# Payment Gateway Simulator
PAYMENT_GATEWAY_SUCCESS_RATE: "0.90"   # Configurado no ConfigMap
PAYMENT_GATEWAY_MIN_DELAY_MS: "2000"   # Configurado no ConfigMap
PAYMENT_GATEWAY_MAX_DELAY_MS: "5000"   # Configurado no ConfigMap
# Nota: atualmente o PaymentGatewaySimulator usa valores hardcoded
# (90%, 2000-5000ms). As variáveis acima estão no ConfigMap como
# preparação para futura externalização via @Value.
```

### Secrets Kubernetes

O Billing Service utiliza **IRSA** (IAM Roles for Service Accounts) para acesso a recursos AWS — não há `secret.yaml` neste repositório. Credenciais sensíveis são gerenciadas via Secrets no cluster (provisionados pela infra Terraform). Ver [`.github/secrets-example.md`](.github/secrets-example.md)

---

## 🧪 Testes

### Executar Específicos

```bash
# Testes de Use Cases
./mvnw test -Dtest=*UseCaseTest

# Testes de Controllers
./mvnw test -Dtest=*ControllerTest

# Testes de Mappers
./mvnw test -Dtest=*MapperTest

# Testes BDD
./mvnw test -Dtest=CucumberTest
```

### Cenários BDD (Gherkin pt-BR)

#### 1. Budget Approval Flow

```gherkin
Cenário: Criação automática de orçamento ao receber evento de OS
  Dado que uma nova ordem de serviço foi criada
  Quando o evento ServiceOrderCreatedEvent é recebido
  Então um orçamento deve ser criado automaticamente
  E o status inicial deve ser PENDING_APPROVAL
```

#### 2. Payment Processing Flow

```gherkin
Cenário: Processamento de pagamento com sucesso
  Dado que existe um orçamento aprovado
  Quando um pagamento é solicitado com método válido
  Então o pagamento deve ser processado
  E o status deve ser PAID
  E um evento PaymentProcessedEvent deve ser publicado
```

#### 3. Saga Compensation Flow

```gherkin
Cenário: Compensação de pagamento em caso de falha na execução
  Dado que um pagamento foi processado com sucesso
  Quando ocorre uma falha na execução do serviço
  Então um estorno deve ser realizado automaticamente
  E um evento PaymentRefundedEvent deve ser publicado
```

---

## 🚢 Deploy

### CI/CD Pipeline

```mermaid
graph LR
    A[Push/PR] --> B[Build & Test]
    B --> C[Code Quality]
    C --> D[Docker Build]
    D --> E[Deploy to EKS]
    E --> F[Smoke Tests]
    
    C --> G{Quality Gates}
    G -->|Pass| D
    G -->|Fail| H[Block]
    
    F -->|Success| I[✅ Done]
    F -->|Fail| J[Rollback]
```

### Workflows GitHub Actions

1. **Build & Test** - Compila e executa testes
2. **Code Quality** - JaCoCo, Checkstyle, SpotBugs, SonarCloud
3. **Docker Build & Push** - Build e push para ECR
4. **Deploy to EKS** - Deploy automatizado no Kubernetes
5. **Release** - Cria releases e changelogs
6. **PR Validation** - Valida Pull Requests
7. **Dependency Review** - Scan de segurança de dependências

Ver [`.github/workflows/README.md`](.github/workflows/README.md)

---

## 🔐 Segurança

### Boas Práticas Implementadas

- ✅ **Container não-root** - Usuário `spring:spring` (UID 1000)
- ✅ **Security Context** - Kubernetes Pod Security
- ✅ **Network Policy** - Controle de tráfego
- ✅ **RBAC** - Least privilege
- ✅ **IRSA** - IAM Roles for Service Accounts (ready)
- ✅ **Secrets** - Não commitados, via K8s Secrets
- ✅ **Image Scanning** - Trivy no CI
- ✅ **Dependency Scanning** - OWASP Dependency Check
- ✅ **Input Validation** - Bean Validation em todos os DTOs
- ✅ **Conditional JWT** - `SecurityConfig` habilita OAuth2/JWT quando `spring.security.oauth2.resourceserver.jwt.issuer-uri` está configurado; caso contrário (ambiente local/Docker sem OIDC), desabilita JWT e libera todas as rotas

---

## 📈 Observabilidade

### Métricas

- **Spring Boot Actuator** - `/actuator/metrics`
- **Prometheus** - Annotations nos Services K8s
- **JVM Metrics** - Memory, GC, Threads

### Logs

- **Formato:** JSON structured logging
- **Nível:** INFO (prod), DEBUG (dev)
- **Destino:** stdout → CloudWatch Logs

### Probes (Kubernetes)

```yaml
livenessProbe:   /actuator/health/liveness
readinessProbe:  /actuator/health/readiness
startupProbe:    /actuator/health
```

---

## 🤝 Integração com Outros Microsserviços

### Eventos Consumidos

| Evento | Origem | Ação |
|--------|--------|------|
| `ServiceOrderCreatedEvent` | OS Service | Criar budget automaticamente |

> **Nota:** Atualmente o consumer (`ServiceOrderEventConsumer`) processa apenas eventos do tipo `ORDER_CREATED`. Outros tipos de evento recebidos na fila são descartados silenciosamente (log DEBUG). O tratamento de `ServiceOrderCancelledEvent` (cancelar budget / estornar) é um ponto de extensão futuro.

### Eventos Publicados

| Evento | Destino | Trigger |
|--------|---------|---------|
| `BudgetApprovedEvent` | OS Service | Budget aprovado |
| `BudgetRejectedEvent` | OS Service | Budget rejeitado |
| `PaymentProcessedEvent` | OS/Execution | Pagamento OK |
| `PaymentFailedEvent` | OS Service | Falha no pagamento |
| `PaymentRefundedEvent` | OS Service | Estorno realizado |

Ver [`EVENT-CONTRACTS.md`](EVENT-CONTRACTS.md) para schemas completos

---

## 🏆 Características Técnicas

### Patterns Implementados

- ✅ **Clean Architecture** - Separação de camadas
- ✅ **Hexagonal Architecture** - Ports & Adapters
- ✅ **DDD** - Domain-Driven Design
- ✅ **Saga Pattern** - Choreographed
- ✅ **CQRS** - Separação de comandos e consultas
- ✅ **Repository Pattern** - Abstração de persistência
- ✅ **Use Case Pattern** - Casos de uso explícitos
- ✅ **Builder Pattern** - Construção de entidades
- ✅ **Factory Pattern** - Criação de objetos complexos

### Princípios SOLID

- ✅ **SRP** - Single Responsibility Principle
- ✅ **OCP** - Open/Closed Principle
- ✅ **LSP** - Liskov Substitution Principle
- ✅ **ISP** - Interface Segregation Principle
- ✅ **DIP** - Dependency Inversion Principle

---

## 📞 Equipe

**Tech Challenge FIAP 12SOAT - Fase 4**

| Pessoa | Responsabilidade | Serviço |
|--------|------------------|---------|
| **Pessoa 1** | Ordem de Serviço | OS Service |
| **Pessoa 2** (você) | Orçamento e Pagamento | **Billing Service** |
| **Pessoa 3** | Execução + Infra/DevOps | Execution Service + Infra |

---

## 📄 Licença

Este projeto é parte do Tech Challenge FIAP 12SOAT.

---

## 🔗 Links Úteis

- [Tech Challenge 12SOAT - Fase 4](12SOAT%20-%20Fase%204%20-%20Tech%20challenge.txt)
- [Divisão de Responsabilidades](DIVISAO-RESPONSABILIDADES-FASE4.md)
- [Plano de Implementação](PLANO-BILLING-SERVICE-PESSOA2.md)
- [Alinhamento com Grupo](ALINHAMENTO-GRUPO.md)
- [Contratos de Eventos](EVENT-CONTRACTS.md)
- [Próximos Passos](PROXIMOS-PASSOS.md)

---

## 🎉 Status

✅ **PROJETO 100% COMPLETO** - Pronto para integração e deploy!

**Última atualização:** 12 de Fevereiro de 2026
