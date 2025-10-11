# 🧪 Teste Rápido - Gestão de Ordens de Serviço

Este guia fornece um roteiro rápido para testar todas as funcionalidades de gestão de OS implementadas.

## 🔧 Pré-requisitos

1. Aplicação rodando em `http://localhost:8080`
2. Usuário: `admin` / Senha: `admin`
3. Cliente, Serviço e Peça já cadastrados

---

## 📝 Cenário Completo de Teste

### Passo 1: Criar Dados Base (se não existirem)

#### 1.1 Criar Cliente
```http
POST http://localhost:8080/api/clientes
Authorization: Basic admin:admin
Content-Type: application/json

{
  "nome": "João Silva",
  "cpfCnpj": "12345678901",
  "contato": "joao.silva@email.com"
}
```
**Resultado esperado:** Status 201, retorna ID do cliente

#### 1.2 Criar Peça
```http
POST http://localhost:8080/api/pecas-insumos
Authorization: Basic admin:admin
Content-Type: application/json

{
  "nome": "Filtro de Óleo",
  "descricao": "Filtro de óleo para motor",
  "preco": 45.90,
  "quantidadeEstoque": 100
}
```
**Resultado esperado:** Status 201, retorna ID da peça

#### 1.3 Criar Serviço
```http
POST http://localhost:8080/api/servicos
Authorization: Basic admin:admin
Content-Type: application/json

{
  "descricao": "Troca de óleo e filtro",
  "preco": 150.00
}
```
**Resultado esperado:** Status 201, retorna ID do serviço

---

### Passo 2: Criar Ordem de Serviço

```http
POST http://localhost:8080/api/ordens-servico
Authorization: Basic admin:admin
Content-Type: application/json

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
    }
  ],
  "pecas": [
    {
      "pecaInsumoId": 1,
      "quantidade": 2
    }
  ],
  "observacoes": "Cliente solicitou revisão completa"
}
```

**Resultado esperado:**
- ✅ Status 201 Created
- ✅ Status inicial: `AGUARDANDO_APROVACAO`
- ✅ Retorna ID da OS (anote para próximos passos)

**Anotar:** `OS_ID = _____`

---

### Passo 3: Testar Alterações de Status

#### 3.1 Status: AGUARDANDO_APROVACAO → EM_EXECUCAO
```http
PATCH http://localhost:8080/api/ordens-servico/[OS_ID]/status
Authorization: Basic admin:admin
Content-Type: application/json

{
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Cliente aprovou o orçamento. Iniciando serviços."
}
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Status mudou para `EM_EXECUCAO`
- ✅ Campo `dataInicioExecucao` foi preenchido
- ✅ Observação foi adicionada com timestamp

#### 3.2 Status: EM_EXECUCAO → FINALIZADA
```http
PATCH http://localhost:8080/api/ordens-servico/[OS_ID]/status
Authorization: Basic admin:admin
Content-Type: application/json

{
  "novoStatus": "FINALIZADA",
  "observacao": "Todos os serviços foram concluídos com sucesso."
}
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Status mudou para `FINALIZADA`
- ✅ Campo `dataFinalizacao` foi preenchido
- ✅ Observação foi adicionada

#### 3.3 Status: FINALIZADA → ENTREGUE
```http
PATCH http://localhost:8080/api/ordens-servico/[OS_ID]/status
Authorization: Basic admin:admin
Content-Type: application/json

{
  "novoStatus": "ENTREGUE",
  "observacao": "Veículo entregue ao cliente."
}
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Status mudou para `ENTREGUE`
- ✅ Campo `dataEntrega` foi preenchido

---

### Passo 4: Testar Consulta Pública (Cliente)

#### 4.1 Consulta com CPF Correto
```http
GET http://localhost:8080/api/ordens-servico/status/[OS_ID]?cpfCnpj=12345678901
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Retorna dados da OS
- ✅ Não contém nome do cliente
- ✅ Não contém CPF do cliente
- ✅ Contém apenas descrição dos serviços (sem preços detalhados)
- ✅ Contém todas as datas de rastreamento

