package com.sanosysalvos.petservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Historial de eventos de cada mascota (CU1, consumidor pet-history-group).
 * El eventId es único: si Kafka entrega dos veces el mismo evento, el segundo
 * no se guarda (idempotencia).
 */
@Entity
@Table(name = "pet_event_history", indexes = @Index(name = "idx_history_pet", columnList = "pet_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetEventHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "tipo_evento", nullable = false, length = 30)
    private String tipoEvento;

    @Column(length = 20)
    private String tipo;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(length = 20)
    private String estado;

    /** Cuándo ocurrió el hecho (viene en el evento). */
    @Column(name = "ocurrido_en", length = 40)
    private String ocurridoEn;

    /** Cuándo lo procesó este servicio. */
    @Column(name = "registrado_en")
    @Builder.Default
    private LocalDateTime registradoEn = LocalDateTime.now();
}
