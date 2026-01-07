package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.*;
import com.techchallenge.domain.model.*;
import com.techchallenge.domain.repository.*;
import com.techchallenge.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled("Requer Docker/Testcontainers - Execute com: mvn verify -P integration")
@AutoConfigureMockMvc
@DisplayName("Ordem de Serviço - Fluxo Completo End-to-End - Teste de Integração")
class OrdemDeServicoFluxoCompletoIntegrationTest extends BaseIntegrationTest {

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

    @Autowired
    private OrdemDeServicoRepository ordemDeServicoRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Limpar base de dados
        ordemDeServicoRepository.deleteAll();
        pecaInsumoRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();

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
    @DisplayName("Fluxo Completo: Abertura de OS → Aprovação → Listagem Ordenada")
    void deveExecutarFluxoCompletoDeOS() throws Exception {
        // ========== PASSO 1: CRIAR ORDEM DE SERVIÇO ==========
        OrdemDeServicoInputDTO osInput = criarInputOSCompleta();

        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(osInput)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.valorTotalOrcamento").value(195.90))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO osCreated = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);
        Long osId = osCreated.getId();

        assertThat(osId).isNotNull();
        assertThat(osCreated.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);

        // ========== PASSO 2: APROVAR ORÇAMENTO ==========
        AprovacaoOrcamentoInputDTO aprovacaoInput = new AprovacaoOrcamentoInputDTO(true, null);

        mockMvc.perform(post("/api/ordens-servico/" + osId + "/aprovar-orcamento")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aprovacaoInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.dataInicioExecucao").exists())
                .andExpect(jsonPath("$.observacoes").value(org.hamcrest.Matchers.containsString("APROVADO")));

        // ========== PASSO 3: LISTAR OS EM ANDAMENTO COM ORDENAÇÃO ==========
        mockMvc.perform(get("/api/ordens-servico/em-andamento")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(osId))
                .andExpect(jsonPath("$[0].status").value("EM_EXECUCAO"));

        // ========== PASSO 4: CONSULTAR STATUS PÚBLICO (SEM AUTENTICAÇÃO) ==========
        mockMvc.perform(get("/api/ordens-servico/status/" + osId)
                        .param("cpfCnpj", "11144477735"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(osId))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1234"));
    }

    @Test
    @DisplayName("Deve ordenar OS por prioridade de status")
    void deveOrdenarOsPorPrioridadeDeStatus() throws Exception {
        // Criar múltiplas OS com diferentes status
        OrdemDeServicoInputDTO os1 = criarInputOSCompleta();
        OrdemDeServicoInputDTO os2 = criarInputOSCompleta();
        OrdemDeServicoInputDTO os3 = criarInputOSCompleta();

        // Criar OS 1 (RECEBIDA)
        MvcResult result1 = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(os1)))
                .andExpect(status().isCreated())
                .andReturn();
        Long osId1 = objectMapper.readValue(result1.getResponse().getContentAsString(), OrdemDeServicoResponseDTO.class).getId();

        // Criar OS 2 (AGUARDANDO_APROVACAO - status padrão após criação)
        MvcResult result2 = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(os2)))
                .andExpect(status().isCreated())
                .andReturn();
        Long osId2 = objectMapper.readValue(result2.getResponse().getContentAsString(), OrdemDeServicoResponseDTO.class).getId();

        // Criar OS 3 e aprovar (EM_EXECUCAO)
        MvcResult result3 = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(os3)))
                .andExpect(status().isCreated())
                .andReturn();
        Long osId3 = objectMapper.readValue(result3.getResponse().getContentAsString(), OrdemDeServicoResponseDTO.class).getId();

        // Aprovar OS 3
        AprovacaoOrcamentoInputDTO aprovacao = new AprovacaoOrcamentoInputDTO(true, null);
        mockMvc.perform(post("/api/ordens-servico/" + osId3 + "/aprovar-orcamento")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aprovacao)))
                .andExpect(status().isOk());

        // Recusar OS 1 (volta para RECEBIDA)
        AprovacaoOrcamentoInputDTO recusa = new AprovacaoOrcamentoInputDTO(false, "Valor alto");
        mockMvc.perform(post("/api/ordens-servico/" + osId1 + "/aprovar-orcamento")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recusa)))
                .andExpect(status().isOk());

        // Listar em andamento - deve vir ordenado: EM_EXECUCAO > AGUARDANDO_APROVACAO > RECEBIDA
        mockMvc.perform(get("/api/ordens-servico/em-andamento")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$[1].status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$[2].status").value("RECEBIDA"));
    }

    @Test
    @DisplayName("Deve rejeitar aprovação de OS que não está aguardando aprovação")
    void deveRejeitarAprovacaoDeOsQueNaoEstaAguardandoAprovacao() throws Exception {
        // Criar e aprovar OS
        OrdemDeServicoInputDTO osInput = criarInputOSCompleta();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(osInput)))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long osId = objectMapper.readValue(createResult.getResponse().getContentAsString(), OrdemDeServicoResponseDTO.class).getId();

        // Aprovar primeira vez
        AprovacaoOrcamentoInputDTO aprovacao = new AprovacaoOrcamentoInputDTO(true, null);
        mockMvc.perform(post("/api/ordens-servico/" + osId + "/aprovar-orcamento")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aprovacao)))
                .andExpect(status().isOk());

        // Tentar aprovar novamente - deve falhar
        mockMvc.perform(post("/api/ordens-servico/" + osId + "/aprovar-orcamento")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aprovacao)))
                .andExpect(status().is5xxServerError());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void criarDadosDeTeste() {
        // Criar cliente
        Cliente cliente = new Cliente();
        cliente.setNome("João Silva Teste");
        cliente.setCpfCnpj(new CpfCnpj("11144477735"));
        cliente.setContato(new Contato("joao@teste.com"));
        clienteRepository.save(cliente);

        // Criar serviço
        Servico servico = new Servico();
        servico.setDescricao("Troca de óleo");
        servico.setPreco(new ValorMonetario(new BigDecimal("150.00")));
        servicoRepository.save(servico);

        // Criar peça
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Filtro de óleo");
        peca.setPreco(new ValorMonetario(new BigDecimal("45.90")));
        peca.setQuantidadeEstoque(100);
        pecaInsumoRepository.save(peca);
    }

    private OrdemDeServicoInputDTO criarInputOSCompleta() {
        OrdemDeServicoInputDTO input = new OrdemDeServicoInputDTO();
        input.setCpfCnpjCliente("11144477735");

        VeiculoInputDTO veiculo = new VeiculoInputDTO();
        veiculo.setPlaca("ABC1234");
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(2020);
        input.setVeiculo(veiculo);

        ItemServicoDTO servico = new ItemServicoDTO();
        servico.setServicoId(1L);
        servico.setQuantidade(1);
        input.setServicos(Arrays.asList(servico));

        ItemPecaDTO peca = new ItemPecaDTO();
        peca.setPecaInsumoId(1L);
        peca.setQuantidade(1);
        input.setPecas(Arrays.asList(peca));

        input.setObservacoes("Teste de integração");

        return input;
    }
}

