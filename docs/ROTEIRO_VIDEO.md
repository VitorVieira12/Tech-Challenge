# Roteiro de Vídeo — Tech Challenge Fase 4

> **Tempo total estimado:** 12–14 minutos (limite do challenge: 15 min)
> **Formato:** YouTube/Vimeo público ou não-listado
> **Ferramentas na tela:** navegador, terminal PowerShell, VSCode

---

## 0. Preparação ANTES de apertar REC (10–15 min)

### 0.1 Janelas/abas que devem estar abertas

**Navegador (separe em tabs nesta ordem):**

1. `https://github.com/VitorVieira12/Tech-Challenge` (OS Service)
2. `https://github.com/VitorVieira12/tech-challenge-billing-service`
3. `https://github.com/VitorVieira12/tech-challenge-execution-service`
4. `https://github.com/VitorVieira12/Tech-Challenge/actions` (CI/CD verde)
5. `https://sonarcloud.io/project/overview?id=VitorVieira12_Tech-Challenge` (Sonar)
6. `https://one.newrelic.com` (já logado) — APM & Services
7. `http://aedfcadcab1a6470a9138cf64ae56407-787906964.us-east-1.elb.amazonaws.com/swagger-ui.html` (Swagger OS)
8. `http://a35b4dcfcb7cb4487a4ab7175fda6c4b-848840900.us-east-1.elb.amazonaws.com/swagger-ui.html` (Swagger Billing)
9. `http://a305e8e2cb34347939684c26616f618e-1728333382.us-east-1.elb.amazonaws.com/swagger-ui.html` (Swagger Execution)
10. RabbitMQ Management UI (port-forward — passo 0.3)

**VSCode aberto em:**

- `docs/ARQUITETURA_FASE4.md` (mostrar diagrama Mermaid renderizado — use o preview)
- `README.md`
- `.github/workflows/ci-cd-os-service.yml` (mostrar pipeline)

**Terminal PowerShell (4 abas/painéis):**

- **A)** demo Saga (`scripts/demo-saga.ps1`)
- **B)** `kubectl` (logs/eventos ao vivo)
- **C)** RabbitMQ port-forward (deixar rodando)
- **D)** livre para qualquer comando ad-hoc

### 0.2 Verificações rápidas (rode antes de gravar)

```powershell
# Tudo verde?
kubectl get pods -A | Select-String -Pattern "(os-service|billing|execution|rabbit|mongo)"

# Health dos 3 serviços
$lbs = @{
  os       = "http://aedfcadcab1a6470a9138cf64ae56407-787906964.us-east-1.elb.amazonaws.com"
  billing  = "http://a35b4dcfcb7cb4487a4ab7175fda6c4b-848840900.us-east-1.elb.amazonaws.com"
  exec     = "http://a305e8e2cb34347939684c26616f618e-1728333382.us-east-1.elb.amazonaws.com"
}
foreach ($k in $lbs.Keys) {
  $r = Invoke-RestMethod "$($lbs[$k])/actuator/health"
  Write-Host "$k -> $($r.status)"
}
```

Resultado esperado: `os -> UP`, `billing -> UP`, `exec -> UP`.

### 0.3 Port-forward do RabbitMQ Management UI

Em uma aba de terminal dedicada (mantenha rodando o vídeo inteiro):

```powershell
kubectl port-forward -n rabbitmq svc/rabbitmq 15672:15672
```

Abra `http://localhost:15672` → login `guest` / `guest`. Vá em **Queues** e deixe a aba aberta (vai aparecer mensagens fluindo durante a demo).

### 0.4 Seed de dados (se RDS ainda não tiver)

Já está aplicado, mas se precisar repetir:

```powershell
psql -h tech-challenge-db.cgxmk6mmaeyg.us-east-1.rds.amazonaws.com -U postgres -d techdb -f k8s/seed-data.sql
```

### 0.5 Checklist visual final

- [ ] Câmera/microfone testados
- [ ] Notificações do desktop silenciadas (modo "Não perturbe")
- [ ] Resolução do navegador em zoom 110–125% (legível na gravação)
- [ ] Fonte do terminal grande (16–18pt)
- [ ] Tema claro no GitHub (mais legível em vídeo)

---

## 1. Abertura (0:00 – 0:45) — ~45s

**Falar:**

