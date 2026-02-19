package com.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class AuthHandlerTest {

    private AuthHandler handler;
    
    @Mock
    private Context context;

    @BeforeEach
    void setUp() {
        handler = new AuthHandler();
    }

    @Test
    void testValidarCPF_Valid() {
        // CPF válido: 123.456.789-09
        assertTrue(handler.validarCPF("12345678909"));
    }

    @Test
    void testValidarCPF_Invalid() {
        // CPF inválido
        assertFalse(handler.validarCPF("12345678900"));
    }

    @Test
    void testValidarCPF_AllSameDigits() {
        // CPF com todos dígitos iguais (inválido)
        assertFalse(handler.validarCPF("11111111111"));
    }

    @Test
    void testValidarCPF_InvalidLength() {
        // CPF com tamanho incorreto
        assertFalse(handler.validarCPF("123456789"));
    }

    @Test
    void testValidarCPF_Null() {
        // CPF nulo
        assertFalse(handler.validarCPF(null));
    }

    @Test
    void testValidarCPF_WithMask() {
        // CPF com máscara
        assertFalse(handler.validarCPF("123.456.789-09"));
    }
}

