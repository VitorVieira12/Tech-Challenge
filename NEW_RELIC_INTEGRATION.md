# Integração New Relic - Aplicação

## 1. Adicionar Dependência no pom.xml

```xml
<dependency>
    <groupId>com.newrelic.agent.java</groupId>
    <artifactId>newrelic-java</artifactId>
    <version>8.9.0</version>
</dependency>
```

## 2. Atualizar Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Download e instalar New Relic Agent
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip /tmp/
RUN unzip /tmp/newrelic-java.zip -d /opt/ && \
    rm /tmp/newrelic-java.zip

# Copiar JAR da aplicação
COPY target/Tech-Challenge-0.0.1-SNAPSHOT.jar app.jar

# Expor porta
EXPOSE 8080

# Rodar com New Relic agent
ENTRYPOINT ["java", \
  "-javaagent:/opt/newrelic/newrelic.jar", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-jar", \
  "app.jar"]
```

## 3. ConfigMap Kubernetes

Criar `k8s/newrelic-config.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: newrelic-config
  namespace: tech-challenge
data:
  NEW_RELIC_APP_NAME: "Tech Challenge"
  NEW_RELIC_LOG_FILE_NAME: "STDOUT"
  NEW_RELIC_LOG_LEVEL: "info"
  NEW_RELIC_DISTRIBUTED_TRACING_ENABLED: "true"
  NEW_RELIC_LABELS: "env:production;project:tech-challenge"
```

## 4. Secret Kubernetes

Criar `k8s/newrelic-secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: newrelic-secret
  namespace: tech-challenge
type: Opaque
stringData:
  license-key: "SEU_NEW_RELIC_LICENSE_KEY_AQUI"
```

## 5. Atualizar Deployment

Atualizar `k8s/app-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tech-challenge-app
  namespace: tech-challenge
spec:
  replicas: 2
  selector:
    matchLabels:
      app: tech-challenge
  template:
    metadata:
      labels:
        app: tech-challenge
    spec:
      containers:
      - name: app
        image: seu-usuario/tech-challenge:latest
        ports:
        - containerPort: 8080
        env:
        # New Relic
        - name: NEW_RELIC_LICENSE_KEY
          valueFrom:
            secretKeyRef:
              name: newrelic-secret
              key: license-key
        - name: NEW_RELIC_APP_NAME
          valueFrom:
            configMapKeyRef:
              name: newrelic-config
              key: NEW_RELIC_APP_NAME
        - name: NEW_RELIC_LOG_FILE_NAME
          valueFrom:
            configMapKeyRef:
              name: newrelic-config
              key: NEW_RELIC_LOG_FILE_NAME
        - name: NEW_RELIC_DISTRIBUTED_TRACING_ENABLED
          valueFrom:
            configMapKeyRef:
              name: newrelic-config
              key: NEW_RELIC_DISTRIBUTED_TRACING_ENABLED
        # Database
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: DB_HOST
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: "tech_challenge"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        # JWT
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

## 6. Deploy

```bash
# Aplicar configurações
kubectl apply -f k8s/newrelic-config.yaml
kubectl apply -f k8s/newrelic-secret.yaml
kubectl apply -f k8s/app-deployment.yaml

# Verificar pods
kubectl get pods -n tech-challenge

# Ver logs
kubectl logs -f deployment/tech-challenge-app -n tech-challenge
```

## 7. Verificar New Relic

1. Acesse https://one.newrelic.com
2. Menu "APM & Services"
3. Encontre "Tech Challenge"
4. Verifique métricas:
   - Response time
   - Throughput
   - Error rate
   - Transactions
   - Distributed tracing

## 8. Dashboards Recomendados

### Dashboard 1: Overview
- Response time (avg, P95, P99)
- Throughput (requests/min)
- Error rate (%)
- Apdex score

### Dashboard 2: Infrastructure
- CPU usage
- Memory usage
- Pod count
- Network I/O

### Dashboard 3: Database
- Database query time
- Slow queries
- Connection pool usage

### Dashboard 4: JVM
- Heap memory
- Garbage collection
- Thread count

## 9. Alertas Recomendados

```yaml
# Alert 1: High Error Rate
condition: error_rate > 5%
duration: 5 minutes
action: notify Slack/Email

# Alert 2: High Response Time
condition: response_time_p95 > 2 seconds
duration: 10 minutes
action: notify Slack/Email

# Alert 3: High CPU
condition: cpu_usage > 80%
duration: 5 minutes
action: scale pods

# Alert 4: Low Throughput
condition: throughput < 10 rpm
duration: 15 minutes
action: investigate
```

## 10. Logs Estruturados

Adicionar em `application.yml`:

```yaml
logging:
  pattern:
    console: '{"timestamp":"%d{ISO8601}","level":"%p","thread":"%t","class":"%c{1}","message":"%m"}%n'
  level:
    root: INFO
    com.techchallenge: DEBUG
```

---

**New Relic totalmente integrado e configurado!** 📊

