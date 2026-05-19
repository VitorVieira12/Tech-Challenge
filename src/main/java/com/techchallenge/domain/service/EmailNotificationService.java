package com.techchallenge.domain.service;

import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;

/**
 * Serviço responsável por enviar notificações por email aos clientes.
 * Fase 2: Implementação de notificação de atualização de status de OS via email.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@techchallenge.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Notifica o cliente sobre alteração de status da Ordem de Serviço.
     * Envia email apenas se o contato do cliente for do tipo EMAIL.
     * 
     * @param os Ordem de Serviço atualizada
     */
    @Async
    public void notificarMudancaStatusOS(OrdemDeServico os) {
        Cliente cliente = os.getCliente();
        
        if (cliente.getContato() == null) {
            log.warn("Cliente {} não possui contato cadastrado. Email não enviado.", cliente.getId());
            return;
        }

        if (!cliente.getContato().isEmail()) {
            log.info("Cliente {} possui telefone cadastrado ({}). Email não enviado. " +
                    "Considere implementar SMS no futuro.", 
                    cliente.getId(), 
                    cliente.getContato().getFormatado());
            return;
        }

        if (!emailEnabled) {
            log.info("📧 [SIMULAÇÃO] Email seria enviado para: {} - OS #{} alterada para status: {}", 
                    cliente.getContato().getValor(), 
                    os.getId(), 
                    os.getStatus());
            return;
        }

        String emailCliente = cliente.getContato().getValor();
        
        try {
            enviarEmailHtml(emailCliente, os);
            log.info("✅ Email enviado com sucesso para {} - OS #{} - Status: {}", 
                    emailCliente, os.getId(), os.getStatus());
        } catch (Exception e) {
            log.error("❌ Erro ao enviar email para {} - OS #{}: {}", 
                    emailCliente, os.getId(), e.getMessage());
        }
    }

    /**
     * Envia email HTML formatado com informações da OS.
     */
    private void enviarEmailHtml(String destinatario, OrdemDeServico os) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(destinatario);
        helper.setSubject(gerarAssunto(os));
        helper.setText(gerarCorpoEmailHtml(os), true); // true = HTML

        mailSender.send(message);
    }

    /**
     * Envia email simples (fallback) - pode ser usado em caso de erro no HTML.
     * Mantido para possível uso futuro.
     */
    @SuppressWarnings("unused")
    private void enviarEmailSimples(String destinatario, OrdemDeServico os) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinatario);
        message.setSubject(gerarAssunto(os));
        message.setText(gerarCorpoEmailTexto(os));

        mailSender.send(message);
    }

    /**
     * Gera assunto do email baseado no status.
     */
    private String gerarAssunto(OrdemDeServico os) {
        StatusOrdemServico status = os.getStatus();
        String assunto = switch (status) {
            case RECEBIDA -> "Ordem de Serviço Recebida";
            case EM_DIAGNOSTICO -> "Seu veículo está em diagnóstico";
            case AGUARDANDO_APROVACAO -> "Orçamento disponível - Aguardando aprovação";
            case EM_EXECUCAO -> "Serviços iniciados no seu veículo";
            case FINALIZADA -> "Serviços finalizados - Veículo pronto";
            case ENTREGUE -> "Veículo entregue - Obrigado pela preferência";
            case CANCELADA -> "Ordem de Serviço Cancelada";
        };
        
        return String.format("🔧 %s - OS #%d", assunto, os.getId());
    }

    /**
     * Gera corpo do email em HTML formatado.
     */
    private String gerarCorpoEmailHtml(OrdemDeServico os) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        String statusDescricao = obterDescricaoStatus(os.getStatus());
        String statusColor = obterCorStatus(os.getStatus());
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>");
        
        html.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center;'>");
        html.append("<h2 style='margin: 0;'>🔧 Tech Challenge Oficina</h2>");
        html.append("<p style='margin: 5px 0 0 0; font-size: 14px;'>Atualização da sua Ordem de Serviço</p>");
        html.append("</div>");
        
        html.append("<div style='padding: 30px; background: #f9f9f9;'>");
        html.append(String.format("<h3 style='color: #667eea; margin-top: 0;'>Olá, %s! 👋</h3>", os.getCliente().getNome()));
        html.append("<p>Sua ordem de serviço foi atualizada:</p>");
        
        html.append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
        html.append(String.format("<p style='margin: 0 0 10px 0;'><strong>Status Atual:</strong></p>"));
        html.append(String.format("<div style='background: %s; color: white; padding: 12px 20px; border-radius: 6px; display: inline-block; font-weight: bold;'>", statusColor));
        html.append(statusDescricao);
        html.append("</div>");
        html.append("</div>");
        
        html.append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
        html.append(String.format("<p style='margin: 8px 0;'><strong>OS #:</strong> %d</p>", os.getId()));
        html.append(String.format("<p style='margin: 8px 0;'><strong>Veículo:</strong> %s - %s</p>", 
                os.getVeiculo().getPlaca().getValor(), 
                os.getVeiculo().getModelo()));
        html.append(String.format("<p style='margin: 8px 0;'><strong>Valor Total:</strong> %s</p>", 
                os.getValorTotalOrcamento().getFormatado()));
        html.append(String.format("<p style='margin: 8px 0;'><strong>Data Criação:</strong> %s</p>", 
                os.getDataCriacao().format(formatter)));
        
        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            html.append("<div style='background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin-top: 15px;'>");
            html.append("<p style='margin: 0; color: #856404;'><strong>⚠️ Ação Necessária:</strong> Por favor, aprove o orçamento para iniciarmos os serviços.</p>");
            html.append("</div>");
        }
        
        if (os.getStatus() == StatusOrdemServico.FINALIZADA) {
            html.append("<div style='background: #d4edda; border-left: 4px solid #28a745; padding: 12px; margin-top: 15px;'>");
            html.append("<p style='margin: 0; color: #155724;'><strong>✅ Seu veículo está pronto!</strong> Você pode retirá-lo a qualquer momento.</p>");
            html.append("</div>");
        }
        
        html.append("</div>");
        
        if (os.getObservacoes() != null && !os.getObservacoes().isBlank()) {
            html.append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            html.append("<p style='margin: 0 0 10px 0;'><strong>Observações:</strong></p>");
            html.append(String.format("<p style='margin: 0; color: #666; font-style: italic;'>%s</p>", 
                    os.getObservacoes().replace("\n", "<br/>")));
            html.append("</div>");
        }
        
        html.append("<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>");
        html.append("<p style='margin: 5px 0; color: #666; font-size: 14px;'>Para consultar sua OS, acesse:</p>");
        html.append("<p style='margin: 5px 0;'><a href='http://localhost:8080/swagger-ui.html' style='color: #667eea; text-decoration: none; font-weight: bold;'>Portal do Cliente</a></p>");
        html.append("<p style='margin: 20px 0 5px 0; color: #999; font-size: 12px;'>Tech Challenge Oficina Mecânica</p>");
        html.append("<p style='margin: 5px 0; color: #999; font-size: 12px;'>📞 (11) 9999-9999 | 📧 contato@techchallenge.com</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * Gera corpo do email em texto simples (fallback).
     */
    private String gerarCorpoEmailTexto(OrdemDeServico os) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        StringBuilder texto = new StringBuilder();
        texto.append("Tech Challenge Oficina\n");
        texto.append("======================\n\n");
        texto.append(String.format("Olá, %s!\n\n", os.getCliente().getNome()));
        texto.append("Sua ordem de serviço foi atualizada:\n\n");
        texto.append(String.format("OS #: %d\n", os.getId()));
        texto.append(String.format("Status: %s\n", obterDescricaoStatus(os.getStatus())));
        texto.append(String.format("Veículo: %s - %s\n", os.getVeiculo().getPlaca().getValor(), os.getVeiculo().getModelo()));
        texto.append(String.format("Valor Total: %s\n", os.getValorTotalOrcamento().getFormatado()));
        texto.append(String.format("Data Criação: %s\n", os.getDataCriacao().format(formatter)));
        
        if (os.getObservacoes() != null && !os.getObservacoes().isBlank()) {
            texto.append(String.format("\nObservações:\n%s\n", os.getObservacoes()));
        }
        
        texto.append("\n---\n");
        texto.append("Tech Challenge Oficina Mecânica\n");
        texto.append("📞 (11) 9999-9999 | 📧 contato@techchallenge.com\n");
        
        return texto.toString();
    }

    /**
     * Retorna descrição amigável do status.
     */
    private String obterDescricaoStatus(StatusOrdemServico status) {
        return switch (status) {
            case RECEBIDA -> "Recebida";
            case EM_DIAGNOSTICO -> "Em Diagnóstico";
            case AGUARDANDO_APROVACAO -> "Aguardando Aprovação";
            case EM_EXECUCAO -> "Em Execução";
            case FINALIZADA -> "Finalizada";
            case ENTREGUE -> "Entregue";
            case CANCELADA -> "Cancelada";
        };
    }

    /**
     * Retorna cor hexadecimal para o status.
     */
    private String obterCorStatus(StatusOrdemServico status) {
        return switch (status) {
            case RECEBIDA -> "#6c757d";           // Cinza
            case EM_DIAGNOSTICO -> "#17a2b8";     // Azul Claro
            case AGUARDANDO_APROVACAO -> "#ffc107"; // Amarelo
            case EM_EXECUCAO -> "#007bff";        // Azul
            case FINALIZADA -> "#28a745";         // Verde
            case ENTREGUE -> "#20c997";           // Verde Água
            case CANCELADA -> "#dc3545";          // Vermelho
        };
    }
}

