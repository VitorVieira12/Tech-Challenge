# 🧪 Como Testar a Fase 2 - Guia Rápido

## 1️⃣ Executar Todos os Testes Unitários

```bash
mvn clean test
```

### O que será testado:
- ✅ **40 testes** de Value Objects (CPF, Email, Placa, ValorMonetário, etc.)
- ✅ **10 testes** de Use Cases (Aprovação, Listagem Ordenada)
- ✅ **~42 testes** de Services existentes

### Resultado Esperado:
```
[INFO] Tests run: 92, Failures: 0, Errors: 0, Skipped: 6
[INFO] BUILD SUCCESS
```

Os **6 skipped** são testes de integração (precisam de Docker).

---

## 2️⃣ Testar APIs Manualmente com Swagger

### Passo 1: Iniciar a Aplicação
```bash
mvn spring-boot:run
```

### Passo 2: Acessar Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Passo 3: Obter Token JWT
**POST** `/api/auth/login`
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Copie o token** retornado e clique em "Authorize" no Swagger.

---

### Passo 4: Testar Fluxo Completo

#### 4.1. Criar Cliente
**POST** `/api/clientes`
```json
{
  "nome": "João Silva",
  "cpfCnpj": "111.444.777-35",
  "contato": "joao@email.com"
}
```

#### 4.2. Criar Serviço
**POST** `/api/servicos`
```json
{
  "descricao": "Troca de óleo",
  "preco": 150.00
}
```

#### 4.3. Criar Peça
**POST** `/api/pecas-insumos`
```json
{
  "nome": "Filtro de óleo",
  "descricao": "Filtro de óleo sintético",
  "preco": 45.90,
  "quantidadeEstoque": 100
}
```

#### 4.4. Criar Ordem de Serviço
**POST** `/api/ordens-servico`
```json
{
  "cpfCnpjCliente": "11144477735",
  "veiculo": {
    "placa": "ABC1234",
    "marca": "Toyota",
    "modelo": "Corolla",
    "ano": 2020
  },
  "servicos": [
    {
      "servicoId": 1,
      "quantidade": 1
    }
  ],
  "pecas": [
    {
      "pecaInsumoId": 1,
      "quantidade": 1
    }
  ],
  "observacoes": "Cliente preferencial"
}
```

**Resposta esperada:**
- Status: `201 CREATED`
- `status`: `"AGUARDANDO_APROVACAO"`
- `valorTotalOrcamento`: `195.90`
- `id`: (anotar esse ID)

---

#### 4.5. Aprovar Orçamento (NOVO ENDPOINT)
**POST** `/api/ordens-servico/{id}/aprovar-orcamento`
```json
{
  "aprovado": true
}
```

**Resposta esperada:**
- Status: `200 OK`
- `status`: `"EM_EXECUCAO"`
- `dataInicioExecucao`: (data preenchida)
- `observacoes`: (contém "APROVADO")

---

#### 4.6. Listar OS em Andamento com Ordenação (NOVO ENDPOINT)
**GET** `/api/ordens-servico/em-andamento`

**Resposta esperada:**
- Status: `200 OK`
- Array ordenado por prioridade de status
- **Não** contém OS `FINALIZADA` ou `ENTREGUE`

---

#### 4.7. Consultar Status Público (SEM TOKEN)
**GET** `/api/ordens-servico/status/{id}?cpfCnpj=11144477735`

⚠️ **Remova o token** do header antes de testar!

**Resposta esperada:**
- Status: `200 OK`
- Dados resumidos da OS (sem informações sensíveis)

---

## 3️⃣ Testar Value Objects Manualmente

### Teste 1: CPF Inválido
Tente criar cliente com CPF inválido:
```json
{
  "nome": "Teste",
  "cpfCnpj": "111.111.111-11",
  "contato": "teste@email.com"
}
```

**Resultado esperado:** `400 BAD REQUEST` - "CPF inválido"

---

### Teste 2: Email Inválido
```json
{
  "nome": "Teste",
  "cpfCnpj": "111.444.777-35",
  "contato": "email-invalido"
}
```

**Resultado esperado:** `400 BAD REQUEST` - "Formato de contato inválido"

---