**Campos esperados:**
```json
{
  "id": 1,
  "dataCriacao": "...",
  "status": "ENTREGUE",
  "veiculoPlaca": "ABC1234",
  "veiculoModelo": "Toyota Corolla",
  "valorTotalOrcamento": 341.80,
  "servicos": [
    {
      "descricao": "Troca de óleo e filtro",
      "quantidade": 1
    }
  ],
  "observacoes": "...",
  "dataInicioExecucao": "...",
  "dataFinalizacao": "...",
  "dataEntrega": "..."
}
```

#### 4.2 Consulta com CPF Incorreto (Teste de Segurança)
```http
GET http://localhost:8080/api/ordens-servico/status/[OS_ID]?cpfCnpj=99999999999
```

**Verificações:**
- ✅ Status 404 Not Found
- ✅ Não revela que a OS existe
- ✅ Mensagem genérica de "não encontrado"

---

### Passo 5: Testar Monitoramento

```http
GET http://localhost:8080/api/ordens-servico/monitoramento/tempo-medio
Authorization: Basic admin:admin
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Retorna `tempoMedioExecucaoHoras` (deve ser > 0 se houver OSs finalizadas)
- ✅ Retorna `quantidadeOsFinalizadas` (deve ser >= 1)
- ✅ Retorna `tempoMinimoHoras`
- ✅ Retorna `tempoMaximoHoras`

**Exemplo de resposta:**
```json
{
  "tempoMedioExecucaoHoras": 0.02,
  "quantidadeOsFinalizadas": 1,
  "tempoMinimoHoras": 0.02,
  "tempoMaximoHoras": 0.02
}
```

---

### Passo 6: Testar Validações (Casos de Erro)

#### 6.1 Transição Inválida: ENTREGUE → EM_EXECUCAO
```http
PATCH http://localhost:8080/api/ordens-servico/[OS_ID]/status
Authorization: Basic admin:admin
Content-Type: application/json

{
  "novoStatus": "EM_EXECUCAO"
}
```

**Verificações:**
- ✅ Status 400 Bad Request
- ✅ Mensagem: "Transição de status inválida"
- ✅ Indica que não pode mudar de ENTREGUE para EM_EXECUCAO

#### 6.2 Status Inexistente
```http
PATCH http://localhost:8080/api/ordens-servico/[OS_ID]/status
Authorization: Basic admin:admin
Content-Type: application/json

{
  "novoStatus": "STATUS_INVALIDO"
}
```

**Verificações:**
- ✅ Status 400 Bad Request
- ✅ Erro de validação/parsing

---

### Passo 7: Testar Listagens com Filtros

#### 7.1 Listar Todas as OSs
```http
GET http://localhost:8080/api/ordens-servico
Authorization: Basic admin:admin
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Retorna array de OSs
- ✅ Cada OS contém as novas datas

