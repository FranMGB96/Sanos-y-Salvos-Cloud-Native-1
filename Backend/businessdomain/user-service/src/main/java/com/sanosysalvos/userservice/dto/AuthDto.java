package com.sanosysalvos.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class AuthDto {

    @Data public static class RegisterRequest {
        @NotBlank private String nombre;
        @Email @NotBlank private String email;
        @NotBlank @Size(min = 6) private String password;
        @NotBlank private String telefono;
        private String rol;
    }

    @Data public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data public static class AuthResponse {
        private String token;
        private String tipo = "Bearer";
        private Long userId;
        private String nombre;
        private String email;
        private String telefono;
        private String rol;

        public AuthResponse(String token, Long userId, String nombre, String email, String telefono, String rol) {
            this.token    = token;
            this.userId   = userId;
            this.nombre   = nombre;
            this.email    = email;
            this.telefono = telefono;
            this.rol      = rol;
        }
    }
}