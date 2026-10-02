package com.careconnect.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resenias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resenia extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuidador_id", nullable = false)
    private Usuario cuidador;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(nullable = false)
    private Boolean visible;

    @Column(nullable = false)
    private Boolean reportada;
}
