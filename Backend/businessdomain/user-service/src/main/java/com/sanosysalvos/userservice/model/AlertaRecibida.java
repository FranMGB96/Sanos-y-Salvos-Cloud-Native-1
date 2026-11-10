package com.sanosysalvos.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Alertas por zona recibidas desde Kafka (CU2, consumidor user-alerts-group).
 * alertaId es único: una alerta repetida no se guarda dos veces (idempotencia).
 */
@Entity
@Table(name = "alertas_recibidas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaRecibida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alerta_id", nullable = false, unique = true, length = 64)
    private String alertaId;

    @Column(nullable = false, length = 40)
    private String zona;

    @Column(length = 20)
    private String tipo;

    private Integer cantidad;

    @Column(length = 10)
    private String severidad;

    @Column(length = 300)
    private String motivo;

    /** Ids de los reportes separados por coma, ej: "15,16,17". */
    @Column(name = "report_ids", length = 500)
    private String reportIds;

    /** Cuándo la generó analytics-service. */
    @Column(name = "generada_en", length = 40)
    private String generadaEn;

    /** Cuándo llegó a este servicio. */
    @Column(name = "recibida_en")
    @Builder.Default
    private LocalDateTime recibidaEn = LocalDateTime.now();
}
