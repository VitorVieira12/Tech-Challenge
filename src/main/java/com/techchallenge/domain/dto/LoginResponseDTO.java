package com.techchallenge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private Long expiresIn; // milliseconds
    
    public LoginResponseDTO(String token, String username, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.expiresIn = expiresIn;
    }
}



