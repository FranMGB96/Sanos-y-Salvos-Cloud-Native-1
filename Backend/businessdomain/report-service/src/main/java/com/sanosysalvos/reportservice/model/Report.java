package com.sanosysalvos.reportservice.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private TipoReporte tipo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    private Double latitud;

    private Double longitud;

    private String ubicacionDescripcion;

    @Column(name = "pet_id")
    private Long petId;

    @NotNull
    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoReporte estado = EstadoReporte.ACTIVO;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum TipoReporte {
        PERDIDO,
        ENCONTRADO
    }

    public enum EstadoReporte {
        ACTIVO,
        RESUELTO,
        CERRADO
    }
}