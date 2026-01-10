# 📧 Sistema de Notificação por Email - Fase 2

## 📋 Visão Geral

Sistema de notificação automática por email implementado para atualizar clientes sobre mudanças de status nas Ordens de Serviço.

---

## ✨ Funcionalidades Implementadas

### 1. **Detecção Inteligente de Contato**
- ✅ Verifica se o cliente possui contato cadastrado
- ✅ Identifica se o contato é **EMAIL** ou **TELEFONE**
- ✅ Envia email **apenas** se for EMAIL válido
- ✅ Loga aviso para telefones (preparado para futura implementação de SMS)

### 2. **Email HTML Formatado**
- ✅ Template HTML responsivo e profissional
- ✅ Informações completas da OS (ID, veículo, status, valor)
- ✅ Cores diferentes por status (visual intuitivo)
- ✅ Mensagens contextuais baseadas no status
- ✅ Footer com informações de contato da oficina

### 3. **Processamento Assíncrono**
- ✅ Envio de email em **background** (@Async)
- ✅ Não bloqueia o fluxo principal da aplicação
- ✅ Performance otimizada

### 4. **Modo Desenvolvimento/Produção**
- ✅ **Dev:** Apenas loga o email (não envia)
- ✅ **Prod:** Envia email real via SMTP

---

## 🏗️ Arquitetura

```
OrdemDeServicoService / AprovarOrcamentoUseCase
           ↓
   atualizarStatus() / executar()
           ↓
   [Salva OS no banco]
           ↓
   EmailNotificationService.notificarMudancaStatusOS()
           ↓ (async)
   ┌─────────────────────────────┐
   │ 1. Verifica se tem contato   │
   │ 2. Verifica se é EMAIL       │
   │ 3. Verifica se está habilitado│
   │ 4. Envia email formatado     │
   └─────────────────────────────┘
```

---

## 📁 Arquivos Criados/Modificados

### Novos Arquivos
```
src/main/java/com/techchallenge/domain/service/
└── EmailNotificationService.java          [NOVO]
```

### Arquivos Modificados
```
pom.xml                                    [+dependência spring-boot-starter-mail]
src/main/resources/application.yml        [+configurações de email]
src/main/java/com/techchallenge/
├── TechChallengeApplication.java         [+@EnableAsync]
├── domain/service/
│   └── OrdemDeServicoService.java        [+injeção EmailNotificationService]
└── domain/usecase/
    └── AprovarOrcamentoUseCase.java      [+injeção EmailNotificationService]
```

---

## ⚙️ Configuração

### 1. **application.yml**

```yaml
# Email Configuration
spring.mail:
  host: smtp.gmail.com                    # SMTP server
  port: 587                               # Porta TLS
  username: seu-email@gmail.com           # Email remetente
  password: sua-senha-app                 # Senha de aplicativo
  from: noreply@techchallenge.com        # Email exibido como remetente
  properties:
    mail.smtp:
      auth: true
      starttls.enable: true

# Habilitar/desabilitar emails
app.email.enabled: false                  # false = apenas loga (dev)
                                          # true = envia real (prod)
```

### 2. **Variáveis de Ambiente**

Para **produção**, configure via variáveis de ambiente:

```bash
# Docker / Kubernetes
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=oficinatechallenge@gmail.com
MAIL_PASSWORD=abc123xyz456                 # Senha de aplicativo Google
MAIL_FROM=noreply@techchallenge.com
EMAIL_ENABLED=true                          # Habilitar envio real
```

### 3. **Docker Compose**

Adicionar no `docker-compose.yml`:

```yaml
services:
  app:
    environment:
      - MAIL_HOST=${MAIL_HOST:-smtp.gmail.com}
      - MAIL_PORT=${MAIL_PORT:-587}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
      - MAIL_FROM=${MAIL_FROM:-noreply@techchallenge.com}
      - EMAIL_ENABLED=${EMAIL_ENABLED:-false}
```

