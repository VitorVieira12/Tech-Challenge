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

