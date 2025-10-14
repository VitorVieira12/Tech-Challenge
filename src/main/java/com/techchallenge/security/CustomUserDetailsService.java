package com.techchallenge.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço personalizado para carregar detalhes de usuários.
 * 
 * Para o MVP, usa usuários em memória.
 * Em produção, este serviço deveria buscar usuários do banco de dados.
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        // Para o MVP, criar usuário admin em memória
        // Senha: admin
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        
        users.put("admin", admin);
        
        log.info("User 'admin' created successfully for MVP");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        
        if (user == null) {
            log.warn("User '{}' not found", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        log.debug("User '{}' loaded successfully", username);
        return user;
    }
}