### 4. **Kubernetes Secret**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: email-secrets
  namespace: tech-challenge
type: Opaque
data:
  mail-username: <base64>
  mail-password: <base64>
```

Atualizar `k8s/configmap.yaml`:
```yaml
- name: EMAIL_ENABLED
  value: "true"
- name: MAIL_HOST
  value: "smtp.gmail.com"
- name: MAIL_PORT
  value: "587"
- name: MAIL_FROM
  value: "noreply@techchallenge.com"
```

Atualizar `k8s/secret.yaml`:
```yaml
data:
  MAIL_USERNAME: <base64-email>
  MAIL_PASSWORD: <base64-senha-app>
```

---

## 🔧 Como Configurar Gmail

### 1. **Criar Senha de Aplicativo**

1. Acesse: https://myaccount.google.com/security
2. Ative **Verificação em duas etapas**
3. Vá em **Senhas de app**
4. Selecione **App: Email** e **Dispositivo: Outro**
5. Digite "Tech Challenge"
6. Copie a senha gerada (16 caracteres)

### 2. **Configurar no Projeto**

```yaml
spring.mail:
  username: sua-conta@gmail.com
  password: abcd efgh ijkl mnop    # Senha de app (sem espaços)
```

---

## 🧪 Testando

### Modo Desenvolvimento (Apenas Loga)

```yaml
app.email.enabled: false
```

**Comportamento:**
```
📧 [SIMULAÇÃO] Email seria enviado para: cliente@email.com
   OS #123 alterada para status: EM_EXECUCAO
```

### Modo Produção (Envia Real)

```yaml
app.email.enabled: true
spring.mail.username: seuemail@gmail.com
spring.mail.password: suasenhaapp
```

**Teste Manual:**

```bash
# 1. Criar cliente com EMAIL
POST /api/clientes
{
  "nome": "João Silva",
  "cpfCnpj": "12345678901",
  "contato": "joao@email.com"    # EMAIL válido
}

# 2. Criar OS para o cliente
POST /api/ordens-servico
{ ... }

# 3. Alterar status da OS
PATCH /api/ordens-servico/1/status
{
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Iniciando serviços"
}

# ✅ Email será enviado para joao@email.com
```

---

## 📊 Exemplos de Email Enviado

### Status: AGUARDANDO_APROVACAO

**Assunto:** 🔧 Orçamento disponível - Aguardando aprovação - OS #123

**Corpo:**
```
┌────────────────────────────────────────┐
│   🔧 Tech Challenge Oficina             │
│   Atualização da sua Ordem de Serviço  │
└────────────────────────────────────────┘

Olá, João Silva! 👋

Sua ordem de serviço foi atualizada:

┌─────────────────────────────┐
│ Status Atual:                │
│ [AGUARDANDO APROVAÇÃO] 🟡   │
└─────────────────────────────┘

OS #: 123
Veículo: ABC1D23 - Toyota Corolla
Valor Total: R$ 520,00
Data Criação: 10/01/2026 14:30

⚠️ Ação Necessária: Por favor, aprove o 
orçamento para iniciarmos os serviços.