> "Olá, sou o **Vitor Vieira**, aluno da turma 13SOAT da pós-tech FIAP, e este é o vídeo de entrega do **Tech Challenge da Fase 4** — Arquitetura de Microsserviços com Saga Pattern.
>
> Nesta fase, refatorei o monolito da Fase 3 em **três microsserviços independentes**, cada um com seu próprio repositório, banco de dados e pipeline de CI/CD, comunicando-se de forma assíncrona via RabbitMQ. Vou demonstrar o fluxo completo de uma Ordem de Serviço passando pelos três serviços, o tratamento de falhas via Saga, o pipeline automatizado e o monitoramento distribuído."

**Mostrar na tela:** abra o README do OS Service no GitHub (a tabela com os 3 repositórios).

> "Esses são os três repositórios da fase, mais os três de infraestrutura herdados da Fase 3."

---

## 2. Arquitetura e justificativas (0:45 – 3:00) — ~2:15

**Mostrar na tela:** VSCode com `docs/ARQUITETURA_FASE4.md` aberto no preview.

**Falar (passe o diagrama Mermaid):**

> "Esta é a arquitetura final. Temos:
> - **OS Service** — núcleo do domínio, gerencia o ciclo de vida da Ordem de Serviço, em **PostgreSQL** (atende o requisito SQL).
> - **Billing Service** — bounded context financeiro: orçamentos e pagamentos, também em PostgreSQL pela necessidade de garantias ACID em dados financeiros.
> - **Execution Service** — gerencia a fila de execução do chão de oficina, em **MongoDB** (atende o requisito NoSQL). Escolhi NoSQL porque o documento de execução tem schema flexível, com etapas, técnico responsável, observações e fotos que evoluem por fase.
>
> Todos os três se comunicam via **RabbitMQ**, com três exchanges separadas (`os.events`, `billing.events`, `execution.events`). Nenhum serviço acessa o banco de outro — toda integração cross-service passa pelo broker.
>
> Para o Saga Pattern eu escolhi a **abordagem coreografada**, sem orquestrador central. As razões estão documentadas na seção 4: ausência de SPOF, menor acoplamento e adequação para o escopo de 3 serviços. A rastreabilidade fica por conta do **New Relic distributed tracing**, que herdei da Fase 3."

**Mostrar:** scroll até a seção "Saga Pattern — Coreografado" do README — passe rápido pelo diagrama de sequência e pela tabela de compensações.

> "Aqui está o fluxo principal e as compensações de rollback: se o orçamento for rejeitado, a OS vai para CANCELADA; se a execução falhar, a OS volta para EM_DIAGNOSTICO. Vou demonstrar os dois fluxos."

---

## 3. Os 3 repositórios separados, com branch protection e PRs (3:00 – 4:00) — ~1:00

**Mostrar:** GitHub do OS Service.

> "Cada microsserviço está em seu próprio repositório. A branch `main` dos três repositórios está **protegida**: exige Pull Request aprovado e checks verdes para qualquer merge."

**Mostrar:** Settings → Branches → mostre as rules ativas em `main`.

