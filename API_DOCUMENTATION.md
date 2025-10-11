# Documentação da API - Tech Challenge

## Visão Geral

API RESTful para gerenciamento de oficina mecânica com funcionalidades de gestão de Clientes, Veículos, Peças/Insumos e Serviços.

## Base URL

```
http://localhost:8080/api
```

## Autenticação

Atualmente configurado com Spring Security básico:
- Usuário: `admin`
- Senha: `admin`

---

## Endpoints

### 1. Clientes

#### 1.1 Criar Cliente
**POST** `/clientes`

**Request Body:**
```json
{
  "nome": "João Silva",
  "cpfCnpj": "12345678901",
  "contato": "joao@email.com"
}
```

**Validações:**
- `nome`: obrigatório, 3-100 caracteres
- `cpfCnpj`: obrigatório, 11 dígitos (CPF) ou 14 dígitos (CNPJ), apenas números
- `contato`: obrigatório, 8-100 caracteres

**Response:** `201 Created`
```json
{
  "id": 1,
  "nome": "João Silva",
  "cpfCnpj": "12345678901",
  "contato": "joao@email.com"
}
```

#### 1.2 Buscar Cliente por ID
**GET** `/clientes/{id}`

**Response:** `200 OK`

#### 1.3 Listar Todos os Clientes
**GET** `/clientes`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "nome": "João Silva",
    "cpfCnpj": "12345678901",
    "contato": "joao@email.com"
  }
]
```

#### 1.4 Atualizar Cliente
**PUT** `/clientes/{id}`

**Request Body:** Mesmo formato do POST

**Response:** `200 OK`

#### 1.5 Deletar Cliente
**DELETE** `/clientes/{id}`

**Response:** `204 No Content`

---

### 2. Veículos

#### 2.1 Criar Veículo
**POST** `/veiculos`

**Request Body:**
```json
{
  "placa": "ABC1234",
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2022,
  "clienteId": 1
}
```

**Validações:**
- `placa`: obrigatória, formato brasileiro (ABC1234 ou ABC1D23)
- `marca`: obrigatória, 2-50 caracteres
- `modelo`: obrigatório, 2-50 caracteres
- `ano`: obrigatório, entre 1900 e 2100
- `clienteId`: obrigatório

**Response:** `201 Created`
```json
{
  "id": 1,
  "placa": "ABC1234",
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2022,
  "clienteId": 1,
  "clienteNome": "João Silva"
}
```

#### 2.2 Buscar Veículo por ID
**GET** `/veiculos/{id}`

**Response:** `200 OK`

#### 2.3 Listar Todos os Veículos
**GET** `/veiculos`

**Query Parameters:**
- `clienteId` (opcional): filtra veículos por cliente

**Response:** `200 OK`

#### 2.4 Atualizar Veículo
**PUT** `/veiculos/{id}`

**Request Body:** Mesmo formato do POST

**Response:** `200 OK`

#### 2.5 Deletar Veículo
**DELETE** `/veiculos/{id}`

**Response:** `204 No Content`

---

### 3. Peças e Insumos

#### 3.1 Criar Peça/Insumo
**POST** `/pecas-insumos`

**Request Body:**
```json
{
  "nome": "Filtro de Óleo",
  "descricao": "Filtro de óleo para motor 1.8",
  "preco": 45.90,
  "quantidadeEstoque": 100
}
```

**Validações:**
- `nome`: obrigatório, 3-100 caracteres
- `descricao`: opcional, máximo 500 caracteres
- `preco`: obrigatório, maior que zero, máximo 8 dígitos inteiros e 2 decimais
- `quantidadeEstoque`: obrigatória, não pode ser negativa

**Response:** `201 Created`
```json
{
  "id": 1,
  "nome": "Filtro de Óleo",
  "descricao": "Filtro de óleo para motor 1.8",
  "preco": 45.90,
  "quantidadeEstoque": 100
}
```

#### 3.2 Buscar Peça/Insumo por ID
**GET** `/pecas-insumos/{id}`

**Response:** `200 OK`

#### 3.3 Listar Todas as Peças/Insumos
**GET** `/pecas-insumos`

**Response:** `200 OK`

#### 3.4 Atualizar Peça/Insumo
**PUT** `/pecas-insumos/{id}`

**Request Body:** Mesmo formato do POST

**Response:** `200 OK`

#### 3.5 Atualizar Estoque
**PATCH** `/pecas-insumos/{id}/estoque?quantidadeAjuste={valor}`

**Query Parameters:**
- `quantidadeAjuste`: valor a ser somado ao estoque (pode ser negativo para subtração)

**Exemplo:**
- Adicionar 50 unidades: `PATCH /pecas-insumos/1/estoque?quantidadeAjuste=50`
- Remover 10 unidades: `PATCH /pecas-insumos/1/estoque?quantidadeAjuste=-10`

**Response:** `200 OK`

#### 3.6 Deletar Peça/Insumo
**DELETE** `/pecas-insumos/{id}`

**Response:** `204 No Content`

---

### 4. Serviços

#### 4.1 Criar Serviço
**POST** `/servicos`

**Request Body:**
```json
{
  "descricao": "Troca de óleo e filtro",
  "preco": 150.00
}
```

**Validações:**
- `descricao`: obrigatória, 5-200 caracteres
- `preco`: obrigatório, maior que zero, máximo 8 dígitos inteiros e 2 decimais

**Response:** `201 Created`
```json
{
  "id": 1,
  "descricao": "Troca de óleo e filtro",
  "preco": 150.00
}
```

#### 4.2 Buscar Serviço por ID
**GET** `/servicos/{id}`

**Response:** `200 OK`

#### 4.3 Listar Todos os Serviços
**GET** `/servicos`

**Response:** `200 OK`

#### 4.4 Atualizar Serviço
**PUT** `/servicos/{id}`

**Request Body:** Mesmo formato do POST

**Response:** `200 OK`

#### 4.5 Deletar Serviço
**DELETE** `/servicos/{id}`

**Response:** `204 No Content`

---

### 5. Ordens de Serviço

#### 5.1 Criar Ordem de Serviço
**POST** `/ordens-servico`

Este é o fluxo principal do sistema. O endpoint:
1. Identifica o cliente pelo CPF/CNPJ (deve estar cadastrado)
2. Verifica se o veículo existe pela placa; se não, cadastra automaticamente
3. Valida se todas as peças têm estoque suficiente
4. Gera o orçamento automaticamente (soma dos preços)
5. Cria a OS com status `RECEBIDA`
6. Simula envio do orçamento e muda status para `AGUARDANDO_APROVACAO`

**Request Body:**
```json
{
  "cpfCnpjCliente": "12345678901",
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
    },
    {
      "servicoId": 2,
      "quantidade": 1
    }
  ],
  "pecas": [
    {
      "pecaInsumoId": 1,
      "quantidade": 2
    },
    {
      "pecaInsumoId": 3,
      "quantidade": 1
    }
  ],
  "observacoes": "Cliente solicitou revisão completa"
}
```

**Validações:**
- `cpfCnpjCliente`: obrigatório, 11 ou 14 dígitos
- `veiculo.placa`: obrigatória, formato brasileiro
- `veiculo.marca`, `modelo`, `ano`: obrigatórios apenas se o veículo não existir
- `servicos[].servicoId`: deve existir no cadastro
- `servicos[].quantidade`: mínimo 1
- `pecas[].pecaInsumoId`: deve existir no cadastro
- `pecas[].quantidade`: mínimo 1, não pode exceder estoque disponível

**Response:** `201 Created`
```json
{
  "id": 1,
  "dataCriacao": "2025-10-05T15:30:00",
  "valorTotalOrcamento": 520.00,
  "status": "AGUARDANDO_APROVACAO",
  "clienteId": 1,
  "clienteNome": "João Silva",
  "veiculoId": 1,
  "veiculoPlaca": "ABC1234",
  "veiculoModelo": "Toyota Corolla",
  "servicos": [
    {
      "id": 1,
      "servicoId": 1,
      "servicoDescricao": "Troca de óleo e filtro",
      "quantidade": 1,
      "precoUnitario": 150.00,
      "subtotal": 150.00
    }
  ],
  "pecas": [
    {
      "id": 1,
      "pecaInsumoId": 1,
      "pecaInsumoNome": "Filtro de Óleo",
      "quantidade": 2,
      "precoUnitario": 45.90,
      "subtotal": 91.80
    }
  ],
  "observacoes": "Cliente solicitou revisão completa"
}
```

**Comportamentos Especiais:**

1. **Cliente não encontrado**: Retorna 404 com mensagem solicitando cadastro prévio
2. **Veículo não cadastrado**: Cadastra automaticamente se marca, modelo e ano forem informados
3. **Veículo já cadastrado**: Usa o veículo existente (ignora marca/modelo/ano do DTO)
4. **Estoque insuficiente**: Retorna 400 com detalhes de cada peça insuficiente
5. **Preços históricos**: Armazena preços atuais de serviços e peças no momento da criação

#### 5.2 Buscar Ordem de Serviço por ID
**GET** `/ordens-servico/{id}`

**Response:** `200 OK`

#### 5.3 Listar Ordens de Serviço
**GET** `/ordens-servico`

**Query Parameters:**
- `status` (opcional): filtra por status
  - Valores: `RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE`
- `clienteId` (opcional): filtra por cliente

**Exemplos:**
- Listar todas: `GET /ordens-servico`
- Listar aguardando aprovação: `GET /ordens-servico?status=AGUARDANDO_APROVACAO`
- Listar de um cliente: `GET /ordens-servico?clienteId=1`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "dataCriacao": "2025-10-05T15:30:00",
    "dataInicioExecucao": "2025-10-05T16:00:00",
    "dataFinalizacao": null,
    "dataEntrega": null,
    "valorTotalOrcamento": 520.00,
    "status": "EM_EXECUCAO",
    "clienteId": 1,
    "clienteNome": "João Silva",
    "veiculoId": 1,
    "veiculoPlaca": "ABC1234",
    "veiculoModelo": "Toyota Corolla",
    "servicos": [...],
    "pecas": [...],
    "observacoes": "..."
  }
]
```

