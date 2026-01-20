package com.techchallenge.application.usecase;

import com.techchallenge.domain.dto.AprovacaoOrcamentoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.model.Veiculo;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.domain.usecase.AprovarOrcamentoUseCase;
import com.techchallenge.domain.valueobject.Placa;
import com.techchallenge.domain.valueobject.ValorMonetario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AprovarOrcamentoUseCase - Testes Unitários")
class AprovarOrcamentoUseCaseTest {

    @Mock
    private OrdemDeServicoRepository ordemDeServicoRepository;

    @InjectMocks
    private AprovarOrcamentoUseCase aprovarOrcamentoUseCase;

    private OrdemDeServico ordemDeServico;
    private Cliente cliente;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca(new Placa("ABC1234")); // Adicionar placa ao veículo

        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(1L);
        ordemDeServico.setCliente(cliente);
        ordemDeServico.setVeiculo(veiculo);
        ordemDeServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ordemDeServico.setValorTotalOrcamento(new ValorMonetario(new BigDecimal("500.00")));
        ordemDeServico.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve aprovar orçamento e mudar status para EM_EXECUCAO")
    void deveAprovarOrcamentoEMudarStatusParaEmExecucao() {
        AprovacaoOrcamentoInputDTO input = new AprovacaoOrcamentoInputDTO(true, null);
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(ordemDeServico));
        when(ordemDeServicoRepository.save(any(OrdemDeServico.class))).thenAnswer(i -> i.getArgument(0));

        OrdemDeServicoResponseDTO response = aprovarOrcamentoUseCase.executar(1L, input);

        assertThat(response).isNotNull();
        verify(ordemDeServicoRepository).findById(1L);
        verify(ordemDeServicoRepository).save(argThat(os -> 
            os.getStatus() == StatusOrdemServico.EM_EXECUCAO &&
            os.getDataInicioExecucao() != null &&
            os.getObservacoes().contains("APROVADO")
        ));
    }

    @Test
    @DisplayName("Deve recusar orçamento e mudar status para RECEBIDA")
    void deveRecusarOrcamentoEMudarStatusParaRecebida() {
        AprovacaoOrcamentoInputDTO input = new AprovacaoOrcamentoInputDTO(false, "Valor muito alto");
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(ordemDeServico));
        when(ordemDeServicoRepository.save(any(OrdemDeServico.class))).thenAnswer(i -> i.getArgument(0));

        OrdemDeServicoResponseDTO response = aprovarOrcamentoUseCase.executar(1L, input);

        assertThat(response).isNotNull();
        verify(ordemDeServicoRepository).findById(1L);
        verify(ordemDeServicoRepository).save(argThat(os -> 
            os.getStatus() == StatusOrdemServico.RECEBIDA &&
            os.getObservacoes().contains("RECUSADO") &&
            os.getObservacoes().contains("Valor muito alto")
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção quando OS não existe")
    void deveLancarExcecaoQuandoOsNaoExiste() {
        AprovacaoOrcamentoInputDTO input = new AprovacaoOrcamentoInputDTO(true, null);
        when(ordemDeServicoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aprovarOrcamentoUseCase.executar(999L, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ordem de Serviço");
    }

    @Test
    @DisplayName("Deve lançar exceção quando OS não está aguardando aprovação")
    void deveLancarExcecaoQuandoOsNaoEstaAguardandoAprovacao() {
        ordemDeServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        AprovacaoOrcamentoInputDTO input = new AprovacaoOrcamentoInputDTO(true, null);
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(ordemDeServico));

        assertThatThrownBy(() -> aprovarOrcamentoUseCase.executar(1L, input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não está aguardando aprovação");
    }

    @Test
    @DisplayName("Deve adicionar observação ao recusar sem motivo")
    void deveAdicionarObservacaoAoRecusarSemMotivo() {
        AprovacaoOrcamentoInputDTO input = new AprovacaoOrcamentoInputDTO(false, null); // motivoRecusa = null
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(ordemDeServico));
        when(ordemDeServicoRepository.save(any(OrdemDeServico.class))).thenAnswer(i -> i.getArgument(0));

        aprovarOrcamentoUseCase.executar(1L, input);

        verify(ordemDeServicoRepository).save(argThat(os -> 
            os.getObservacoes().contains("RECUSADO") &&
            os.getObservacoes().contains("Não informado")
        ));
    }
}

