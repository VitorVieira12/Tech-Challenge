package com.techchallenge.service;

import com.techchallenge.domain.dto.ClienteDTO;
import com.techchallenge.domain.dto.ClienteResponseDTO;
import com.techchallenge.domain.exception.DuplicateResourceException;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.service.ClienteService;
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
@DisplayName("ClienteService - Testes Unitários")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteDTO clienteDTO;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteDTO = new ClienteDTO();
        clienteDTO.setNome("João Silva");
        clienteDTO.setCpfCnpj("12345678901");
        clienteDTO.setContato("joao@email.com");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpfCnpj("12345678901");
        cliente.setContato("joao@email.com");
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso quando dados são válidos")
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.criar(clienteDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("João Silva");
        assertThat(result.getCpfCnpj()).isEqualTo("12345678901");
        
        verify(clienteRepository).existsByCpfCnpj("12345678901");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF/CNPJ já existe")
    void deveLancarExcecaoQuandoCpfCnpjJaExiste() {
        when(clienteRepository.existsByCpfCnpj("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(clienteDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Já existe um cliente cadastrado com este CPF/CNPJ");
        
        verify(clienteRepository).existsByCpfCnpj("12345678901");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponseDTO result = clienteService.buscarPorId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("João Silva");
        
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosOsClientes() {
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNome("Maria Santos");
        cliente2.setCpfCnpj("98765432100");
        cliente2.setContato("maria@email.com");

        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));

        List<ClienteResponseDTO> result = clienteService.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNome()).isEqualTo("João Silva");
        assertThat(result.get(1).getNome()).isEqualTo("Maria Santos");
        
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() {
        ClienteDTO atualizacaoDTO = new ClienteDTO();
        atualizacaoDTO.setNome("João Silva Atualizado");
        atualizacaoDTO.setCpfCnpj("12345678901");
        atualizacaoDTO.setContato("joao.novo@email.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponseDTO result = clienteService.atualizar(1L, atualizacaoDTO);

        assertThat(result).isNotNull();
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar CPF para um que já existe")
    void deveLancarExcecaoAoAtualizarCpfParaUmQueJaExiste() {
        ClienteDTO atualizacaoDTO = new ClienteDTO();
        atualizacaoDTO.setNome("João Silva");
        atualizacaoDTO.setCpfCnpj("98765432100");
        atualizacaoDTO.setContato("joao@email.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpfCnpj("98765432100")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.atualizar(1L, atualizacaoDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Já existe um cliente cadastrado com este CPF/CNPJ");
        
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCpfCnpj("98765432100");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void deveDeletarClienteComSucesso() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        assertThatCode(() -> clienteService.deletar(1L))
                .doesNotThrowAnyException();

        verify(clienteRepository).existsById(1L);
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cliente inexistente")
    void deveLancarExcecaoAoDeletarClienteInexistente() {
        when(clienteRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.deletar(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(clienteRepository).existsById(1L);
        verify(clienteRepository, never()).deleteById(any());
    }
}

