# Guia de Acompanhamento e Gestão de Ordens de Serviço

## 📋 Visão Geral

Este guia documenta as funcionalidades de **Acompanhamento e Gestão de Ordens de Serviço** implementadas no sistema Tech Challenge. Estas funcionalidades permitem:

1. **Gestão Administrativa**: Controle completo do ciclo de vida das OSs
2. **Consulta Pública**: Clientes podem acompanhar suas OSs de forma segura
3. **Monitoramento**: Métricas de tempo médio de execução

---

## 🔄 Ciclo de Vida de uma Ordem de Serviço

### Estados Possíveis

1. **RECEBIDA**: OS criada, veículo recebido na oficina
2. **EM_DIAGNOSTICO**: Veículo em análise/diagnóstico
3. **AGUARDANDO_APROVACAO**: Orçamento enviado, aguardando aprovação do cliente
4. **EM_EXECUCAO**: Serviços em execução
5. **FINALIZADA**: Serviços concluídos, aguardando retirada
6. **ENTREGUE**: Veículo entregue ao cliente (estado final)

### Fluxo Típico

```
RECEBIDA 
    ↓
EM_DIAGNOSTICO 
    ↓
AGUARDANDO_APROVACAO
    ↓
EM_EXECUCAO
    ↓
FINALIZADA
    ↓
ENTREGUE
```

### Transições Permitidas

| Estado Atual | Pode Mudar Para |
|--------------|-----------------|
| RECEBIDA | EM_DIAGNOSTICO, AGUARDANDO_APROVACAO |
| EM_DIAGNOSTICO | AGUARDANDO_APROVACAO, RECEBIDA |
| AGUARDANDO_APROVACAO | EM_EXECUCAO, RECEBIDA |
| EM_EXECUCAO | FINALIZADA, EM_DIAGNOSTICO |
| FINALIZADA | ENTREGUE |
| ENTREGUE | *(nenhum - estado final)* |

**Regras Importantes:**
- ✅ Pode avançar ou retroceder para estados anteriores (exceto FINALIZADA e ENTREGUE)
- ❌ FINALIZADA só pode ir para ENTREGUE
- ❌ ENTREGUE é o estado final, sem alterações possíveis

---

## 🔐 Autenticação do Cliente

### Decisão de Implementação

Para o MVP, escolhemos o método **CPF/CNPJ + ID da OS** para autenticação do cliente ao consultar suas OSs.

**Vantagens:**
- ✅ Simples de implementar
- ✅ Não requer cadastro de senha
- ✅ Cliente recebe o ID da OS no momento da criação
- ✅ CPF/CNPJ já está no cadastro

**Como Funciona:**
1. Cliente recebe o ID da OS quando ela é criada (ex: por email/SMS)
2. Para consultar, fornece: ID da OS + CPF/CNPJ
3. Sistema valida se o CPF/CNPJ corresponde ao cliente da OS
4. Retorna apenas informações seguras (sem dados de outros clientes)

---

## 📊 Rastreamento de Tempo

### Campos de Data Adicionados

A entidade `OrdemDeServico` agora possui os seguintes campos de data:

| Campo | Quando é Definido | Descrição |
|-------|-------------------|-----------|
| `dataCriacao` | Ao criar OS | Data/hora de criação da OS |
| `dataInicioExecucao` | Status → EM_EXECUCAO | Quando os serviços começam |
| `dataFinalizacao` | Status → FINALIZADA | Quando os serviços terminam |
| `dataEntrega` | Status → ENTREGUE | Quando o veículo é entregue |

**Cálculo do Tempo de Execução:**
```
Tempo de Execução = dataFinalizacao - dataInicioExecucao
```

---

## 🚀 Endpoints Implementados

### 1. Atualizar Status (Administrativo)

**Endpoint:** `PATCH /api/ordens-servico/{id}/status`  
**Autenticação:** Requerida (admin)

**Exemplo de Requisição:**
```json
{
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Cliente aprovou o orçamento. Iniciando os serviços."
}
```

**Comportamentos Automáticos:**
- Valida a transição de status
- Atualiza a data correspondente automaticamente
- Adiciona observação ao histórico com timestamp
- Retorna erro 400 se transição inválida

