package com.sanosysalvos.reportservice.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    private Long id;

    @NotNull(message = "El tipo es obligatorio (PERDIDO o ENCONTRADO)")
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private Double latitud;

    private Double longitud;

    private String ubicacionDescripcion;

    private Long petId;

    @NotNull(message = "El reporterUserId es obligatorio")
    private Long reporterUserId;

    private String estado;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}