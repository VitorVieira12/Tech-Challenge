package com.techchallenge.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para AuthController.
 * Testa o fluxo completo de autenticação JWT.
 */
@AutoConfigureMockMvc
@DisplayName("AuthController - Testes de Integração")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve autenticar com sucesso com credenciais válidas")
    void deveAutenticarComSucessoComCredenciaisValidas() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "admin");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        // Verifica se o token foi gerado
        String responseBody = result.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(responseBody, LoginResponseDTO.class);
        
        assertThat(response.getToken()).isNotEmpty();
        assertThat(response.getToken()).hasSizeGreaterThan(100); // Token JWT é longo
    }

    @Test
    @DisplayName("Deve retornar 401 com credenciais inválidas")
    void deveRetornar401ComCredenciaisInvalidas() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "senha_errada");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 quando username está vazio")
    void deveRetornar400QuandoUsernameVazio() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("", "admin");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando password está vazio")
    void deveRetornar400QuandoPasswordVazio() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}



