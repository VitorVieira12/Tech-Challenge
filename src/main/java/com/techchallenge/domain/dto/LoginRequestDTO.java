package com.techchallenge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    @NotBlank(message = "Username é obrigatório")
    private String username;
    
    @NotBlank(message = "Password é obrigatório")
    private String password;
}