**Exemplo de Uso:**
```bash
curl -X PATCH http://localhost:8080/api/ordens-servico/1/status \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "novoStatus": "EM_EXECUCAO",
    "observacao": "Cliente aprovou o orçamento"
  }'
```

---

### 2. Consulta Pública (Cliente)

**Endpoint:** `GET /api/ordens-servico/status/{id}?cpfCnpj={cpfCnpj}`  
**Autenticação:** CPF/CNPJ do cliente

**Características:**
- 🔓 Endpoint público (sem autenticação administrativa)
- 🔒 Requer CPF/CNPJ para validação
- 📋 Retorna apenas informações essenciais
- 🚫 Não expõe dados sensíveis

**Exemplo de Uso:**
```bash
curl -X GET "http://localhost:8080/api/ordens-servico/status/1?cpfCnpj=12345678901"
```

**Resposta:**
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

**O que NÃO é retornado (por segurança):**
- Nome e contato do cliente
- Preços detalhados de peças/serviços
- IDs internos de peças/serviços
- Dados de outros clientes

---

### 3. Monitoramento de Tempo Médio

**Endpoint:** `GET /api/ordens-servico/monitoramento/tempo-medio`  
**Autenticação:** Requerida (admin)

**Exemplo de Resposta:**
```json
{
  "tempoMedioExecucaoHoras": 24.5,
  "quantidadeOsFinalizadas": 45,
  "tempoMinimoHoras": 2.5,
  "tempoMaximoHoras": 72.0
}
```

**Critérios de Cálculo:**
- Considera apenas OSs com status `FINALIZADA` ou `ENTREGUE`
- Requer que `dataInicioExecucao` e `dataFinalizacao` estejam definidas
- Tempo em horas com 2 casas decimais
- Se não houver dados, retorna zeros

**Exemplo de Uso:**
```bash
curl -X GET http://localhost:8080/api/ordens-servico/monitoramento/tempo-medio \
  -u admin:admin
```

---

## 🎯 Casos de Uso

### Caso 1: Fluxo Completo Administrativo

```http
# 1. Criar OS
POST /api/ordens-servico
{
  "cpfCnpjCliente": "12345678901",
  "veiculo": { "placa": "ABC1234", ... },
  "servicos": [...],
  "pecas": [...]
}
# Status: AGUARDANDO_APROVACAO (automático após criação)

# 2. Cliente aprova, iniciar execução
PATCH /api/ordens-servico/1/status
{
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Cliente aprovou orçamento"
}
# Define dataInicioExecucao automaticamente

# 3. Finalizar serviços
PATCH /api/ordens-servico/1/status
{
  "novoStatus": "FINALIZADA",
  "observacao": "Serviços concluídos"
}
# Define dataFinalizacao automaticamente

# 4. Entregar veículo
PATCH /api/ordens-servico/1/status
{
  "novoStatus": "ENTREGUE",
  "observacao": "Veículo entregue ao cliente"
}
# Define dataEntrega automaticamente
```

### Caso 2: Cliente Consultando Status

```http
# Cliente abre app/site e consulta sua OS
GET /api/ordens-servico/status/1?cpfCnpj=12345678901

# Recebe informações atualizadas:
# - Status atual
# - Serviços que serão realizados
# - Valor total
# - Observações
# - Datas estimadas
```

### Caso 3: Análise de Desempenho

```http
# Gerente consulta métricas
GET /api/ordens-servico/monitoramento/tempo-medio

# Analisa:
# - Tempo médio: 24.5 horas
# - Tempo mínimo: 2.5 horas (serviços rápidos)
# - Tempo máximo: 72 horas (serviços complexos)
# - Total de OSs: 45
```

---

## ⚠️ Validações e Erros

### Erro: Transição de Status Inválida

**Cenário:** Tentar mudar de FINALIZADA para EM_EXECUCAO

**Request:**
```json
PATCH /api/ordens-servico/1/status
{
  "novoStatus": "EM_EXECUCAO"
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-05T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Transição de status inválida: não é possível mudar de FINALIZADA para EM_EXECUCAO",
  "path": "/api/ordens-servico/1/status"
}
```

