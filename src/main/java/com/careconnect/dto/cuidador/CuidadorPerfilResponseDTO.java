package com.careconnect.dto.cuidador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuidadorPerfilResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String fotoPerfil;
    private String descripcion;
    private Integer aniosExperiencia;
    private String zonaPrincipal;
    private BigDecimal precioHora;
    private boolean disponible;
    private Double calificacionPromedio;
    private Long totalResenas;
    private Long totalResenias;
    private List<String> especialidades;
    private List<String> zonasCobertura;
}
