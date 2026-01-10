package com.techchallenge.service;

import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.model.Veiculo;
import com.techchallenge.domain.service.EmailNotificationService;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
import com.techchallenge.domain.valueobject.Placa;
import com.techchallenge.domain.valueobject.ValorMonetario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailNotificationService - Testes Unitários")
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private OrdemDeServico ordemDeServico;
    private Cliente clienteComEmail;
    private Cliente clienteComTelefone;
    private Cliente clienteSemContato;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "fromEmail", "noreply@techchallenge.com");
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", false); // Modo simulação

        clienteComEmail = new Cliente();
        clienteComEmail.setId(1L);
        clienteComEmail.setNome("João Silva");
        clienteComEmail.setCpfCnpj(new CpfCnpj("11144477735")); // CPF válido usado em outros testes
        clienteComEmail.setContato(new Contato("joao@email.com"));

        clienteComTelefone = new Cliente();
        clienteComTelefone.setId(2L);
        clienteComTelefone.setNome("Maria Santos");
        clienteComTelefone.setCpfCnpj(new CpfCnpj("52998224725")); // CPF válido usado em outros testes
        clienteComTelefone.setContato(new Contato("11987654321"));

        clienteSemContato = new Cliente();
        clienteSemContato.setId(3L);
        clienteSemContato.setNome("Pedro Oliveira");
        clienteSemContato.setCpfCnpj(new CpfCnpj("12345678000195")); // CNPJ válido
        clienteSemContato.setContato(null);

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca(new Placa("ABC1234"));
        veiculo.setModelo("Toyota Corolla");
        veiculo.setMarca("Toyota");

        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(1L);
        ordemDeServico.setCliente(clienteComEmail);
        ordemDeServico.setVeiculo(veiculo);
        ordemDeServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemDeServico.setValorTotalOrcamento(new ValorMonetario(new BigDecimal("500.00")));
        ordemDeServico.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve logar simulação quando email está desabilitado e cliente tem email")
    void deveLogarSimulacaoQuandoEmailDesabilitado() {
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", false);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar email quando cliente não tem contato")
    void naoDeveEnviarEmailQuandoClienteNaoTemContato() {
        ordemDeServico.setCliente(clienteSemContato);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar email quando contato é telefone")
    void naoDeveEnviarEmailQuandoContatoETelefone() {
        ordemDeServico.setCliente(clienteComTelefone);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve enviar email quando habilitado e cliente tem email")
    void deveEnviarEmailQuandoHabilitadoEClienteTemEmail() {
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", true);
        
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve tratar erro ao enviar email sem lançar exceção")
    void deveTratarErroAoEnviarEmail() {
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", true);
        
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Erro de conexão SMTP")).when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve funcionar com diferentes status de OS")
    void deveFuncionarComDiferentesStatus() {
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", false);

        for (StatusOrdemServico status : StatusOrdemServico.values()) {
            ordemDeServico.setStatus(status);
            emailNotificationService.notificarMudancaStatusOS(ordemDeServico);
        }

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve incluir observações no email quando presentes")
    void deveIncluirObservacoesNoEmail() {
        ordemDeServico.setObservacoes("Peça importada chegou");
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", false);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve funcionar com status AGUARDANDO_APROVACAO")
    void deveFuncionarComStatusAguardandoAprovacao() {
        ordemDeServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", false);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve funcionar com status FINALIZADA")
    void deveFuncionarComStatusFinalizada() {
        ordemDeServico.setStatus(StatusOrdemServico.FINALIZADA);
        ordemDeServico.setDataFinalizacao(LocalDateTime.now());
        ReflectionTestUtils.setField(emailNotificationService, "emailEnabled", false);

        emailNotificationService.notificarMudancaStatusOS(ordemDeServico);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}

