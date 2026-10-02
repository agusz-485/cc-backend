package com.careconnect.dto.enfermero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnfermeroPerfilResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String fotoPerfil;
    private String matriculaProfesional;
    private String descripcion;
    private Integer aniosExperiencia;
    private String zonaPrincipal;
    private BigDecimal precioHora;
    private boolean visible;
    private Double calificacionPromedio;
    private Long totalResenas;
    private Long totalResenias;
}
