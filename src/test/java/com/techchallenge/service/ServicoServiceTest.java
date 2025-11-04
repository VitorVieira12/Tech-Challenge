package com.techchallenge.service;

import com.techchallenge.domain.dto.ServicoDTO;
import com.techchallenge.domain.dto.ServicoResponseDTO;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Servico;
import com.techchallenge.domain.repository.ServicoRepository;
import com.techchallenge.domain.service.ServicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicoService - Testes Unitários")
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private ServicoDTO servicoDTO;
    private Servico servico;

    @BeforeEach
    void setUp() {
        servicoDTO = new ServicoDTO();
        servicoDTO.setDescricao("Troca de óleo");
        servicoDTO.setPreco(new BigDecimal("150.00"));

        servico = new Servico();
        servico.setId(1L);
        servico.setDescricao("Troca de óleo");
        servico.setPreco(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve criar serviço com sucesso quando dados são válidos")
    void deveCriarServicoComSucesso() {
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        ServicoResponseDTO result = servicoService.criar(servicoDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescricao()).isEqualTo("Troca de óleo");
        assertThat(result.getPreco()).isEqualByComparingTo(new BigDecimal("150.00"));
        
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        ServicoResponseDTO result = servicoService.buscarPorId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescricao()).isEqualTo("Troca de óleo");
        
        verify(servicoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço não existe")
    void deveLancarExcecaoQuandoServicoNaoExiste() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.buscarPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(servicoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void deveListarTodosOsServicos() {
        Servico servico2 = new Servico();
        servico2.setId(2L);
        servico2.setDescricao("Alinhamento e balanceamento");
        servico2.setPreco(new BigDecimal("80.00"));

        when(servicoRepository.findAll()).thenReturn(Arrays.asList(servico, servico2));

        List<ServicoResponseDTO> result = servicoService.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescricao()).isEqualTo("Troca de óleo");
        assertThat(result.get(1).getDescricao()).isEqualTo("Alinhamento e balanceamento");
        
        verify(servicoRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar serviço com sucesso")
    void deveAtualizarServicoComSucesso() {
        ServicoDTO atualizacaoDTO = new ServicoDTO();
        atualizacaoDTO.setDescricao("Troca de óleo sintético");
        atualizacaoDTO.setPreco(new BigDecimal("200.00"));

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        ServicoResponseDTO result = servicoService.atualizar(1L, atualizacaoDTO);

        assertThat(result).isNotNull();
        verify(servicoRepository).findById(1L);
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar serviço inexistente")
    void deveLancarExcecaoAoAtualizarServicoInexistente() {
        ServicoDTO atualizacaoDTO = new ServicoDTO();
        atualizacaoDTO.setDescricao("Troca de óleo");
        atualizacaoDTO.setPreco(new BigDecimal("150.00"));

        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.atualizar(1L, atualizacaoDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(servicoRepository).findById(1L);
        verify(servicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar serviço com sucesso")
    void deveDeletarServicoComSucesso() {
        when(servicoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(servicoRepository).deleteById(1L);

        assertThatCode(() -> servicoService.deletar(1L))
                .doesNotThrowAnyException();

        verify(servicoRepository).existsById(1L);
        verify(servicoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar serviço inexistente")
    void deveLancarExcecaoAoDeletarServicoInexistente() {
        when(servicoRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> servicoService.deletar(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(servicoRepository).existsById(1L);
        verify(servicoRepository, never()).deleteById(any());
    }
}

