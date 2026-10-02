package com.careconnect.model;

import com.careconnect.model.enums.EstadoReporte;
import com.careconnect.model.enums.TipoReporte;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_id")
    private Usuario reportado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoReporte tipo;

    @Column(nullable = false, length = 150)
    private String motivo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;

    @Column(name = "respuesta_admin", columnDefinition = "TEXT")
    private String respuestaAdmin;
}
