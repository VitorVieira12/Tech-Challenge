package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.*;
import com.techchallenge.domain.model.*;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.repository.PecaInsumoRepository;
import com.techchallenge.domain.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para OrdemDeServicoController.
 * Testa o fluxo completo de criação, consulta e gestão de OSs.
 */
@AutoConfigureMockMvc
@DisplayName("OrdemDeServicoController - Testes de Integração")
class OrdemDeServicoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private PecaInsumoRepository pecaInsumoRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Obter token JWT
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "admin");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(loginResponse, LoginResponseDTO.class);
        jwtToken = response.getToken();

        // Criar dados de teste
        criarDadosDeTeste();
    }

    @Test
    @DisplayName("Deve criar OS com sucesso")
    void deveCriarOSComSucesso() throws Exception {
        // Arrange
        OrdemDeServicoInputDTO input = criarInputDTOValido();

        // Act & Assert
        mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.clienteNome").value("João Silva"))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1234"));
    }

    @Test
    @DisplayName("Deve bloquear criação de OS sem autenticação")
    void deveBloquerarCriacaoSemAutenticacao() throws Exception {
        // Arrange
        OrdemDeServicoInputDTO input = criarInputDTOValido();

        // Act & Assert
        mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir consulta pública com CPF correto")
    void devePermitirConsultaPublicaComCPFCorreto() throws Exception {
        // Arrange - Criar OS primeiro
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO createdOS = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);

        // Act & Assert - Consulta pública SEM token JWT
        mockMvc.perform(get("/api/ordens-servico/status/" + createdOS.getId())
                        .param("cpfCnpj", "12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOS.getId()))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1234"));
    }

    @Test
    @DisplayName("Deve bloquear consulta pública com CPF incorreto")
    void deveBloquerConsultaPublicaComCPFIncorreto() throws Exception {
        // Arrange - Criar OS primeiro
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO createdOS = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);

        // Act & Assert
        mockMvc.perform(get("/api/ordens-servico/status/" + createdOS.getId())
                        .param("cpfCnpj", "99999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar status da OS com sucesso")
    void deveAtualizarStatusComSucesso() throws Exception {
        // Arrange - Criar OS primeiro
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO createdOS = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);

        StatusUpdateDTO statusUpdate = new StatusUpdateDTO();
        statusUpdate.setNovoStatus(StatusOrdemServico.EM_EXECUCAO);
        statusUpdate.setObservacao("Cliente aprovou");

        // Act & Assert
        mockMvc.perform(patch("/api/ordens-servico/" + createdOS.getId() + "/status")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.dataInicioExecucao").exists());
    }

    // Helper methods
    private void criarDadosDeTeste() {
        // Criar cliente
        Cliente cliente = new Cliente();
        cliente.setNome("João Silva");
        cliente.setCpfCnpj("12345678901");
        cliente.setContato("joao@email.com");
        clienteRepository.save(cliente);

        // Criar serviço
        Servico servico = new Servico();
        servico.setDescricao("Troca de óleo");
        servico.setPreco(new BigDecimal("150.00"));
        servicoRepository.save(servico);

        // Criar peça
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Filtro de óleo");
        peca.setPreco(new BigDecimal("45.90"));
        peca.setQuantidadeEstoque(100);
        pecaInsumoRepository.save(peca);
    }

    private OrdemDeServicoInputDTO criarInputDTOValido() {
        OrdemDeServicoInputDTO input = new OrdemDeServicoInputDTO();
        input.setCpfCnpjCliente("12345678901");

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

        input.setObservacoes("Teste de integração");

        return input;
    }
}