#### 5.4 Atualizar Status da Ordem de Serviço (Administrativo)
**PATCH** `/ordens-servico/{id}/status`

Permite que administradores alterem o status de uma OS seguindo as regras de transição.
As datas são atualizadas automaticamente conforme o novo status.

**Regras de Transição:**
- `RECEBIDA` → `EM_DIAGNOSTICO` ou `AGUARDANDO_APROVACAO`
- `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` ou `RECEBIDA`
- `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` ou `RECEBIDA`
- `EM_EXECUCAO` → `FINALIZADA` ou `EM_DIAGNOSTICO`
- `FINALIZADA` → `ENTREGUE` (não pode voltar)
- `ENTREGUE` → (estado final, sem transições)

**Request Body:**
```json
{
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Cliente aprovou o orçamento. Iniciando serviços."
}
```

**Validações:**
- `novoStatus`: obrigatório, deve ser um status válido
- `observacao`: opcional, será adicionada ao histórico da OS

**Response:** `200 OK`
```json
{
  "id": 1,
  "dataCriacao": "2025-10-05T15:30:00",
  "dataInicioExecucao": "2025-10-05T16:00:00",
  "dataFinalizacao": null,
  "dataEntrega": null,
  "valorTotalOrcamento": 520.00,
  "status": "EM_EXECUCAO",
  "clienteId": 1,
  "clienteNome": "João Silva",
  "veiculoId": 1,
  "veiculoPlaca": "ABC1234",
  "veiculoModelo": "Toyota Corolla",
  "servicos": [...],
  "pecas": [...],
  "observacoes": "Cliente solicitou revisão completa\n[2025-10-05T16:00:00] Status alterado para EM_EXECUCAO: Cliente aprovou o orçamento. Iniciando serviços."
}
```

