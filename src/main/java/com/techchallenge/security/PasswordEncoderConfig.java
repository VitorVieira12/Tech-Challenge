package com.techchallenge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração do encoder de senhas.
 * Separado do SecurityConfig para evitar dependências circulares.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Encoder de senhas usando BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