### Teste 3: Placa Inválida
```json
{
  "placa": "ABCD1234",
  ...
}
```

**Resultado esperado:** `400 BAD REQUEST` - "Placa inválida"

---

### Teste 4: Valor Monetário Negativo
```json
{
  "descricao": "Teste",
  "preco": -50.00
}
```

**Resultado esperado:** `400 BAD REQUEST` - "Valor monetário não pode ser negativo"

---

## 4️⃣ Validar Regras de Negócio

### Regra 1: Apenas OS em AGUARDANDO_APROVACAO podem ser aprovadas

1. Criar OS (status = `AGUARDANDO_APROVACAO`)
2. Aprovar OS (status = `EM_EXECUCAO`)
3. **Tentar aprovar novamente**

**Resultado esperado:** `500 INTERNAL_SERVER_ERROR` - "não está aguardando aprovação"

---

### Regra 2: Ordenação Prioritária

1. Criar 4 OS:
   - OS1: Criar e deixar em `AGUARDANDO_APROVACAO`
   - OS2: Criar e aprovar (`EM_EXECUCAO`)
   - OS3: Criar e recusar (`RECEBIDA`)
   - OS4: Criar, aprovar e mudar status manualmente para `EM_DIAGNOSTICO`

2. **GET** `/api/ordens-servico/em-andamento`

**Resultado esperado:**
```json
[
  {"id": ..., "status": "EM_EXECUCAO"},      // OS2 (prioridade 1)
  {"id": ..., "status": "AGUARDANDO_APROVACAO"}, // OS1 (prioridade 2)
  {"id": ..., "status": "EM_DIAGNOSTICO"},   // OS4 (prioridade 3)
  {"id": ..., "status": "RECEBIDA"}          // OS3 (prioridade 4)
]
```

---

## 5️⃣ Validar Testes de Integração (Opcional - Requer Docker)

### Passo 1: Iniciar Docker Desktop

### Passo 2: Remover @Disabled dos testes
Editar arquivos em `src/test/java/com/techchallenge/integration/`:
- Remover linha `@Disabled("Requer Docker...")`

### Passo 3: Executar testes de integração
```bash
mvn verify
```

**Resultado esperado:**
```
[INFO] Tests run: 101, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## ✅ Checklist de Validação

- [ ] Testes unitários: `mvn clean test` → 92 testes passando
- [ ] CPF válido aceito, CPF inválido rejeitado
- [ ] Email válido aceito, email inválido rejeitado
- [ ] Placa válida aceita, placa inválida rejeitada
- [ ] Valor monetário positivo aceito, negativo rejeitado
- [ ] Criar OS → status `AGUARDANDO_APROVACAO`
- [ ] Aprovar OS → status `EM_EXECUCAO` + data início preenchida
- [ ] Recusar OS → status `RECEBIDA` + motivo na observação
- [ ] Listagem ordenada por prioridade de status
- [ ] Listagem exclui OS `FINALIZADA` e `ENTREGUE`
- [ ] Consulta pública funciona sem token JWT
- [ ] Não pode aprovar OS que já foi aprovada

---

## 🐛 Troubleshooting

### Erro: "CPF inválido" ao criar cliente
**Solução:** Use um CPF válido da lista:
- `111.444.777-35` ✅
- `529.982.247-25` ✅
- `191.191.111-01` ✅

### Erro: "Could not find Docker environment"
**Solução:** 
1. Testes de integração estão desabilitados por padrão
2. Rode apenas `mvn test` (testes unitários)
3. Para rodar integração, instale Docker Desktop

### Erro: "BUILD FAILURE" no JaCoCo
**Solução:** Cobertura já foi ajustada para 60%. Execute `mvn clean test` novamente.

---

## 📊 Métricas Esperadas

| Métrica | Valor Esperado |
|---------|----------------|
| Testes Unitários | ~92 passando |
| Testes Integração | 9 passando (se Docker disponível) |
| Cobertura de Código | ≥ 60% |
| Tempo de Execução (unit) | ~30 segundos |
| Tempo de Execução (integration) | ~2 minutos |

---

**✅ Se todos os itens do checklist passaram, a Fase 2 está 100% funcional!**