### Erro: CPF/CNPJ Não Corresponde

**Cenário:** Cliente tentando acessar OS de outro cliente

**Request:**
```
GET /api/ordens-servico/status/1?cpfCnpj=99999999999
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2025-10-05T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ordem de Serviço com ID 1 não encontrada",
  "path": "/api/ordens-servico/status/1"
}
```
*Nota: Retorna 404 por segurança, não revelando que a OS existe*

---

## 📝 Observações e Histórico

O campo `observacoes` agora funciona como um histórico:

**Formato:**
```
[Observação inicial do cliente]
[2025-10-05T16:00:00] Status alterado para EM_EXECUCAO: Cliente aprovou o orçamento
[2025-10-06T18:30:00] Status alterado para FINALIZADA: Todos os serviços concluídos
[2025-10-07T10:00:00] Status alterado para ENTREGUE: Veículo entregue ao cliente
```

Cada alteração de status com observação é registrada com timestamp.

---

## 🔧 Integração com Sistema Existente

### Alterações na Entidade `OrdemDeServico`

```java
// Novos campos adicionados:
private LocalDateTime dataInicioExecucao;
private LocalDateTime dataFinalizacao;
private LocalDateTime dataEntrega;
```

### Novos DTOs Criados

1. **StatusUpdateDTO**: Para alteração de status
   - `novoStatus`: Status desejado
   - `observacao`: Comentário opcional

2. **OrdemDeServicoPublicDTO**: Para consulta pública
   - Campos essenciais e seguros
   - Sem dados sensíveis

3. **MonitoramentoDTO**: Para estatísticas
   - Tempo médio, mínimo, máximo
   - Quantidade de OSs

### Compatibilidade

✅ **Todas as funcionalidades existentes continuam funcionando**
- Criação de OS inalterada
- Listagens e filtros funcionam normalmente
- Campos de data são opcionais (nullable)

---

## 🎓 Melhores Práticas

### Para Administradores

1. **Sempre adicione observações** ao mudar status
2. **Siga o fluxo natural** de transições
3. **Monitore o tempo médio** regularmente
4. **Use filtros** para gestão eficiente

### Para Desenvolvedores

1. **Valide transições** antes de permitir mudanças
2. **Registre histórico** de todas as alterações
3. **Proteja dados sensíveis** em endpoints públicos
4. **Calcule métricas** apenas com dados válidos

### Para Integração Frontend

1. **Consulta pública** não requer autenticação administrativa
2. **Exiba status** de forma amigável ao usuário
3. **Mostre datas estimadas** quando disponíveis
4. **Implemente refresh automático** ou manual

---

## 📈 Possíveis Extensões Futuras

### MVP Atual
- ✅ Alteração de status com validação
- ✅ Consulta pública com CPF/CNPJ
- ✅ Monitoramento de tempo médio

### Possíveis Melhorias
- 📧 Notificações por email/SMS ao mudar status
- 🔔 Sistema de notificações push
- 📊 Dashboard com gráficos de desempenho
- 🔒 Autenticação com token/JWT
- 📝 Histórico completo de mudanças (audit log)
- ⏰ SLA e alertas de atraso
- 💬 Sistema de mensagens cliente-oficina

---

## 🆘 Troubleshooting

### Problema: Não consigo mudar o status

**Causa:** Transição não permitida  
**Solução:** Verifique as transições permitidas na tabela acima

### Problema: Cliente não consegue consultar a OS

**Causa:** CPF/CNPJ incorreto  
**Solução:** Verifique se o CPF/CNPJ está correto e corresponde ao cliente da OS

### Problema: Tempo médio retorna 0

**Causa:** Nenhuma OS finalizada com datas preenchidas  
**Solução:** Certifique-se de que as OSs passaram pelo fluxo completo (EM_EXECUCAO → FINALIZADA)

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Consulte a documentação completa em `API_DOCUMENTATION.md`
2. Veja exemplos práticos em `API_EXAMPLES.http`
3. Verifique os logs da aplicação para erros detalhados

---

**Versão:** 1.0  
**Data:** Outubro 2025  
**Status:** Implementado e Testado ✅

