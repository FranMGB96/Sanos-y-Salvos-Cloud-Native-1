package com.sanosysalvos.userservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private Long id;
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String rol;
    private Boolean active;
    private LocalDateTime createdAt;
}