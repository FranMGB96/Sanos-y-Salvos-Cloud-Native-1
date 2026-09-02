package com.sanosysalvos.petservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "pets") @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Pet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank @Column(nullable = false) private String nombre;
    @NotBlank @Column(nullable = false) private String especie;
    private String raza;
    private String color;
    @Enumerated(EnumType.STRING) private Tamanio tamanio;
    @Column(name = "foto_url") private String fotoUrl;
    private String descripcion;
    @NotNull @Column(name = "owner_id", nullable = false) private Long ownerId;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default private Boolean active = true;
    public enum Tamanio { PEQUENIO, MEDIANO, GRANDE }
}
