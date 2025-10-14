package com.techchallenge.security;

import com.techchallenge.domain.dto.LoginRequestDTO;
import com.techchallenge.domain.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Serviço de autenticação.
 * Responsável por processar login e gerar tokens JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    /**
     * Autentica um usuário e retorna um token JWT.
     * 
     * @param request Credenciais de login
     * @return Response contendo o token JWT
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Attempting login for user: {}", request.getUsername());

        // Autentica o usuário
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Carrega os detalhes do usuário
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Gera o token JWT
        String token = jwtService.generateToken(userDetails);

        log.info("User '{}' authenticated successfully", request.getUsername());

        return new LoginResponseDTO(
                token,
                userDetails.getUsername(),
                jwtService.getExpirationTime()
        );
    }
}



