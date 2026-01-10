package com.techchallenge.application.usecase;

import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.model.Veiculo;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.domain.usecase.ListarOrdensServicoUseCase;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListarOrdensServicoUseCase - Testes Unitários")
class ListarOrdensServicoUseCaseTest {

    @Mock
    private OrdemDeServicoRepository ordemDeServicoRepository;

    @InjectMocks
    private ListarOrdensServicoUseCase listarOrdensServicoUseCase;

    private Cliente cliente;
    private Veiculo veiculo;
    private LocalDateTime agora;

    @BeforeEach
    void setUp() {
        agora = LocalDateTime.now();

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca(new Placa("ABC1234")); // Adicionar placa ao veículo
    }

    @Test
    @DisplayName("Deve listar OS em andamento excluindo finalizadas e entregues")
    void deveListarOsEmAndamentoExcluindoFinalizadasEEntregues() {
        List<OrdemDeServico> ordensEmAndamento = Arrays.asList(
                criarOS(1L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(5)),
                criarOS(2L, StatusOrdemServico.AGUARDANDO_APROVACAO, agora.minusDays(3)),
                criarOS(3L, StatusOrdemServico.RECEBIDA, agora.minusDays(1))
        );

        when(ordemDeServicoRepository.findOrdensEmAndamento()).thenReturn(ordensEmAndamento);

        List<OrdemDeServicoResponseDTO> resultado = listarOrdensServicoUseCase.executar();

        assertThat(resultado).hasSize(3);
        verify(ordemDeServicoRepository).findOrdensEmAndamento();
    }

    @Test
    @DisplayName("Deve ordenar por prioridade de status: EM_EXECUCAO primeiro")
    void deveOrdenarPorPrioridadeDeStatusEmExecucaoPrimeiro() {
        List<OrdemDeServico> ordens = Arrays.asList(
                criarOS(1L, StatusOrdemServico.RECEBIDA, agora.minusDays(5)),
                criarOS(2L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(1)),
                criarOS(3L, StatusOrdemServico.AGUARDANDO_APROVACAO, agora.minusDays(3))
        );

        when(ordemDeServicoRepository.findOrdensEmAndamento()).thenReturn(ordens);

        List<OrdemDeServicoResponseDTO> resultado = listarOrdensServicoUseCase.executar();

        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(resultado.get(1).getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(resultado.get(2).getStatus()).isEqualTo(StatusOrdemServico.RECEBIDA);
    }

    @Test
    @DisplayName("Deve ordenar por data dentro do mesmo status: mais antigas primeiro")
    void deveOrdenarPorDataDentroDoMesmoStatusMaisAntigasPrimeiro() {
        List<OrdemDeServico> ordens = Arrays.asList(
                criarOS(1L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(1)),  // Mais recente
                criarOS(2L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(5)),  // Mais antiga
                criarOS(3L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(3))   // Meio termo
        );

        when(ordemDeServicoRepository.findOrdensEmAndamento()).thenReturn(ordens);

        List<OrdemDeServicoResponseDTO> resultado = listarOrdensServicoUseCase.executar();

        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).getId()).isEqualTo(2L); // Mais antiga (5 dias)
        assertThat(resultado.get(1).getId()).isEqualTo(3L); // Meio termo (3 dias)
        assertThat(resultado.get(2).getId()).isEqualTo(1L); // Mais recente (1 dia)
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há OS em andamento")
    void deveRetornarListaVaziaQuandoNaoHaOsEmAndamento() {
        when(ordemDeServicoRepository.findOrdensEmAndamento()).thenReturn(Arrays.asList());

        List<OrdemDeServicoResponseDTO> resultado = listarOrdensServicoUseCase.executar();

        assertThat(resultado).isEmpty();
        verify(ordemDeServicoRepository).findOrdensEmAndamento();
    }

    @Test
    @DisplayName("Deve ordenar corretamente mix de status e datas")
    void deveOrdenarCorretamenteMixDeStatusEDatas() {
        List<OrdemDeServico> ordens = Arrays.asList(
                criarOS(1L, StatusOrdemServico.RECEBIDA, agora.minusDays(10)),            // Prioridade 4, mais antiga
                criarOS(2L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(2)),          // Prioridade 1, recente
                criarOS(3L, StatusOrdemServico.AGUARDANDO_APROVACAO, agora.minusDays(7)), // Prioridade 2, antiga
                criarOS(4L, StatusOrdemServico.EM_DIAGNOSTICO, agora.minusDays(5)),       // Prioridade 3
                criarOS(5L, StatusOrdemServico.EM_EXECUCAO, agora.minusDays(8)),          // Prioridade 1, mais antiga
                criarOS(6L, StatusOrdemServico.RECEBIDA, agora.minusDays(3))              // Prioridade 4, recente
        );

        when(ordemDeServicoRepository.findOrdensEmAndamento()).thenReturn(ordens);

        List<OrdemDeServicoResponseDTO> resultado = listarOrdensServicoUseCase.executar();

        assertThat(resultado).hasSize(6);
        
        assertThat(resultado.get(0).getId()).isEqualTo(5L); // 8 dias
        assertThat(resultado.get(1).getId()).isEqualTo(2L); // 2 dias
        
        assertThat(resultado.get(2).getId()).isEqualTo(3L); // 7 dias
        
        assertThat(resultado.get(3).getId()).isEqualTo(4L); // 5 dias
        
        assertThat(resultado.get(4).getId()).isEqualTo(1L); // 10 dias
        assertThat(resultado.get(5).getId()).isEqualTo(6L); // 3 dias
    }

    private OrdemDeServico criarOS(Long id, StatusOrdemServico status, LocalDateTime dataCriacao) {
        OrdemDeServico os = new OrdemDeServico();
        os.setId(id);
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setStatus(status);
        os.setDataCriacao(dataCriacao);
        os.setValorTotalOrcamento(new ValorMonetario(new BigDecimal("100.00")));
        return os;
    }
}