---
Tech Challenge Oficina Mecânica
📞 (11) 9999-9999 | 📧 contato@techchallenge.com
```

### Status: FINALIZADA

**Assunto:** 🔧 Serviços finalizados - Veículo pronto - OS #123

**Corpo:** Similar ao acima, com:
```
✅ Seu veículo está pronto! Você pode 
retirá-lo a qualquer momento.
```

---

## 🎨 Cores por Status

| Status | Cor | Significado |
|--------|-----|-------------|
| RECEBIDA | Cinza (#6c757d) | Aguardando |
| EM_DIAGNOSTICO | Azul Claro (#17a2b8) | Em análise |
| AGUARDANDO_APROVACAO | Amarelo (#ffc107) | Ação necessária |
| EM_EXECUCAO | Azul (#007bff) | Em andamento |
| FINALIZADA | Verde (#28a745) | Concluído |
| ENTREGUE | Verde Água (#20c997) | Finalizado |

---

## 🔒 Segurança

### ✅ Boas Práticas Implementadas

1. **Senhas não commitadas:** Usar variáveis de ambiente
2. **TLS habilitado:** Conexão criptografada
3. **Timeout configurado:** Evita bloqueios longos
4. **Processamento assíncrono:** Não impacta performance
5. **Validação de email:** Value Object garante email válido
6. **Logs informativos:** Facilita debug

### ⚠️ Importante

- **NUNCA** commite credenciais no código
- Use `.env` ou secrets do Kubernetes
- Configure **Senha de Aplicativo** (não senha normal)
- Monitore limites de envio do SMTP (Gmail: 500/dia)

---

## 📈 Monitoramento

### Logs Importantes

```log
# Email enviado com sucesso
✅ Email enviado com sucesso para cliente@email.com - OS #123 - Status: EM_EXECUCAO

# Cliente sem contato
⚠️ Cliente 45 não possui contato cadastrado. Email não enviado.

# Cliente com telefone
ℹ️ Cliente 45 possui telefone cadastrado (11987654321). Email não enviado.

# Email desabilitado (dev)
📧 [SIMULAÇÃO] Email seria enviado para: cliente@email.com - OS #123

# Erro no envio
❌ Erro ao enviar email para cliente@email.com - OS #123: Connection timeout
```

---

## 🚀 Melhorias Futuras

- [ ] **Templates avançados:** Thymeleaf/Freemarker
- [ ] **Anexos:** PDF do orçamento
- [ ] **Notificação SMS:** Para telefones
- [ ] **Fila de emails:** RabbitMQ/SQS para alta demanda
- [ ] **Email tracking:** Saber se foi aberto
- [ ] **Retry automático:** Retentar envios falhados
- [ ] **HTML/Texto:** Fallback para clientes sem suporte HTML

---

## 🧪 Testes Unitários

### EmailNotificationServiceTest.java

```java
@Test
void deveEnviarEmailQuandoContatoForEmail() {
    // Arrange
    Cliente cliente = criarClienteComEmail("joao@email.com");
    OrdemDeServico os = criarOS(cliente);
    
    // Act
    emailService.notificarMudancaStatusOS(os);
    
    // Assert
    verify(mailSender).send(any(MimeMessage.class));
}

@Test
void naoDeveEnviarEmailQuandoContatoForTelefone() {
    // Arrange
    Cliente cliente = criarClienteComTelefone("11987654321");
    OrdemDeServico os = criarOS(cliente);
    
    // Act
    emailService.notificarMudancaStatusOS(os);
    
    // Assert
    verify(mailSender, never()).send(any(MimeMessage.class));
}
```

---

## 📞 Suporte

### Problemas Comuns

#### ❌ Erro: "Authentication failed"
- Verifique se a senha de aplicativo está correta
- Confirme que a verificação em 2 etapas está ativa

#### ❌ Erro: "Connection timeout"
- Verifique firewall/proxy
- Confirme porta 587 está aberta
- Tente porta 465 (SSL) como alternativa

#### ❌ Email não chega
- Verifique pasta de SPAM
- Confirme que `EMAIL_ENABLED=true`
- Veja logs para confirmar envio

---

## ✅ Checklist de Implementação

- [x] Dependência `spring-boot-starter-mail` adicionada
- [x] Service `EmailNotificationService` criado
- [x] Integração com `OrdemDeServicoService`
- [x] Integração com `AprovarOrcamentoUseCase`
- [x] `@EnableAsync` na aplicação principal
- [x] Configurações no `application.yml`
- [x] Template HTML profissional
- [x] Modo dev/prod configurável
- [x] Tratamento de contato telefone vs email
- [x] Logs informativos
- [x] Documentação completa

---

**Status:** ✅ **IMPLEMENTADO E PRONTO PARA USO**  
**Data:** Janeiro 2026  
**Versão:** 1.0.0

