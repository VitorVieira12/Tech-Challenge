package com.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.sql.*;
import java.util.*;

/**
 * AWS Lambda Handler para autenticação por CPF
 * 
 * Fluxo:
 * 1. Recebe CPF via POST
 * 2. Valida CPF (algoritmo)
 * 3. Consulta cliente no RDS PostgreSQL
 * 4. Gera JWT se cliente existir e estiver ativo
 * 5. Retorna token ou erro
 */
public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final String DB_URL = "jdbc:postgresql://" + System.getenv("DB_HOST") + ":" + 
                                         System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME");
    private static final String DB_USER = System.getenv("DB_USERNAME");
    private static final String DB_PASS = System.getenv("DB_PASSWORD");
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    private static final long JWT_EXPIRATION = Long.parseLong(System.getenv().getOrDefault("JWT_EXPIRATION", "86400000"));
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Received request: " + input.getBody());
        
        try {
            // 1. Parse request body
            Map<String, String> body = objectMapper.readValue(input.getBody(), Map.class);
            String cpf = body.get("cpf");

            if (cpf == null || cpf.isBlank()) {
                return errorResponse(400, "CPF é obrigatório");
            }

            // 2. Validate CPF
            String cpfLimpo = cpf.replaceAll("[^0-9]", "");
            if (!validarCPF(cpfLimpo)) {
                context.getLogger().log("CPF inválido: " + cpf);
                return errorResponse(400, "CPF inválido");
            }

            // 3. Query database for cliente
            Cliente cliente = buscarCliente(cpfLimpo);
            
            if (cliente == null) {
                context.getLogger().log("Cliente não encontrado: " + cpf);
                return errorResponse(401, "Cliente não encontrado");
            }
            
            if (!cliente.isAtivo()) {
                context.getLogger().log("Cliente inativo: " + cpf);
                return errorResponse(401, "Cliente inativo");
            }

            // 4. Generate JWT token
            String token = gerarJWT(cliente);
            context.getLogger().log("Token gerado com sucesso para cliente: " + cliente.getId());

            // 5. Build response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("expiresIn", JWT_EXPIRATION);
            response.put("clienteId", cliente.getId());
            response.put("clienteNome", cliente.getNome());

            return successResponse(200, objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            e.printStackTrace();
            return errorResponse(500, "Erro interno do servidor");
        }
    }

    /**
     * Busca cliente no banco de dados por CPF
     */
    private Cliente buscarCliente(String cpf) {
        String sql = "SELECT id, nome, cpf_cnpj, contato, ativo FROM clientes WHERE cpf_cnpj = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getLong("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setCpfCnpj(rs.getString("cpf_cnpj"));
                cliente.setContato(rs.getString("contato"));
                cliente.setAtivo(rs.getBoolean("ativo"));
                return cliente;
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gera token JWT para o cliente
     */
    private String gerarJWT(Cliente cliente) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(cliente.getCpfCnpj())
                .claim("clienteId", cliente.getId())
                .claim("nome", cliente.getNome())
                .claim("email", cliente.getContato())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setIssuer("tech-challenge-lambda")
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET.getBytes())
                .compact();
    }

    /**
     * Valida CPF usando algoritmo oficial
     */
    private boolean validarCPF(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        
        // CPFs conhecidos como inválidos
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int[] digitos = cpf.chars().map(c -> c - '0').toArray();
        
        // Calcula primeiro dígito verificador
        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += digitos[i] * (10 - i);
        }
        int dig1 = 11 - (soma1 % 11);
        if (dig1 > 9) dig1 = 0;

        // Calcula segundo dígito verificador
        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += digitos[i] * (11 - i);
        }
        int dig2 = 11 - (soma2 % 11);
        if (dig2 > 9) dig2 = 0;

        return digitos[9] == dig1 && digitos[10] == dig2;
    }

    /**
     * Cria resposta de sucesso
     */
    private APIGatewayProxyResponseEvent successResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        response.setHeaders(headers);
        
        return response;
    }

    /**
     * Cria resposta de erro
     */
    private APIGatewayProxyResponseEvent errorResponse(int statusCode, String message) {
        String body = String.format("{\"message\":\"%s\",\"statusCode\":%d}", message, statusCode);
        return successResponse(statusCode, body);
    }
}

