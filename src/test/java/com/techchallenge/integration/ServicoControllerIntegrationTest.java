package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import com.techchallenge.domain.dto.ServicoDTO;
import com.techchallenge.domain.model.Servico;
import com.techchallenge.domain.repository.ServicoRepository;
import com.techchallenge.domain.valueobject.ValorMonetario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Disabled;

@Disabled("Requer Docker/Testcontainers - Execute com: mvn verify -P integration")
@AutoConfigureMockMvc
@DisplayName("ServicoController - Testes de Integração")
class ServicoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicoRepository servicoRepository;

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
    @DisplayName("Deve criar serviço com sucesso")
    void deveCriarServicoComSucesso() throws Exception {
        ServicoDTO servicoDTO = new ServicoDTO();
        servicoDTO.setDescricao("Troca de óleo");
        servicoDTO.setPreco(new BigDecimal("150.00"));

        mockMvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(servicoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.preco").value(150.00));
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() throws Exception {
        Servico servico = new Servico();
        servico.setDescricao("Alinhamento e balanceamento");
        servico.setPreco(new ValorMonetario(new BigDecimal("80.00")));
        Servico servicoSalvo = servicoRepository.save(servico);

        mockMvc.perform(get("/api/servicos/" + servicoSalvo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(servicoSalvo.getId()))
                .andExpect(jsonPath("$.descricao").value("Alinhamento e balanceamento"))
                .andExpect(jsonPath("$.preco").value(80.00));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar serviço inexistente")
    void deveRetornar404AoBuscarServicoInexistente() throws Exception {
        mockMvc.perform(get("/api/servicos/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void deveListarTodosOsServicos() throws Exception {
        Servico servico1 = new Servico();
        servico1.setDescricao("Revisão completa");
        servico1.setPreco(new ValorMonetario(new BigDecimal("500.00")));
        servicoRepository.save(servico1);

        Servico servico2 = new Servico();
        servico2.setDescricao("Troca de pastilhas de freio");
        servico2.setPreco(new ValorMonetario(new BigDecimal("200.00")));
        servicoRepository.save(servico2);

        mockMvc.perform(get("/api/servicos")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Deve atualizar serviço com sucesso")
    void deveAtualizarServicoComSucesso() throws Exception {
        Servico servico = new Servico();
        servico.setDescricao("Serviço Original");
        servico.setPreco(new ValorMonetario(new BigDecimal("100.00")));
        Servico servicoSalvo = servicoRepository.save(servico);

        ServicoDTO atualizacaoDTO = new ServicoDTO();
        atualizacaoDTO.setDescricao("Serviço Atualizado");
        atualizacaoDTO.setPreco(new BigDecimal("150.00"));

        mockMvc.perform(put("/api/servicos/" + servicoSalvo.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Serviço Atualizado"))
                .andExpect(jsonPath("$.preco").value(150.00));
    }

    @Test
    @DisplayName("Deve retornar 404 ao atualizar serviço inexistente")
    void deveRetornar404AoAtualizarServicoInexistente() throws Exception {
        ServicoDTO servicoDTO = new ServicoDTO();
        servicoDTO.setDescricao("Teste");
        servicoDTO.setPreco(new BigDecimal("100.00"));

        mockMvc.perform(put("/api/servicos/99999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(servicoDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar serviço com sucesso")
    void deveDeletarServicoComSucesso() throws Exception {
        Servico servico = new Servico();
        servico.setDescricao("Serviço para Deletar");
        servico.setPreco(new ValorMonetario(new BigDecimal("50.00")));
        Servico servicoSalvo = servicoRepository.save(servico);

        mockMvc.perform(delete("/api/servicos/" + servicoSalvo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao deletar serviço inexistente")
    void deveRetornar404AoDeletarServicoInexistente() throws Exception {
        mockMvc.perform(delete("/api/servicos/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve bloquear acesso sem autenticação")
    void deveBloquerAcessoSemAutenticacao() throws Exception {
        ServicoDTO servicoDTO = new ServicoDTO();
        servicoDTO.setDescricao("Teste");
        servicoDTO.setPreco(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(servicoDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios")
    void deveValidarCamposObrigatorios() throws Exception {
        ServicoDTO servicoDTO = new ServicoDTO();

        mockMvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(servicoDTO)))
                .andExpect(status().isBadRequest());
    }
}