#### 7.2 Listar OSs por Status
```http
GET http://localhost:8080/api/ordens-servico?status=ENTREGUE
Authorization: Basic admin:admin
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Retorna apenas OSs com status ENTREGUE

#### 7.3 Listar OSs por Cliente
```http
GET http://localhost:8080/api/ordens-servico?clienteId=1
Authorization: Basic admin:admin
```

**Verificações:**
- ✅ Status 200 OK
- ✅ Retorna apenas OSs do cliente especificado

---

## 🎯 Checklist de Funcionalidades

### Alteração de Status
- [ ] Pode mudar de AGUARDANDO_APROVACAO para EM_EXECUCAO
- [ ] Pode mudar de EM_EXECUCAO para FINALIZADA
- [ ] Pode mudar de FINALIZADA para ENTREGUE
- [ ] Não pode mudar de ENTREGUE para qualquer outro
- [ ] Não pode mudar de FINALIZADA para EM_EXECUCAO
- [ ] Data é preenchida automaticamente ao mudar para EM_EXECUCAO
- [ ] Data é preenchida automaticamente ao mudar para FINALIZADA
- [ ] Data é preenchida automaticamente ao mudar para ENTREGUE
- [ ] Observação é adicionada ao histórico com timestamp

### Consulta Pública
- [ ] Cliente consegue consultar com CPF correto
- [ ] Cliente não consegue consultar com CPF incorreto
- [ ] Retorna apenas dados essenciais
- [ ] Não expõe nome/CPF/contato do cliente
- [ ] Não expõe preços detalhados de peças/serviços
- [ ] Retorna 404 para acesso não autorizado

### Monitoramento
- [ ] Retorna tempo médio de execução
- [ ] Retorna quantidade de OSs consideradas
- [ ] Retorna tempo mínimo e máximo
- [ ] Considera apenas OSs finalizadas
- [ ] Considera apenas OSs com datas preenchidas

### Compatibilidade
- [ ] Endpoints existentes continuam funcionando
- [ ] Criação de OS funciona normalmente
- [ ] Listagens funcionam normalmente
- [ ] Filtros por status e cliente funcionam

---

## 🐛 Problemas Comuns e Soluções

### Problema: "Ordem de Serviço não encontrada"
**Solução:** Verifique se você está usando o ID correto da OS criada no Passo 2.

### Problema: "Cliente não encontrado"
**Solução:** Execute o Passo 1.1 para criar o cliente antes de criar a OS.

### Problema: "Serviço/Peça não encontrada"
**Solução:** Execute os Passos 1.2 e 1.3 para criar serviço e peça.

### Problema: "Estoque insuficiente"
**Solução:** Aumente o estoque da peça usando:
```http
PATCH http://localhost:8080/api/pecas-insumos/1/estoque?quantidadeAjuste=100
Authorization: Basic admin:admin
```

### Problema: Tempo médio retorna 0
**Causa:** Ainda não há OSs finalizadas com datas preenchidas.
**Solução:** Complete o fluxo do Passo 3 (até FINALIZADA ou ENTREGUE).

---

## 📊 Exemplo de Fluxo Completo com Tempos

Para testar o cálculo de tempo médio de forma realista, você pode:

1. Criar OS → Status: AGUARDANDO_APROVACAO
2. Esperar 1 minuto
3. Mudar para EM_EXECUCAO → Define dataInicioExecucao
4. Esperar 2 minutos
5. Mudar para FINALIZADA → Define dataFinalizacao
6. Consultar tempo médio → Deve mostrar aproximadamente 0.03 horas (2 minutos)

**Ou para testes rápidos:**
- Execute os passos 2 e 3 em sequência rápida
- O tempo será de alguns segundos (0.01 horas ou menos)

---

## 🎓 Dicas de Teste

### Usando cURL
Se preferir usar cURL em vez de ferramentas HTTP:

```bash
# Alterar status
curl -X PATCH http://localhost:8080/api/ordens-servico/1/status \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"novoStatus": "EM_EXECUCAO", "observacao": "Teste"}'

# Consulta pública
curl http://localhost:8080/api/ordens-servico/status/1?cpfCnpj=12345678901

# Monitoramento
curl http://localhost:8080/api/ordens-servico/monitoramento/tempo-medio \
  -u admin:admin
```

### Usando Postman/Insomnia
1. Importe as requisições do arquivo `API_EXAMPLES.http`
2. Configure a autenticação Basic Auth
3. Execute as requisições na ordem do teste

### Usando REST Client (VS Code)
1. Instale a extensão "REST Client"
2. Abra o arquivo `API_EXAMPLES.http`
3. Clique em "Send Request" acima de cada requisição

---

## ✅ Resultado Esperado

Ao final dos testes, você deve ter:

1. ✅ Uma OS completa (status ENTREGUE)
2. ✅ Todas as datas preenchidas (criação, início, finalização, entrega)
3. ✅ Histórico de observações com timestamps
4. ✅ Consulta pública funcionando
5. ✅ Estatísticas de tempo médio disponíveis
6. ✅ Validações de transição funcionando

---

## 📞 Suporte

Se encontrar problemas:
1. Verifique os logs da aplicação
2. Consulte `GESTAO_OS_GUIDE.md` para detalhes
3. Consulte `API_DOCUMENTATION.md` para referência completa
4. Verifique `CHANGELOG_GESTAO_OS.md` para detalhes de implementação

---

**Tempo estimado de teste:** 10-15 minutos  
**Última atualização:** 11/10/2025