**Comportamentos Especiais:**
1. Ao mudar para `EM_EXECUCAO`: define automaticamente `dataInicioExecucao`
2. Ao mudar para `FINALIZADA`: define automaticamente `dataFinalizacao`
3. Ao mudar para `ENTREGUE`: define automaticamente `dataEntrega`
4. Observações são acumuladas com timestamp

**Erros Possíveis:**
- `400 Bad Request`: Transição de status inválida
```json
{
  "timestamp": "2025-10-05T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Transição de status inválida: não é possível mudar de FINALIZADA para EM_EXECUCAO",
  "path": "/api/ordens-servico/1/status"
}
```

#### 5.5 Consultar Status da OS (Público - Cliente)
**GET** `/ordens-servico/status/{id}?cpfCnpj={cpfCnpj}`

Endpoint público que permite ao cliente consultar o status de sua OS.
Requer autenticação simples via CPF/CNPJ para segurança.
Retorna apenas informações essenciais, sem expor dados sensíveis ou de outros clientes.

**Query Parameters:**
- `cpfCnpj` (obrigatório): CPF ou CNPJ do cliente (11 ou 14 dígitos)

**Exemplo:**
`GET /ordens-servico/status/1?cpfCnpj=12345678901`

**Response:** `200 OK`
```json
{
  "id": 1,
  "dataCriacao": "2025-10-05T15:30:00",
  "status": "EM_EXECUCAO",
  "veiculoPlaca": "ABC1234",
  "veiculoModelo": "Toyota Corolla",
  "valorTotalOrcamento": 520.00,
  "servicos": [
    {
      "descricao": "Troca de óleo e filtro",
      "quantidade": 1
    }
  ],
  "observacoes": "Cliente solicitou revisão completa",
  "dataInicioExecucao": "2025-10-05T16:00:00",
  "dataFinalizacao": null,
  "dataEntrega": null
}
```

**Segurança:**
- Não retorna informações do cliente (nome, CPF, contato)
- Não retorna preços detalhados de peças/serviços
- Valida que o CPF/CNPJ corresponde ao cliente da OS
- Retorna 404 se OS não existir ou CPF/CNPJ não corresponder

