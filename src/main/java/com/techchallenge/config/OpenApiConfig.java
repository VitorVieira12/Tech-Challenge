package com.techchallenge.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI/Swagger para documentação interativa da API.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tech Challenge API",
                version = "1.0.0",
                description = """
                        API RESTful para gerenciamento de oficina mecânica.
                        
                        Funcionalidades:
                        - Gestão de Clientes
                        - Gestão de Veículos
                        - Gestão de Peças e Insumos
                        - Gestão de Serviços
                        - Criação e Acompanhamento de Ordens de Serviço
                        - Consulta Pública de Status para Clientes
                        - Monitoramento de Tempo Médio de Execução
                        
                        Autenticação:
                        - Use o endpoint /api/auth/login para obter um token JWT
                        - Credenciais padrão: username=admin, password=admin
                        - Clique em "Authorize" e cole o token recebido
                        """,
                contact = @Contact(
                        name = "Tech Challenge Team",
                        email = "support@techchallenge.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Docker Environment",
                        url = "http://localhost:8080"
                )
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication. Use /api/auth/login to obtain a token.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}



