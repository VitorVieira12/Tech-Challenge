# tech-challenge-lambda

AWS Lambda Function para autenticação serverless por CPF/CNPJ

## 📋 Descrição

Function Lambda que:
1. Recebe CPF/CNPJ via POST
2. Valida CPF/CNPJ (algoritmo)
3. Consulta cliente no RDS PostgreSQL
4. Gera JWT token se cliente existir e estiver ativo
5. Retorna token ou erro 401

## 🚀 Tecnologias

- **Runtime**: Java 21 + GraalVM Native
- **Framework**: AWS Lambda Java Events
- **Database**: PostgreSQL (AWS RDS)
- **JWT**: jjwt library
- **Build**: Maven + AWS SAM
- **CI/CD**: GitHub Actions

## 📦 Estrutura do Projeto

```
tech-challenge-lambda/
├── src/
│   ├── main/
│   │   └── java/com/lambda/
│   │       ├── AuthHandler.java          # Handler principal
│   │       ├── model/
│   │       │   ├── AuthRequest.java       # DTO de entrada
│   │       │   ├── AuthResponse.java      # DTO de saída
│   │       │   └── Cliente.java           # Entity simples
│   │       ├── service/
│   │       │   ├── CpfValidator.java      # Validador de CPF/CNPJ
│   │       │   ├── JwtService.java        # Gerador de JWT
│   │       │   └── ClienteService.java    # Consulta ao banco
│   │       └── repository/
│   │           └── ClienteRepository.java # JDBC simples
│   └── test/
│       └── java/com/lambda/
│           ├── AuthHandlerTest.java
│           └── CpfValidatorTest.java
├── template.yaml                         # AWS SAM template
├── pom.xml
├── .github/
│   └── workflows/
│       └── deploy.yml                    # CI/CD pipeline
├── README.md
└── .gitignore
```

## 🛠️ Setup Local

### Pré-requisitos

- Java 21
- Maven 3.9+
- AWS CLI configurado
- AWS SAM CLI
- Docker (para testes locais)

### Instalação

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/tech-challenge-lambda.git
cd tech-challenge-lambda

# Build
mvn clean package

# Testes
mvn test

# Build imagem nativa (opcional, mais rápido)
mvn package -Pnative
```

### Executar Localmente

```bash
# Iniciar Lambda localmente
sam local start-api

# Testar endpoint
curl -X POST http://localhost:3000/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678901"}'
```

## 🚀 Deploy

### Deploy Manual

```bash
# Build
sam build

# Deploy
sam deploy --guided
```

### Deploy Automático (CI/CD)

Push para branch `main` aciona GitHub Actions que faz deploy automático.

## 🔐 Variáveis de Ambiente

Configure no AWS Lambda:

```bash
DB_HOST=tech-challenge.xxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=tech_challenge
DB_USERNAME=admin
DB_PASSWORD=<secret>
JWT_SECRET=<sua-chave-secreta-256-bits>
JWT_EXPIRATION=86400000  # 24 horas em ms
```

## 📡 API

### POST /auth

**Request:**
```json
{
  "cpf": "12345678901"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "clienteId": 123,
  "clienteNome": "João Silva"
}
```

**Response 400 (CPF inválido):**
```json
{
  "message": "CPF inválido",
  "statusCode": 400
}
```

**Response 401 (Cliente não encontrado/inativo):**
```json
{
  "message": "Cliente não encontrado ou inativo",
  "statusCode": 401
}
```

## 🧪 Testes

```bash
# Testes unitários
mvn test

# Cobertura
mvn jacoco:report
# Relatório em: target/site/jacoco/index.html

# Teste integração (local)
sam local invoke AuthFunction --event events/auth-event.json
```

## 📊 Monitoramento

### CloudWatch Logs

```bash
# Ver logs
aws logs tail /aws/lambda/tech-challenge-auth-cpf --follow
```

### Métricas

- Invocações
- Erros
- Duração (target: < 500ms com cold start)
- Throttles

## 🔄 CI/CD Pipeline

O pipeline GitHub Actions executa:

1. **Build & Test**
   - Checkout código
   - Setup Java 21
   - Maven test
   - Jacoco coverage check

2. **Build Native Image**
   - GraalVM native-image
   - Otimização para cold start

3. **Deploy Lambda**
   - AWS credentials
   - `sam build`
   - `sam deploy --no-confirm-changeset`

4. **Integration Test**
   - Invoke Lambda
   - Validar resposta

## 🏗️ Arquitetura

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ POST /auth/cpf
       ▼
┌────────────────────┐
│   API Gateway      │
└────────┬───────────┘
         │ Invoke
         ▼
┌───────────────────────────────┐
│  Lambda: AuthFunction         │
│                               │
│  1. Validate CPF              │
│  2. Query RDS (JDBC)          │
│  3. Generate JWT              │
│  4. Return token              │
└────────┬──────────────────────┘
         │
         ▼
┌────────────────────┐
│   RDS PostgreSQL   │
│   (tech_challenge) │
└────────────────────┘
```

## 🔧 Troubleshooting

### Cold Start Lento

- ✅ Usar GraalVM native image
- ✅ Provisioned Concurrency (se necessário)
- ✅ Otimizar dependências Maven

### Erro de Conexão com RDS

- Verificar Security Group do RDS
- Lambda deve estar na mesma VPC
- Subnet privada com NAT Gateway

### Token JWT Inválido

- Verificar JWT_SECRET (deve ser o mesmo em todas apps)
- Verificar expiração

## 📝 Licença

MIT

## 👥 Autores

Tech Challenge Team - Fase 3

---

**Lambda serverless para autenticação escalável e de baixo custo!** ⚡

