package com.careconnect.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario1_id", nullable = false)
    private Usuario usuario1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario2_id", nullable = false)
    private Usuario usuario2;

    @Column(name = "ultimo_mensaje", length = 500)
    private String ultimoMensaje;

    @Column(name = "ultimo_mensaje_fecha")
    private LocalDateTime ultimoMensajeFecha;

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Mensaje> mensajes = new ArrayList<>();
}