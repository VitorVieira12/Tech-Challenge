package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import com.techchallenge.domain.dto.VeiculoDTO;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.Veiculo;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.repository.VeiculoRepository;
import com.techchallenge.domain.valueobject.AnoVeiculo;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
import com.techchallenge.domain.valueobject.Placa;
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
import org.junit.jupiter.api.Disabled;

@org.junit.jupiter.api.Disabled("TODO: Remove JWT token references")
@AutoConfigureMockMvc
@DisplayName("VeiculoController - Testes de Integração")
class VeiculoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private String jwtToken;
    private Cliente cliente;

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

        cliente = new Cliente();
        cliente.setNome("João Silva");
        cliente.setCpfCnpj(new CpfCnpj("11144477735"));
        cliente.setContato(new Contato("joao@email.com"));
        cliente = clienteRepository.save(cliente);
    }

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    void deveCriarVeiculoComSucesso() throws Exception {
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("ABC1234");
        veiculoDTO.setMarca("Toyota");
        veiculoDTO.setModelo("Corolla");
        veiculoDTO.setAno(2020);
        veiculoDTO.setClienteId(cliente.getId());

        mockMvc.perform(post("/api/veiculos")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.placa").value("ABC1234"))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"))
                .andExpect(jsonPath("$.ano").value(2020));
    }

    @Test
    @DisplayName("Deve converter placa para maiúsculo ao criar")
    void deveConverterPlacaParaMaiusculoAoCriar() throws Exception {
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("xyz5678");
        veiculoDTO.setMarca("Honda");
        veiculoDTO.setModelo("Civic");
        veiculoDTO.setAno(2021);
        veiculoDTO.setClienteId(cliente.getId());

        mockMvc.perform(post("/api/veiculos")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("XYZ5678"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar veículo com placa duplicada")
    void deveRetornarErroAoCriarVeiculoComPlacaDuplicada() throws Exception {
        Veiculo veiculoExistente = new Veiculo();
        veiculoExistente.setPlaca(new Placa("DEF9876"));
        veiculoExistente.setMarca("Fiat");
        veiculoExistente.setModelo("Uno");
        veiculoExistente.setAno(new AnoVeiculo(2019));
        veiculoExistente.setCliente(cliente);
        veiculoRepository.save(veiculoExistente);

        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("DEF9876");
        veiculoDTO.setMarca("Outro");
        veiculoDTO.setModelo("Modelo");
        veiculoDTO.setAno(2020);
        veiculoDTO.setClienteId(cliente.getId());

        mockMvc.perform(post("/api/veiculos")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar erro ao criar veículo com cliente inexistente")
    void deveRetornarErroAoCriarVeiculoComClienteInexistente() throws Exception {
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("GHI4567");
        veiculoDTO.setMarca("Chevrolet");
        veiculoDTO.setModelo("Onix");
        veiculoDTO.setAno(2022);
        veiculoDTO.setClienteId(99999L);

        mockMvc.perform(post("/api/veiculos")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve buscar veículo por ID com sucesso")
    void deveBuscarVeiculoPorIdComSucesso() throws Exception {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(new Placa("JKL1234"));
        veiculo.setMarca("Volkswagen");
        veiculo.setModelo("Gol");
        veiculo.setAno(new AnoVeiculo(2020));
        veiculo.setCliente(cliente);
        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);

        mockMvc.perform(get("/api/veiculos/" + veiculoSalvo.getId())
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(veiculoSalvo.getId()))
                .andExpect(jsonPath("$.placa").value("JKL1234"))
                .andExpect(jsonPath("$.marca").value("Volkswagen"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar veículo inexistente")
    void deveRetornar404AoBuscarVeiculoInexistente() throws Exception {
        mockMvc.perform(get("/api/veiculos/99999")
                        )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void deveListarTodosOsVeiculos() throws Exception {
        Veiculo veiculo1 = new Veiculo();
        veiculo1.setPlaca(new Placa("MNO1234"));
        veiculo1.setMarca("Ford");
        veiculo1.setModelo("Ka");
        veiculo1.setAno(new AnoVeiculo(2021));
        veiculo1.setCliente(cliente);
        veiculoRepository.save(veiculo1);

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setPlaca(new Placa("PQR5678"));
        veiculo2.setMarca("Renault");
        veiculo2.setModelo("Sandero");
        veiculo2.setAno(new AnoVeiculo(2022));
        veiculo2.setCliente(cliente);
        veiculoRepository.save(veiculo2);

        mockMvc.perform(get("/api/veiculos")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Deve listar veículos por cliente")
    void deveListarVeiculosPorCliente() throws Exception {
        Veiculo veiculo1 = new Veiculo();
        veiculo1.setPlaca(new Placa("STU1234"));
        veiculo1.setMarca("Hyundai");
        veiculo1.setModelo("HB20");
        veiculo1.setAno(new AnoVeiculo(2020));
        veiculo1.setCliente(cliente);
        veiculoRepository.save(veiculo1);

        Cliente outroCliente = new Cliente();
        outroCliente.setNome("Maria Santos");
        outroCliente.setCpfCnpj(new CpfCnpj("52998224725"));
        outroCliente.setContato(new Contato("maria@email.com"));
        outroCliente = clienteRepository.save(outroCliente);

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setPlaca(new Placa("VWX5678"));
        veiculo2.setMarca("Nissan");
        veiculo2.setModelo("Versa");
        veiculo2.setAno(new AnoVeiculo(2021));
        veiculo2.setCliente(outroCliente);
        veiculoRepository.save(veiculo2);

        mockMvc.perform(get("/api/veiculos")
                        .param("clienteId", cliente.getId().toString())
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placa").value("STU1234"));
    }

    @Test
    @DisplayName("Deve atualizar veículo com sucesso")
    void deveAtualizarVeiculoComSucesso() throws Exception {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(new Placa("YZA1234"));
        veiculo.setMarca("Marca Original");
        veiculo.setModelo("Modelo Original");
        veiculo.setAno(new AnoVeiculo(2020));
        veiculo.setCliente(cliente);
        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);

        VeiculoDTO atualizacaoDTO = new VeiculoDTO();
        atualizacaoDTO.setPlaca("YZA1234");
        atualizacaoDTO.setMarca("Marca Atualizada");
        atualizacaoDTO.setModelo("Modelo Atualizado");
        atualizacaoDTO.setAno(2021);
        atualizacaoDTO.setClienteId(cliente.getId());

        mockMvc.perform(put("/api/veiculos/" + veiculoSalvo.getId())
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Marca Atualizada"))
                .andExpect(jsonPath("$.modelo").value("Modelo Atualizado"))
                .andExpect(jsonPath("$.ano").value(2021));
    }

    @Test
    @DisplayName("Deve deletar veículo com sucesso")
    void deveDeletarVeiculoComSucesso() throws Exception {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(new Placa("BCD1234"));
        veiculo.setMarca("Marca Deletar");
        veiculo.setModelo("Modelo Deletar");
        veiculo.setAno(new AnoVeiculo(2020));
        veiculo.setCliente(cliente);
        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);

        mockMvc.perform(delete("/api/veiculos/" + veiculoSalvo.getId())
                        )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao deletar veículo inexistente")
    void deveRetornar404AoDeletarVeiculoInexistente() throws Exception {
        mockMvc.perform(delete("/api/veiculos/99999")
                        )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve bloquear acesso sem autenticação")
    void deveBloquerAcessoSemAutenticacao() throws Exception {
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("EFG1234");
        veiculoDTO.setMarca("Teste");
        veiculoDTO.setModelo("Teste");
        veiculoDTO.setAno(2020);
        veiculoDTO.setClienteId(cliente.getId());

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDTO)))
                .andExpect(status().isForbidden());
    }
}

