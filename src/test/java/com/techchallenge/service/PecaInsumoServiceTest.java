package com.techchallenge.service;

import com.techchallenge.domain.dto.PecaInsumoDTO;
import com.techchallenge.domain.dto.PecaInsumoResponseDTO;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.PecaInsumo;
import com.techchallenge.domain.repository.PecaInsumoRepository;
import com.techchallenge.domain.service.PecaInsumoService;
import com.techchallenge.domain.valueobject.ValorMonetario;
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
@DisplayName("PecaInsumoService - Testes Unitários")
class PecaInsumoServiceTest {

    @Mock
    private PecaInsumoRepository pecaInsumoRepository;

    @InjectMocks
    private PecaInsumoService pecaInsumoService;

    private PecaInsumoDTO pecaInsumoDTO;
    private PecaInsumo pecaInsumo;

    @BeforeEach
    void setUp() {
        pecaInsumoDTO = new PecaInsumoDTO();
        pecaInsumoDTO.setNome("Filtro de óleo");
        pecaInsumoDTO.setDescricao("Filtro de óleo para motor");
        pecaInsumoDTO.setPreco(new BigDecimal("45.90"));
        pecaInsumoDTO.setQuantidadeEstoque(100);

        pecaInsumo = new PecaInsumo();
        pecaInsumo.setId(1L);
        pecaInsumo.setNome("Filtro de óleo");
        pecaInsumo.setDescricao("Filtro de óleo para motor");
        pecaInsumo.setPreco(new ValorMonetario(new BigDecimal("45.90")));
        pecaInsumo.setQuantidadeEstoque(100);
    }

    @Test
    @DisplayName("Deve criar peça/insumo com sucesso quando dados são válidos")
    void deveCriarPecaInsumoComSucesso() {
        when(pecaInsumoRepository.save(any(PecaInsumo.class))).thenReturn(pecaInsumo);

        PecaInsumoResponseDTO result = pecaInsumoService.criar(pecaInsumoDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("Filtro de óleo");
        assertThat(result.getPreco()).isEqualByComparingTo(new BigDecimal("45.90"));
        assertThat(result.getQuantidadeEstoque()).isEqualTo(100);
        
        verify(pecaInsumoRepository).save(any(PecaInsumo.class));
    }

    @Test
    @DisplayName("Deve buscar peça/insumo por ID com sucesso")
    void deveBuscarPecaInsumoPorIdComSucesso() {
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(pecaInsumo));

        PecaInsumoResponseDTO result = pecaInsumoService.buscarPorId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("Filtro de óleo");
        
        verify(pecaInsumoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando peça/insumo não existe")
    void deveLancarExcecaoQuandoPecaInsumoNaoExiste() {
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pecaInsumoService.buscarPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(pecaInsumoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve listar todas as peças/insumos")
    void deveListarTodasAsPecasInsumos() {
        PecaInsumo peca2 = new PecaInsumo();
        peca2.setId(2L);
        peca2.setNome("Pastilha de freio");
        peca2.setDescricao("Pastilha de freio dianteira");
        peca2.setPreco(new ValorMonetario(new BigDecimal("120.00")));
        peca2.setQuantidadeEstoque(50);

        when(pecaInsumoRepository.findAll()).thenReturn(Arrays.asList(pecaInsumo, peca2));

        List<PecaInsumoResponseDTO> result = pecaInsumoService.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNome()).isEqualTo("Filtro de óleo");
        assertThat(result.get(1).getNome()).isEqualTo("Pastilha de freio");
        
        verify(pecaInsumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar peça/insumo com sucesso")
    void deveAtualizarPecaInsumoComSucesso() {
        PecaInsumoDTO atualizacaoDTO = new PecaInsumoDTO();
        atualizacaoDTO.setNome("Filtro de óleo sintético");
        atualizacaoDTO.setDescricao("Filtro de óleo sintético premium");
        atualizacaoDTO.setPreco(new BigDecimal("65.90"));
        atualizacaoDTO.setQuantidadeEstoque(150);

        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(pecaInsumo));
        when(pecaInsumoRepository.save(any(PecaInsumo.class))).thenReturn(pecaInsumo);

        PecaInsumoResponseDTO result = pecaInsumoService.atualizar(1L, atualizacaoDTO);

        assertThat(result).isNotNull();
        verify(pecaInsumoRepository).findById(1L);
        verify(pecaInsumoRepository).save(any(PecaInsumo.class));
    }

    @Test
    @DisplayName("Deve deletar peça/insumo com sucesso")
    void deveDeletarPecaInsumoComSucesso() {
        when(pecaInsumoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pecaInsumoRepository).deleteById(1L);

        assertThatCode(() -> pecaInsumoService.deletar(1L))
                .doesNotThrowAnyException();

        verify(pecaInsumoRepository).existsById(1L);
        verify(pecaInsumoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar peça/insumo inexistente")
    void deveLancarExcecaoAoDeletarPecaInsumoInexistente() {
        when(pecaInsumoRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> pecaInsumoService.deletar(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(pecaInsumoRepository).existsById(1L);
        verify(pecaInsumoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve atualizar estoque com sucesso quando ajuste é positivo")
    void deveAtualizarEstoqueComSucessoQuandoAjustePositivo() {
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(pecaInsumo));
        when(pecaInsumoRepository.save(any(PecaInsumo.class))).thenReturn(pecaInsumo);

        PecaInsumoResponseDTO result = pecaInsumoService.atualizarEstoque(1L, 50);

        assertThat(result).isNotNull();
        assertThat(pecaInsumo.getQuantidadeEstoque()).isEqualTo(150);
        
        verify(pecaInsumoRepository).findById(1L);
        verify(pecaInsumoRepository).save(pecaInsumo);
    }

    @Test
    @DisplayName("Deve atualizar estoque com sucesso quando ajuste é negativo")
    void deveAtualizarEstoqueComSucessoQuandoAjusteNegativo() {
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(pecaInsumo));
        when(pecaInsumoRepository.save(any(PecaInsumo.class))).thenReturn(pecaInsumo);

        PecaInsumoResponseDTO result = pecaInsumoService.atualizarEstoque(1L, -30);

        assertThat(result).isNotNull();
        assertThat(pecaInsumo.getQuantidadeEstoque()).isEqualTo(70);
        
        verify(pecaInsumoRepository).findById(1L);
        verify(pecaInsumoRepository).save(pecaInsumo);
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque ficaria negativo")
    void deveLancarExcecaoQuandoEstoqueFicariaNegativo() {
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.of(pecaInsumo));

        assertThatThrownBy(() -> pecaInsumoService.atualizarEstoque(1L, -150))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade em estoque não pode ser negativa");
        
        verify(pecaInsumoRepository).findById(1L);
        verify(pecaInsumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar estoque de peça inexistente")
    void deveLancarExcecaoAoAtualizarEstoqueDePecaInexistente() {
        when(pecaInsumoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pecaInsumoService.atualizarEstoque(1L, 10))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(pecaInsumoRepository).findById(1L);
        verify(pecaInsumoRepository, never()).save(any());
    }
}

