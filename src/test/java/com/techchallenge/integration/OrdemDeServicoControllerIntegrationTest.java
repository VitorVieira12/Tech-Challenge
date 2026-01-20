package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.*;
import com.techchallenge.domain.model.*;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.domain.repository.PecaInsumoRepository;
import com.techchallenge.domain.repository.ServicoRepository;
import com.techchallenge.domain.repository.VeiculoRepository;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private OrdemDeServicoRepository ordemDeServicoRepository;

    private Cliente clienteTeste;
    private Servico servicoTeste;
    private PecaInsumo pecaTeste;

    @BeforeEach
    void setUp() {
        ordemDeServicoRepository.deleteAll();
        veiculoRepository.deleteAll();
        pecaInsumoRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        
        criarDadosDeTeste();
    }

    @Test
    @DisplayName("Deve criar OS com sucesso")
    void deveCriarOSComSucesso() throws Exception {
        OrdemDeServicoInputDTO input = criarInputDTOValido();

        mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.clienteNome").value("João Silva"))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1234"));
    }

    @Test
    @org.junit.jupiter.api.Disabled("Security disabled in test environment via TestSecurityConfig")
    @DisplayName("Deve bloquear criação de OS sem autenticação")
    void deveBloquerarCriacaoSemAutenticacao() throws Exception {
        OrdemDeServicoInputDTO input = criarInputDTOValido();

        mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir consulta pública com CPF correto")
    void devePermitirConsultaPublicaComCPFCorreto() throws Exception {
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO createdOS = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);

        mockMvc.perform(get("/api/ordens-servico/status/" + createdOS.getId())
                        .param("cpfCnpj", "11144477735"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOS.getId()))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.veiculoPlaca").value("ABC1234"));
    }

    @Test
    @DisplayName("Deve bloquear consulta pública com CPF incorreto")
    void deveBloquerConsultaPublicaComCPFIncorreto() throws Exception {
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO createdOS = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);

        mockMvc.perform(get("/api/ordens-servico/status/" + createdOS.getId())
                        .param("cpfCnpj", "12345678909"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar status da OS com sucesso")
    void deveAtualizarStatusComSucesso() throws Exception {
        OrdemDeServicoInputDTO input = criarInputDTOValido();
        MvcResult createResult = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        OrdemDeServicoResponseDTO createdOS = objectMapper.readValue(createResponse, OrdemDeServicoResponseDTO.class);

        StatusUpdateDTO statusUpdate = new StatusUpdateDTO();
        statusUpdate.setNovoStatus(StatusOrdemServico.EM_EXECUCAO);
        statusUpdate.setObservacao("Cliente aprovou");

        mockMvc.perform(patch("/api/ordens-servico/" + createdOS.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.dataInicioExecucao").exists());
    }

    private void criarDadosDeTeste() {
        Cliente cliente = new Cliente();
        cliente.setNome("João Silva");
        cliente.setCpfCnpj(new CpfCnpj("11144477735"));
        cliente.setContato(new Contato("joao@email.com"));
        clienteTeste = clienteRepository.save(cliente);

        Servico servico = new Servico();
        servico.setDescricao("Troca de óleo");
        servico.setPreco(new ValorMonetario(new BigDecimal("150.00")));
        servicoTeste = servicoRepository.save(servico);

        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Filtro de óleo");
        peca.setPreco(new ValorMonetario(new BigDecimal("45.90")));
        peca.setQuantidadeEstoque(100);
        pecaTeste = pecaInsumoRepository.save(peca);
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
        servicoDTO.setServicoId(servicoTeste.getId());
        servicoDTO.setQuantidade(1);
        input.setServicos(List.of(servicoDTO));

        ItemPecaDTO pecaDTO = new ItemPecaDTO();
        pecaDTO.setPecaInsumoId(pecaTeste.getId());
        pecaDTO.setQuantidade(2);
        input.setPecas(List.of(pecaDTO));

        input.setObservacoes("Teste de integração");

        return input;
    }
}
