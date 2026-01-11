package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.ClienteDTO;
import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("ClienteController - Testes de Integração")
class ClienteControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "admin");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(loginResponse, LoginResponseDTO.class);
        jwtToken = response.getToken();
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void deveCriarClienteComSucesso() throws Exception {
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Carlos Silva");
        clienteDTO.setCpfCnpj("19119111101");
        clienteDTO.setContato("carlos@email.com");

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Carlos Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("19119111101"))
                .andExpect(jsonPath("$.contato").value("carlos@email.com"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar cliente com CPF duplicado")
    void deveRetornarErroAoCriarClienteComCpfDuplicado() throws Exception {
        Cliente clienteExistente = new Cliente();
        clienteExistente.setNome("João Silva");
        clienteExistente.setCpfCnpj(new CpfCnpj("19119111101"));
        clienteExistente.setContato(new Contato("joao@email.com"));
        clienteRepository.save(clienteExistente);

        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Outro Cliente");
        clienteDTO.setCpfCnpj("19119111101");
        clienteDTO.setContato("outro@email.com");

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Maria Santos");
        cliente.setCpfCnpj(new CpfCnpj("68319775072"));
        cliente.setContato(new Contato("maria@email.com"));
        Cliente clienteSalvo = clienteRepository.save(cliente);

        mockMvc.perform(get("/api/clientes/" + clienteSalvo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteSalvo.getId()))
                .andExpect(jsonPath("$.nome").value("Maria Santos"))
                .andExpect(jsonPath("$.cpfCnpj").value("68319775072"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar cliente inexistente")
    void deveRetornar404AoBuscarClienteInexistente() throws Exception {
        mockMvc.perform(get("/api/clientes/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosOsClientes() throws Exception {
        Cliente cliente1 = new Cliente();
        cliente1.setNome("Cliente 1");
        cliente1.setCpfCnpj(new CpfCnpj("03877708070"));
        cliente1.setContato(new Contato("cliente1@email.com"));
        clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNome("Cliente 2");
        cliente2.setCpfCnpj(new CpfCnpj("86698489023"));
        cliente2.setContato(new Contato("cliente2@email.com"));
        clienteRepository.save(cliente2);

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Original");
        cliente.setCpfCnpj(new CpfCnpj("58995335001"));
        cliente.setContato(new Contato("original@email.com"));
        Cliente clienteSalvo = clienteRepository.save(cliente);

        ClienteDTO atualizacaoDTO = new ClienteDTO();
        atualizacaoDTO.setNome("Cliente Atualizado");
        atualizacaoDTO.setCpfCnpj("58995335001");
        atualizacaoDTO.setContato("atualizado@email.com");

        mockMvc.perform(put("/api/clientes/" + clienteSalvo.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cliente Atualizado"))
                .andExpect(jsonPath("$.contato").value("atualizado@email.com"));
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void deveDeletarClienteComSucesso() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente para Deletar");
        cliente.setCpfCnpj(new CpfCnpj("80682199033"));
        cliente.setContato(new Contato("deletar@email.com"));
        Cliente clienteSalvo = clienteRepository.save(cliente);

        mockMvc.perform(delete("/api/clientes/" + clienteSalvo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao deletar cliente inexistente")
    void deveRetornar404AoDeletarClienteInexistente() throws Exception {
        mockMvc.perform(delete("/api/clientes/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve bloquear acesso sem autenticação")
    void deveBloquerAcessoSemAutenticacao() throws Exception {
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Cliente Teste");
        clienteDTO.setCpfCnpj("12345678909");
        clienteDTO.setContato("teste@email.com");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isForbidden());
    }
}

