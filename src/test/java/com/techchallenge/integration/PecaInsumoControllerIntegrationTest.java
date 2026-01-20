package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import com.techchallenge.domain.dto.PecaInsumoDTO;
import com.techchallenge.domain.model.PecaInsumo;
import com.techchallenge.domain.repository.*;
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
@AutoConfigureMockMvc
@DisplayName("PecaInsumoController - Testes de Integração")
class PecaInsumoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PecaInsumoRepository pecaInsumoRepository;

    @Autowired
    private OrdemDeServicoRepository ordemDeServicoRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @BeforeEach
    void setUp() {
        ordemDeServicoRepository.deleteAll();
        veiculoRepository.deleteAll();
        pecaInsumoRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar peça/insumo com sucesso")
    void deveCriarPecaInsumoComSucesso() throws Exception {
        PecaInsumoDTO pecaDTO = new PecaInsumoDTO();
        pecaDTO.setNome("Filtro de óleo");
        pecaDTO.setDescricao("Filtro de óleo para motor");
        pecaDTO.setPreco(new BigDecimal("45.90"));
        pecaDTO.setQuantidadeEstoque(100);

        mockMvc.perform(post("/api/pecas-insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pecaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Filtro de óleo"))
                .andExpect(jsonPath("$.descricao").value("Filtro de óleo para motor"))
                .andExpect(jsonPath("$.preco").value(45.90))
                .andExpect(jsonPath("$.quantidadeEstoque").value(100));
    }

    @Test
    @DisplayName("Deve buscar peça/insumo por ID com sucesso")
    void deveBuscarPecaInsumoPorIdComSucesso() throws Exception {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Pastilha de freio");
        peca.setDescricao("Pastilha de freio dianteira");
        peca.setPreco(new ValorMonetario(new BigDecimal("120.00")));
        peca.setQuantidadeEstoque(50);
        PecaInsumo pecaSalva = pecaInsumoRepository.save(peca);

        mockMvc.perform(get("/api/pecas-insumos/" + pecaSalva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pecaSalva.getId()))
                .andExpect(jsonPath("$.nome").value("Pastilha de freio"))
                .andExpect(jsonPath("$.preco").value(120.00));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar peça/insumo inexistente")
    void deveRetornar404AoBuscarPecaInsumoInexistente() throws Exception {
        mockMvc.perform(get("/api/pecas-insumos/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todas as peças/insumos")
    void deveListarTodasAsPecasInsumos() throws Exception {
        PecaInsumo peca1 = new PecaInsumo();
        peca1.setNome("Óleo de motor");
        peca1.setDescricao("Óleo sintético 5W30");
        peca1.setPreco(new ValorMonetario(new BigDecimal("35.00")));
        peca1.setQuantidadeEstoque(200);
        pecaInsumoRepository.save(peca1);

        PecaInsumo peca2 = new PecaInsumo();
        peca2.setNome("Vela de ignição");
        peca2.setDescricao("Vela de ignição padrão");
        peca2.setPreco(new ValorMonetario(new BigDecimal("25.00")));
        peca2.setQuantidadeEstoque(150);
        pecaInsumoRepository.save(peca2);

        mockMvc.perform(get("/api/pecas-insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Deve atualizar peça/insumo com sucesso")
    void deveAtualizarPecaInsumoComSucesso() throws Exception {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Filtro Original");
        peca.setDescricao("Descrição Original");
        peca.setPreco(new ValorMonetario(new BigDecimal("40.00")));
        peca.setQuantidadeEstoque(80);
        PecaInsumo pecaSalva = pecaInsumoRepository.save(peca);

        PecaInsumoDTO atualizacaoDTO = new PecaInsumoDTO();
        atualizacaoDTO.setNome("Filtro Atualizado");
        atualizacaoDTO.setDescricao("Descrição Atualizada");
        atualizacaoDTO.setPreco(new BigDecimal("55.00"));
        atualizacaoDTO.setQuantidadeEstoque(120);

        mockMvc.perform(put("/api/pecas-insumos/" + pecaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Filtro Atualizado"))
                .andExpect(jsonPath("$.descricao").value("Descrição Atualizada"))
                .andExpect(jsonPath("$.preco").value(55.00))
                .andExpect(jsonPath("$.quantidadeEstoque").value(120));
    }

    @Test
    @DisplayName("Deve atualizar estoque com ajuste positivo")
    void deveAtualizarEstoqueComAjustePositivo() throws Exception {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Fluido de freio");
        peca.setDescricao("Fluido DOT 4");
        peca.setPreco(new ValorMonetario(new BigDecimal("20.00")));
        peca.setQuantidadeEstoque(100);
        PecaInsumo pecaSalva = pecaInsumoRepository.save(peca);

        mockMvc.perform(patch("/api/pecas-insumos/" + pecaSalva.getId() + "/estoque")
                        .param("quantidadeAjuste", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(150));
    }

    @Test
    @DisplayName("Deve atualizar estoque com ajuste negativo")
    void deveAtualizarEstoqueComAjusteNegativo() throws Exception {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Pneu");
        peca.setDescricao("Pneu 175/70 R14");
        peca.setPreco(new ValorMonetario(new BigDecimal("250.00")));
        peca.setQuantidadeEstoque(100);
        PecaInsumo pecaSalva = pecaInsumoRepository.save(peca);

        mockMvc.perform(patch("/api/pecas-insumos/" + pecaSalva.getId() + "/estoque")
                        .param("quantidadeAjuste", "-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(70));
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar estoque para valor negativo")
    void deveRetornarErroAoAtualizarEstoqueParaValorNegativo() throws Exception {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Bateria");
        peca.setDescricao("Bateria 60Ah");
        peca.setPreco(new ValorMonetario(new BigDecimal("350.00")));
        peca.setQuantidadeEstoque(10);
        PecaInsumo pecaSalva = pecaInsumoRepository.save(peca);

        mockMvc.perform(patch("/api/pecas-insumos/" + pecaSalva.getId() + "/estoque")
                        .param("quantidadeAjuste", "-20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve deletar peça/insumo com sucesso")
    void deveDeletarPecaInsumoComSucesso() throws Exception {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome("Peça para Deletar");
        peca.setDescricao("Descrição");
        peca.setPreco(new ValorMonetario(new BigDecimal("10.00")));
        peca.setQuantidadeEstoque(5);
        PecaInsumo pecaSalva = pecaInsumoRepository.save(peca);

        mockMvc.perform(delete("/api/pecas-insumos/" + pecaSalva.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao deletar peça/insumo inexistente")
    void deveRetornar404AoDeletarPecaInsumoInexistente() throws Exception {
        mockMvc.perform(delete("/api/pecas-insumos/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Security disabled in test environment via TestSecurityConfig")
    @DisplayName("Deve bloquear acesso sem autenticação")
    void deveBloquerAcessoSemAutenticacao() throws Exception {
        PecaInsumoDTO pecaDTO = new PecaInsumoDTO();
        pecaDTO.setNome("Teste");
        pecaDTO.setDescricao("Teste");
        pecaDTO.setPreco(new BigDecimal("10.00"));
        pecaDTO.setQuantidadeEstoque(10);

        mockMvc.perform(post("/api/pecas-insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pecaDTO)))
                .andExpect(status().isForbidden());
    }
}

