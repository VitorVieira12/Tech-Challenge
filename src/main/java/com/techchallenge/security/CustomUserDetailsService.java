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

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final Map<String, String> userPasswords = new HashMap<>();

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        userPasswords.put("admin", passwordEncoder.encode("admin"));
        log.info("User 'admin' created successfully for MVP");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String encodedPassword = userPasswords.get(username);

        if (encodedPassword == null) {
            log.warn("User '{}' not found", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        log.debug("User '{}' loaded successfully", username);
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }
}



