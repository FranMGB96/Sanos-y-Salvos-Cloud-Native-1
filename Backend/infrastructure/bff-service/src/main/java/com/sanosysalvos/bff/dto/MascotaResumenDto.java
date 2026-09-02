package com.sanosysalvos.bff.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MascotaResumenDto {
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private String color;
    private String fotoUrl;
    private Long ownerId;
    private LocalDateTime createdAt;
}
