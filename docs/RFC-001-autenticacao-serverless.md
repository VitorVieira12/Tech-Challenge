# RFC-001: Autenticação Serverless via AWS Lambda + JWT

**Status:** Implementado  
**Data:** 2026-03-01  
**Autores:** Tech Challenge Team  
**Revisores:** FIAP 13SOAT

---

## Resumo

Este RFC descreve o design e implementação do módulo de autenticação da plataforma Tech Challenge Fase 3, utilizando uma **AWS Lambda Function** para validar clientes pelo CPF e gerar tokens JWT.

---

## Motivação

O requisito da Fase 3 exige que:
1. A autenticação seja feita por CPF/CNPJ (sem senha)
2. O módulo de autenticação seja implementado como **função serverless** (Lambda)
3. A função seja exposta via **API Gateway**
4. O resultado seja um **JWT Token** utilizável na aplicação principal

---

## Design Detalhado

### Fluxo de Autenticação

```
POST /auth
Body: { "cpf": "123.456.789-00" }

1. API Gateway recebe request → invoca Lambda
2. Lambda valida formato do CPF (regex)
3. Lambda consulta RDS: SELECT id, ativo FROM clientes WHERE cpf = ?
4. Se não encontrado → 401 { "error": "Cliente não encontrado" }
5. Se inativo → 401 { "error": "Cliente inativo" }
6. Se válido → gera JWT (HMAC-SHA256, 24h) → 200 { "token": "eyJ..." }
```

### Estrutura do JWT

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "123.456.789-00",
    "clienteId": 1,
    "iat": 1742000000,
    "exp": 1742086400
  }
}
```

### Configuração do Lambda

```yaml
# template.yaml (AWS SAM)
Resources:
  AuthFunction:
    Type: AWS::Serverless::Function
    Properties:
      Runtime: java21
      Handler: com.lambda.AuthHandler::handleRequest
      MemorySize: 512
      Timeout: 30
      Environment:
        Variables:
          DB_HOST: !Ref DBHost
          DB_USERNAME: !Ref DBUsername
          DB_PASSWORD: !Ref DBPassword
          JWT_SECRET: !Ref JWTSecret
```

### Variáveis de Ambiente

| Variável | Descrição | Origem |
|---|---|---|
| `DB_HOST` | Endpoint RDS | GitHub Secret |
| `DB_USERNAME` | Usuário PostgreSQL | GitHub Secret |
| `DB_PASSWORD` | Senha PostgreSQL | GitHub Secret |
| `JWT_SECRET` | Chave HMAC para assinar JWT | GitHub Secret |
| `DB_NAME` | Nome do banco (`tech_challenge`) | Hardcoded |
| `DB_PORT` | Porta PostgreSQL (`5432`) | Hardcoded |

---

## Segurança

- **JWT assinado com HMAC-SHA256** (não encriptado — payload é base64)
- **Segredo JWT compartilhado** entre Lambda e Spring Boot App
- **Expiração de 24 horas** — cliente deve re-autenticar diariamente
- **Validação de formato CPF** antes de consultar o banco
- **Sem logs de CPF/tokens** — apenas IDs e status

---

## Performance

- **Cold Start**: ~2-5 segundos (Java 21 na Lambda)
- **Warm requests**: <500ms
- **Timeout configurado**: 30 segundos
- **Memory**: 512MB (otimizado para startup do JVM)

### Mitigação de Cold Start
- Provisioned Concurrency (opcional, custo adicional)
- SnapStart (Java 21 suporte beta na Lambda)

---

## Alternativas Rejeitadas

### Autenticação no Spring Boot
Seria mais simples, mas não cumpriria o requisito de Lambda Serverless.

### Amazon Cognito
Overcomplexity para o caso de uso. Cognito é ideal para auth social (Google, Facebook) ou casos com MFA, que não são requisitos aqui.

### JWT com RS256 (assimétrico)
Melhor segurança (chave pública para verificação), mas aumenta complexidade de gerenciamento de chaves. HS256 é suficiente para o escopo do projeto.

---

## Implementação

### AuthHandler.java

```java
public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {
        
        // 1. Parse CPF do body
        String cpf = parseCpf(event.getBody());
        
        // 2. Consulta RDS
        Optional<Cliente> cliente = findClienteByCpf(cpf);
        if (cliente.isEmpty() || !cliente.get().isAtivo()) {
            return buildResponse(401, "{\"error\": \"Não autorizado\"}");
        }
        
        // 3. Gera JWT
        String token = generateJwt(cpf, cliente.get().getId());
        
        return buildResponse(200, 
            "{\"token\": \"" + token + "\", \"type\": \"Bearer\"}");
    }
}
```

---

## Monitoramento

- **CloudWatch Logs**: logs automáticos da Lambda
- **CloudWatch Metrics**: invocações, erros, duração, throttles
- **API Gateway Metrics**: requests, latência, 4xx/5xx errors

---

## Rollback

Para rollback, basta re-executar o workflow `Deploy Lambda` apontando para o commit anterior via `workflow_dispatch`.

---

## Referências

- [AWS Lambda Java 21](https://docs.aws.amazon.com/lambda/latest/dg/java-package.html)
- [JWT.io](https://jwt.io/)
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html)