**Erros Possíveis:**
- `404 Not Found`: OS não encontrada ou CPF/CNPJ não corresponde
```json
{
  "timestamp": "2025-10-05T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ordem de Serviço com ID 1 não encontrada",
  "path": "/api/ordens-servico/status/1"
}
```

#### 5.6 Monitoramento - Tempo Médio de Execução
**GET** `/ordens-servico/monitoramento/tempo-medio`

Retorna estatísticas sobre o tempo médio de execução das OSs finalizadas.
Útil para monitoramento e análise de desempenho da oficina.

**Response:** `200 OK`
```json
{
  "tempoMedioExecucaoHoras": 24.5,
  "quantidadeOsFinalizadas": 45,
  "tempoMinimoHoras": 2.5,
  "tempoMaximoHoras": 72.0
}
```

**Campos:**
- `tempoMedioExecucaoHoras`: Média de horas entre início da execução e finalização
- `quantidadeOsFinalizadas`: Quantidade de OSs consideradas no cálculo
- `tempoMinimoHoras`: Menor tempo de execução registrado
- `tempoMaximoHoras`: Maior tempo de execução registrado

**Regras de Cálculo:**
1. Considera apenas OSs com status `FINALIZADA` ou `ENTREGUE`
2. Considera apenas OSs que têm `dataInicioExecucao` e `dataFinalizacao` definidas
3. Tempo calculado: `dataFinalizacao - dataInicioExecucao`
4. Se não houver OSs finalizadas, retorna zeros

---

## Respostas de Erro

### 400 Bad Request - Validação
```json
{
  "timestamp": "2025-10-05T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação nos dados fornecidos",
  "path": "/api/clientes",
  "errors": [
    {
      "field": "cpfCnpj",
      "message": "CPF/CNPJ deve conter 11 dígitos (CPF) ou 14 dígitos (CNPJ), apenas números"
    }
  ]
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-10-05T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente com ID 999 não encontrado",
  "path": "/api/clientes/999"
}
```

### 409 Conflict - Recurso Duplicado
```json
{
  "timestamp": "2025-10-05T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um cliente cadastrado com este CPF/CNPJ",
  "path": "/api/clientes"
}
```

### 400 Bad Request - Estoque Insuficiente
```json
{
  "timestamp": "2025-10-05T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Estoque insuficiente para as seguintes peças: Filtro de Óleo (solicitado: 10, disponível: 5), Pastilha de Freio (solicitado: 8, disponível: 2)",
  "path": "/api/ordens-servico",
  "errors": [
    {
      "field": "peca_1",
      "message": "Filtro de Óleo - Solicitado: 10, Disponível: 5"
    },
    {
      "field": "peca_3",
      "message": "Pastilha de Freio - Solicitado: 8, Disponível: 2"
    }
  ]
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-10-05T14:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
  "path": "/api/clientes"
}
```

---

## Validações Específicas

### CPF/CNPJ
- Deve conter **11 dígitos** (CPF) ou **14 dígitos** (CNPJ)
- Apenas números (sem pontos, traços ou barras)
- Exemplo válido CPF: `12345678901`
- Exemplo válido CNPJ: `12345678000199`

### Placa de Veículo
- Formato antigo: **ABC1234** (3 letras + 4 números)
- Formato Mercosul: **ABC1D23** (3 letras + 1 número + 1 letra + 2 números)
- Letras devem ser maiúsculas
- A placa é automaticamente convertida para maiúsculas no backend

---

## Testes com cURL

### Criar Cliente
```bash
curl -X POST http://localhost:8080/api/clientes \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "cpfCnpj": "12345678901",
    "contato": "joao@email.com"
  }'
```

### Criar Veículo
```bash
curl -X POST http://localhost:8080/api/veiculos \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "placa": "ABC1234",
    "marca": "Toyota",
    "modelo": "Corolla",
    "ano": 2022,
    "clienteId": 1
  }'
```

### Atualizar Estoque de Peça
```bash
curl -X PATCH "http://localhost:8080/api/pecas-insumos/1/estoque?quantidadeAjuste=50" \
  -u admin:admin
```

---

## Observações Importantes

1. **Transações**: Todas as operações de escrita (POST, PUT, DELETE) são transacionais.

2. **Cascata**: Ao deletar um cliente, seus veículos também são deletados (orphanRemoval=true).

3. **Validações**: As validações ocorrem antes de chegar à camada de serviço, retornando 400 Bad Request.

4. **Unicidade**: 
   - CPF/CNPJ de cliente deve ser único
   - Placa de veículo deve ser única

5. **Estoque**: O endpoint PATCH de atualização de estoque permite ajustes incrementais, facilitando operações de entrada e saída.

6. **PUT vs PATCH**: 
   - PUT é usado para substituição completa do recurso (todos os campos obrigatórios)
   - PATCH é usado apenas para atualização de estoque (operação parcial)