**Mostrar:** aba "Pull Requests" → "Closed" → mostre o PR mais recente mergeado (por exemplo PR #7 do OS Service).

> "Toda entrega da Fase 4 foi feita via PR — não há commits diretos em `main`."

Repita rápido para os outros dois repos (mostrando só a tab "Pull requests" mergeada).

---

## 4. CI/CD automatizado (4:00 – 5:30) — ~1:30

**Mostrar:** GitHub Actions do OS Service (aba Actions).

> "Cada um dos três serviços tem seu próprio pipeline independente. Veja o último run."

**Mostrar:** clique no último run verde → expanda o job. Mostre as 3 etapas que aparecem no workflow:

1. **Build & Test** (mvn verify + JaCoCo + SonarCloud)
2. **Docker Build & Push** (Docker Hub)
3. **Deploy EKS** (kubectl apply)

> "São três estágios: build com testes e gate do JaCoCo de 80%, análise estática no SonarCloud, build da imagem Docker e push para o Docker Hub, e finalmente o deploy automatizado no cluster EKS via `kubectl apply`."

**Mostrar:** abra o arquivo `.github/workflows/ci-cd-os-service.yml` no VSCode rapidamente — só para evidenciar a estrutura YAML.

---

## 5. Testes, cobertura e qualidade (5:30 – 7:00) — ~1:30

**Mostrar:** SonarCloud — `https://sonarcloud.io/project/overview?id=VitorVieira12_Tech-Challenge`.

> "O SonarCloud é o nosso Quality Gate. Aqui estão as métricas do OS Service: coverage, code smells, vulnerabilidades, duplicações. O Quality Gate está passando."

**Mostrar:** rapidamente o overview dos 3 projetos no Sonar (se tiver os 3 cadastrados).

**Mostrar:** abra o arquivo BDD `src/test/resources/features/ordem_servico.feature` no VSCode.

> "Para BDD eu usei Cucumber. Esta feature cobre o ciclo completo do Saga com 6 cenários — incluindo os dois rollbacks: orçamento rejeitado e execução falhou."

**Mostrar:** abra o relatório JaCoCo localmente: `target/site/jacoco/index.html`.

> "A cobertura por serviço passa de 80%, que é o gate mínimo. O `mvn verify` quebra a build se cair abaixo disso."

**Comando se precisar rodar ao vivo:**

```powershell
# Da pasta de cada serviço
.\mvnw verify
```

---

## 6. Demonstração do Saga — fluxo feliz (7:00 – 10:30) — ~3:30

> "Agora a parte central: o fluxo completo de uma OS passando pelos três microsserviços, ao vivo, no ambiente AWS EKS."

**Mostrar:** RabbitMQ Management UI aberto (`http://localhost:15672`, Queues view).

> "À direita vocês veem as filas do RabbitMQ. Vão piscar mensagens conforme o Saga progride."

**Mostrar:** Terminal A, rode o script de demo:

```powershell
.\scripts\demo-saga.ps1 -Cenario feliz
```

O script faz, com pausas explicativas:

1. **Login** no OS Service (admin/admin) → recebe JWT
2. **POST /api/ordens-servico** → cria OS → status `EM_DIAGNOSTICO`, evento `os.criada` publicado
3. **Pausa 5s** → mostra que Billing consumiu e publicou `orcamento.gerado`
4. **GET /api/ordens-servico/{id}** → status agora é `AGUARDANDO_APROVACAO`
5. **GET /orcamentos/os/{osId}** no Billing → mostra orçamento criado
6. **POST /orcamentos/{id}/aprovar** → publica `orcamento.aprovado`
7. **Pausa 5s** → OS Service consumiu, status `EM_EXECUCAO`
8. **GET /execucoes/os/{osId}** no Execution → mostra que entrou na fila
9. **PATCH /execucoes/os/{osId}/status** → status `EM_ANDAMENTO`
10. **PATCH /execucoes/os/{osId}/status** → `FINALIZADA` → publica `execucao.finalizada`
11. **Pausa 5s** → OS Service consumiu, status final `FINALIZADA`
12. **GET /api/ordens-servico/{id}** → confirma `FINALIZADA`

**O que destacar enquanto roda:**

> "Repare o status mudando sem nenhuma chamada REST entre serviços — tudo coordenado por eventos. Cada serviço fala apenas com seu próprio banco e com o broker."

**Mostrar:** no RabbitMQ UI a coluna "Messages" subindo nas filas `billing.os.criada`, `os.orcamento.gerado`, etc.

---

## 7. Saga com falha — rollback/compensação (10:30 – 11:45) — ~1:15

> "Agora vou demonstrar o rollback do Saga: o cliente rejeita o orçamento."

**Comando:**

```powershell
.\scripts\demo-saga.ps1 -Cenario rollback
```

O script faz:

1. Cria nova OS → `EM_DIAGNOSTICO`
2. Aguarda Billing gerar orçamento → `AGUARDANDO_APROVACAO`
3. **POST /orcamentos/{id}/rejeitar?motivo=Preço elevado** → publica `orcamento.rejeitado`
4. OS Service consome → status `CANCELADA`
5. Confirma com `GET /api/ordens-servico/{id}`

**Falar:**

> "O evento `orcamento.rejeitado` foi consumido pelo OS Service que aplicou a compensação. A OS foi marcada como CANCELADA. Tudo isso sem nenhum orquestrador — apenas eventos. Esse é o coração da coreografia."

---

## 8. Monitoramento e tracing distribuído (11:45 – 13:00) — ~1:15

**Mostrar:** New Relic → APM & Services → escolha um dos 3 apps (`Tech Challenge - Oficina`).

> "O New Relic mostra cada um dos três microsserviços como uma aplicação independente, com seus próprios SLAs."

**Mostrar:** Distributed Tracing → escolha um trace que toque os 3 serviços (procure por um trace recente cobrindo `POST /api/ordens-servico`).

> "Aqui está um trace distribuído de uma das chamadas que fizemos. Veja como a transação atravessa OS Service, Billing Service e Execution Service via RabbitMQ. É exatamente o tipo de rastreabilidade que a documentação da Saga coreografada exige."

**Mostrar (opcional):** o dashboard customizado em `docs/newrelic-dashboard.json` (se já importado no NR).

---

## 9. Encerramento (13:00 – 13:30) — ~30s

**Mostrar:** README do OS Service, scroll até a seção "Entregáveis da Fase 4" (checklist ✅).

**Falar:**

> "Para fechar: os entregáveis da Fase 4 estão todos cumpridos — três microsserviços em repositórios separados, bancos SQL e NoSQL, Saga Pattern coreografado com compensações, testes unitários e BDD com cobertura acima de 80%, Quality Gate via SonarCloud, CI/CD por serviço com deploy automatizado em EKS e observabilidade via New Relic.
>
> Os links dos três repositórios estão na descrição do vídeo e no PDF entregue no portal. Obrigado!"

---

## 10. Dicas operacionais durante a gravação

### O que falar enquanto comandos longos rodam

- "Enquanto o pipeline executa, repare que…"
- "Esse pequeno delay é o tempo de propagação do evento pelo RabbitMQ — assíncrono por design."

### Se algo der errado AO VIVO

| Problema | Plano B |
|---|---|
| Health check fora do ar | Volte para o Swagger UI e mostre que o endpoint responde |
| Saga não muda status | Cheque o pod com `kubectl logs -n <ns> -l app=<svc> --tail=50` no Terminal B |
| RabbitMQ UI sem mensagens | Recarregue a página; mensagens consumidas rapidamente podem não aparecer |
| Mongo travado | Mostre que MongoDB está rodando em `kubectl get pods -n execution-service` |

### Comando de "reset" entre tomadas (caso queira regravar)

```powershell
# Limpa apenas as OSs da demo (ID > 6 que é o último do seed)
psql -h tech-challenge-db.cgxmk6mmaeyg.us-east-1.rds.amazonaws.com -U postgres -d techdb -c "DELETE FROM ordens_servico WHERE id > 6;"
```

---

## 11. Estrutura final do PDF a ser entregue no portal

Use este esqueleto:

1. **Capa** — Nome, RM, turma
2. **Links dos repositórios** (tabela com os 3 + os 3 de infra)
3. **Link do vídeo** (YouTube/Vimeo)
4. **Diagrama da arquitetura final** (exportar o Mermaid de `docs/ARQUITETURA_FASE4.md` como PNG — use https://mermaid.live)
5. **Saga Pattern** — colar o conteúdo da seção 4 do README
6. **Justificativa da divisão dos microsserviços** — colar a tabela "Justificativa da Divisão em Microsserviços" do README
7. **Justificativa das tecnologias** — colar a tabela "Bancos de Dados (SQL + NoSQL)"

---

## 12. Resumo dos comandos prontos

### Login no OS Service
```powershell
$os = "http://aedfcadcab1a6470a9138cf64ae56407-787906964.us-east-1.elb.amazonaws.com"
$body = '{ "username":"admin", "password":"admin" }'
$r = Invoke-RestMethod -Uri "$os/api/auth/login" -Method POST -ContentType "application/json" -Body $body
$jwt = $r.token
```

### Criar OS
```powershell
$payload = @{
  cpfCnpjCliente = "12674781602"
  veiculo = @{ placa = "ABC1234" }
  servicos = @(@{ servicoId = 1; quantidade = 1 })
  pecas = @(@{ pecaInsumoId = 3; quantidade = 1 })
  observacoes = "Demo do vídeo Fase 4"
} | ConvertTo-Json -Depth 5
$os1 = Invoke-RestMethod -Uri "$os/api/ordens-servico" -Method POST -Headers @{ Authorization = "Bearer $jwt" } -ContentType "application/json" -Body $payload
$os1
```

### Aprovar orçamento (Billing)
```powershell
$billing = "http://a35b4dcfcb7cb4487a4ab7175fda6c4b-848840900.us-east-1.elb.amazonaws.com"
$orc = Invoke-RestMethod -Uri "$billing/orcamentos/os/$($os1.id)"
Invoke-RestMethod -Uri "$billing/orcamentos/$($orc.id)/aprovar" -Method POST
```

### Finalizar execução (Execution)
```powershell
$exec = "http://a305e8e2cb34347939684c26616f618e-1728333382.us-east-1.elb.amazonaws.com"
$updBody = '{ "status":"FINALIZADA", "tecnico":"Vitor", "observacoes":"Serviço concluído" }'
Invoke-RestMethod -Uri "$exec/execucoes/os/$($os1.id)/status" -Method PATCH -ContentType "application/json" -Body $updBody
```

### Conferir status final
```powershell
Invoke-RestMethod -Uri "$os/api/ordens-servico/$($os1.id)" -Headers @{ Authorization = "Bearer $jwt" }
```

---

> **Importante:** este roteiro está em `docs/ROTEIRO_VIDEO.md`. O script automatizado está em `scripts/demo-saga.ps1`.
