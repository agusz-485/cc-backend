package com.careconnect.dto.enfermero;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnfermeroPerfilUpdateDTO {

    private String nombre;
    private String apellido;
    private String telefono;
    private String fotoPerfil;
    private String matriculaProfesional;

    @Size(max = 4000, message = "La descripcion no puede superar los 4000 caracteres")
    private String descripcion;

    @Min(value = 0, message = "Los anios de experiencia no pueden ser negativos")
    private Integer aniosExperiencia;

    private String zonaPrincipal;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio por hora debe ser mayor a 0")
    private BigDecimal precioHora;

    private Boolean visible;
}
