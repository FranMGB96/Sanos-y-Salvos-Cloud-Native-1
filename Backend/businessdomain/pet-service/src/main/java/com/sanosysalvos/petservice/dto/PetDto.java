package com.sanosysalvos.petservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PetDto {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio") private String nombre;
    @NotBlank(message = "La especie es obligatoria") private String especie;
    private String raza;
    private String color;
    private String tamanio;
    private String fotoUrl;
    private String descripcion;
    @NotNull(message = "El ownerId es obligatorio") private Long ownerId;
    private Boolean active;
    private LocalDateTime createdAt;
}
