package com.techchallenge.service;

import com.techchallenge.domain.dto.*;
import com.techchallenge.domain.exception.EstoqueInsuficienteException;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.*;
import com.techchallenge.domain.repository.*;
import com.techchallenge.domain.service.OrdemDeServicoService;
import com.techchallenge.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemDeServicoService - Testes Unitários")
class OrdemDeServicoServiceTest {

    @Mock
    private OrdemDeServicoRepository ordemDeServicoRepository;
    
    @Mock
    private ClienteRepository clienteRepository;
    
    @Mock
    private VeiculoRepository veiculoRepository;
    
    @Mock
    private ServicoRepository servicoRepository;
    
    @Mock
    private PecaInsumoRepository pecaInsumoRepository;

    @Mock
    private com.techchallenge.domain.service.EmailNotificationService emailNotificationService;

    @InjectMocks
    private OrdemDeServicoService ordemDeServicoService;

    private Cliente cliente;
    private Veiculo veiculo;
    private Servico servico;
    private PecaInsumo peca;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpfCnpj(new CpfCnpj("11144477735"));
        cliente.setContato(new Contato("joao@email.com"));

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca(new Placa("ABC1234"));
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(new AnoVeiculo(2020));
        veiculo.setCliente(cliente);

        servico = new Servico();
        servico.setId(1L);
        servico.setDescricao("Troca de óleo");
        servico.setPreco(new ValorMonetario(new BigDecimal("150.00")));

        peca = new PecaInsumo();
        peca.setId(1L);
        peca.setNome("Filtro de óleo");
        peca.setPreco(new ValorMonetario(new BigDecimal("45.90")));
        peca.setQuantidadeEstoque(100);
    }

    @Test
    @DisplayName("Deve criar OS com sucesso quando todos os dados são válidos")
    void deveCriarOSComSucesso() {
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        
        when(clienteRepository.findByCpfCnpjValor("11144477735")).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlacaValor("ABC1234")).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(ordemDeServicoRepository.save(any(OrdemDeServico.class))).thenAnswer(i -> {
            OrdemDeServico os = i.getArgument(0);
            os.setId(1L);
            return os;
        });

        OrdemDeServicoResponseDTO result = ordemDeServicoService.criarOS(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(result.getValorTotalOrcamento()).isEqualByComparingTo(new BigDecimal("241.80")); // 150 + (45.90 * 2)
        
        verify(clienteRepository).findByCpfCnpjValor("11144477735");
        verify(veiculoRepository).findByPlacaValor("ABC1234");
        verify(servicoRepository).findById(1L);
        verify(pecaInsumoRepository).findById(1L);
        verify(ordemDeServicoRepository, times(2)).save(any(OrdemDeServico.class)); // Uma vez na criação, outra no envio
        verify(pecaInsumoRepository).save(peca);

        assertThat(peca.getQuantidadeEstoque()).isEqualTo(98); // 100 - 2
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        when(clienteRepository.findByCpfCnpjValor("11144477735")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemDeServicoService.criarOS(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente com CPF/CNPJ 11144477735 não encontrado");
        
        verify(clienteRepository).findByCpfCnpjValor("11144477735");
        verifyNoInteractions(veiculoRepository, servicoRepository, pecaInsumoRepository, ordemDeServicoRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque é insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        peca.setQuantidadeEstoque(1);
        
        when(clienteRepository.findByCpfCnpjValor("11144477735")).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlacaValor("ABC1234")).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(peca));

        assertThatThrownBy(() -> ordemDeServicoService.criarOS(input))
                .isInstanceOf(EstoqueInsuficienteException.class);
        
        verify(clienteRepository).findByCpfCnpjValor("11144477735");
        verify(veiculoRepository).findByPlacaValor("ABC1234");
        verify(servicoRepository).findById(1L);
        verify(pecaInsumoRepository).findById(1L);
        verifyNoInteractions(ordemDeServicoRepository);
    }

    @Test
    @DisplayName("Deve atualizar status com sucesso quando transição é válida")
    void deveAtualizarStatusComSucesso() {
        OrdemDeServico os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setValorTotalOrcamento(new ValorMonetario(new BigDecimal("200.00")));
        
        StatusUpdateDTO statusUpdate = new StatusUpdateDTO();
        statusUpdate.setNovoStatus(StatusOrdemServico.EM_EXECUCAO);
        statusUpdate.setObservacao("Cliente aprovou");
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        when(ordemDeServicoRepository.save(any(OrdemDeServico.class))).thenReturn(os);

        OrdemDeServicoResponseDTO result = ordemDeServicoService.atualizarStatus(1L, statusUpdate);

        assertThat(result.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(os.getDataInicioExecucao()).isNotNull();
        verify(ordemDeServicoRepository).findById(1L);
        verify(ordemDeServicoRepository).save(os);
        verify(emailNotificationService).notificarMudancaStatusOS(any(OrdemDeServico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando transição de status é inválida")
    void deveLancarExcecaoQuandoTransicaoInvalida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemServico.FINALIZADA);
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        
        StatusUpdateDTO statusUpdate = new StatusUpdateDTO();
        statusUpdate.setNovoStatus(StatusOrdemServico.EM_EXECUCAO);
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(os));

        assertThatThrownBy(() -> ordemDeServicoService.atualizarStatus(1L, statusUpdate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transição de status inválida");
        
        verify(ordemDeServicoRepository).findById(1L);
        verify(ordemDeServicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir consulta pública com CPF correto")
    void devePermitirConsultaPublicaComCPFCorreto() {
        OrdemDeServico os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setValorTotalOrcamento(new ValorMonetario(new BigDecimal("200.00")));
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(os));

        OrdemDeServicoPublicDTO result = ordemDeServicoService.consultarStatusPublico(1L, "11144477735");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
        assertThat(result.getVeiculoPlaca()).isEqualTo("ABC1234");
        verify(ordemDeServicoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve bloquear consulta pública com CPF incorreto")
    void deveBloquearConsultaPublicaComCPFIncorreto() {
        OrdemDeServico os = new OrdemDeServico();
        os.setId(1L);
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        
        when(ordemDeServicoRepository.findById(1L)).thenReturn(Optional.of(os));

        assertThatThrownBy(() -> ordemDeServicoService.consultarStatusPublico(1L, "52998224725"))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(ordemDeServicoRepository).findById(1L);
    }

    private OrdemDeServicoInputDTO criarInputDTOValido() {
        OrdemDeServicoInputDTO input = new OrdemDeServicoInputDTO();
        input.setCpfCnpjCliente("11144477735");
        
        VeiculoInputDTO veiculoDTO = new VeiculoInputDTO();
        veiculoDTO.setPlaca("ABC1234");
        veiculoDTO.setMarca("Toyota");
        veiculoDTO.setModelo("Corolla");
        veiculoDTO.setAno(2020);
        input.setVeiculo(veiculoDTO);
        
        ItemServicoDTO servicoDTO = new ItemServicoDTO();
        servicoDTO.setServicoId(1L);
        servicoDTO.setQuantidade(1);
        input.setServicos(List.of(servicoDTO));
        
        ItemPecaDTO pecaDTO = new ItemPecaDTO();
        pecaDTO.setPecaInsumoId(1L);
        pecaDTO.setQuantidade(2);
        input.setPecas(List.of(pecaDTO));
        
        input.setObservacoes("Teste");
        
        return input;
    }
}



