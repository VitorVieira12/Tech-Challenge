package com.techchallenge.service;

import com.techchallenge.domain.dto.VeiculoDTO;
import com.techchallenge.domain.dto.VeiculoResponseDTO;
import com.techchallenge.domain.exception.DuplicateResourceException;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.Veiculo;
import com.techchallenge.domain.repository.VeiculoRepository;
import com.techchallenge.domain.service.ClienteService;
import com.techchallenge.domain.service.VeiculoService;
import com.techchallenge.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoService - Testes Unitários")
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private VeiculoService veiculoService;

    private VeiculoDTO veiculoDTO;
    private Veiculo veiculo;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpfCnpj(new CpfCnpj("11144477735"));
        cliente.setContato(new Contato("joao@email.com"));

        veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("ABC1234");
        veiculoDTO.setMarca("Toyota");
        veiculoDTO.setModelo("Corolla");
        veiculoDTO.setAno(2020);
        veiculoDTO.setClienteId(1L);

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca(new Placa("ABC1234"));
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(new AnoVeiculo(2020));
        veiculo.setCliente(cliente);
    }

    @Test
    @DisplayName("Deve criar veículo com sucesso quando dados são válidos")
    void deveCriarVeiculoComSucesso() {
        when(veiculoRepository.existsByPlacaValor("ABC1234")).thenReturn(false);
        when(clienteService.buscarEntidadePorId(1L)).thenReturn(cliente);
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        VeiculoResponseDTO result = veiculoService.criar(veiculoDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlaca()).isEqualTo("ABC1234");
        assertThat(result.getMarca()).isEqualTo("Toyota");
        assertThat(result.getModelo()).isEqualTo("Corolla");
        assertThat(result.getAno()).isEqualTo(2020);
        
        verify(veiculoRepository).existsByPlacaValor("ABC1234");
        verify(clienteService).buscarEntidadePorId(1L);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve converter placa para maiúsculo ao criar veículo")
    void deveConverterPlacaParaMaiusculoAoCriar() {
        veiculoDTO.setPlaca("abc1234");
        
        when(veiculoRepository.existsByPlacaValor("ABC1234")).thenReturn(false);
        when(clienteService.buscarEntidadePorId(1L)).thenReturn(cliente);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo v = invocation.getArgument(0);
            assertThat(v.getPlaca().getValor()).isEqualTo("ABC1234");
            return veiculo;
        });

        veiculoService.criar(veiculoDTO);

        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando placa já existe")
    void deveLancarExcecaoQuandoPlacaJaExiste() {
        when(veiculoRepository.existsByPlacaValor("ABC1234")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.criar(veiculoDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Já existe um veículo cadastrado com esta placa");
        
        verify(veiculoRepository).existsByPlacaValor("ABC1234");
        verify(clienteService, never()).buscarEntidadePorId(any());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        when(veiculoRepository.existsByPlacaValor("ABC1234")).thenReturn(false);
        when(clienteService.buscarEntidadePorId(1L))
                .thenThrow(new ResourceNotFoundException("Cliente", 1L));

        assertThatThrownBy(() -> veiculoService.criar(veiculoDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(veiculoRepository).existsByPlacaValor("ABC1234");
        verify(clienteService).buscarEntidadePorId(1L);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar veículo por ID com sucesso")
    void deveBuscarVeiculoPorIdComSucesso() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));

        VeiculoResponseDTO result = veiculoService.buscarPorId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlaca()).isEqualTo("ABC1234");
        
        verify(veiculoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não existe")
    void deveLancarExcecaoQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.buscarPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(veiculoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void deveListarTodosOsVeiculos() {
        Veiculo veiculo2 = new Veiculo();
        veiculo2.setId(2L);
        veiculo2.setPlaca(new Placa("XYZ5678"));
        veiculo2.setMarca("Honda");
        veiculo2.setModelo("Civic");
        veiculo2.setAno(new AnoVeiculo(2021));
        veiculo2.setCliente(cliente);

        when(veiculoRepository.findAll()).thenReturn(Arrays.asList(veiculo, veiculo2));

        List<VeiculoResponseDTO> result = veiculoService.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlaca()).isEqualTo("ABC1234");
        assertThat(result.get(1).getPlaca()).isEqualTo("XYZ5678");
        
        verify(veiculoRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar veículos por cliente")
    void deveListarVeiculosPorCliente() {
        when(clienteService.buscarEntidadePorId(1L)).thenReturn(cliente);
        when(veiculoRepository.findByCliente(cliente)).thenReturn(Arrays.asList(veiculo));

        List<VeiculoResponseDTO> result = veiculoService.listarPorCliente(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlaca()).isEqualTo("ABC1234");
        
        verify(clienteService).buscarEntidadePorId(1L);
        verify(veiculoRepository).findByCliente(cliente);
    }

    @Test
    @DisplayName("Deve atualizar veículo com sucesso")
    void deveAtualizarVeiculoComSucesso() {
        VeiculoDTO atualizacaoDTO = new VeiculoDTO();
        atualizacaoDTO.setPlaca("ABC1234");
        atualizacaoDTO.setMarca("Toyota");
        atualizacaoDTO.setModelo("Corolla XEI");
        atualizacaoDTO.setAno(2021);
        atualizacaoDTO.setClienteId(1L);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(clienteService.buscarEntidadePorId(1L)).thenReturn(cliente);
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        VeiculoResponseDTO result = veiculoService.atualizar(1L, atualizacaoDTO);

        assertThat(result).isNotNull();
        verify(veiculoRepository).findById(1L);
        verify(clienteService).buscarEntidadePorId(1L);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar placa para uma que já existe")
    void deveLancarExcecaoAoAtualizarPlacaParaUmaQueJaExiste() {
        VeiculoDTO atualizacaoDTO = new VeiculoDTO();
        atualizacaoDTO.setPlaca("XYZ5678");
        atualizacaoDTO.setMarca("Toyota");
        atualizacaoDTO.setModelo("Corolla");
        atualizacaoDTO.setAno(2020);
        atualizacaoDTO.setClienteId(1L);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.existsByPlacaValor("XYZ5678")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.atualizar(1L, atualizacaoDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Já existe um veículo cadastrado com esta placa");
        
        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository).existsByPlacaValor("XYZ5678");
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar veículo com sucesso")
    void deveDeletarVeiculoComSucesso() {
        when(veiculoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(veiculoRepository).deleteById(1L);

        assertThatCode(() -> veiculoService.deletar(1L))
                .doesNotThrowAnyException();

        verify(veiculoRepository).existsById(1L);
        verify(veiculoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar veículo inexistente")
    void deveLancarExcecaoAoDeletarVeiculoInexistente() {
        when(veiculoRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> veiculoService.deletar(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(veiculoRepository).existsById(1L);
        verify(veiculoRepository, never()).deleteById(any());
    }
}

