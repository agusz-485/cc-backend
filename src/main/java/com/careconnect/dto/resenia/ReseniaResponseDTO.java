package com.careconnect.dto.resenia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReseniaResponseDTO {

    private Long id;
    private Long turnoId;
    
    // Autor de la reseña
    private Long autorId;
    private String autorNombre;
    private String autorFoto;
    private String autorRol;

    // Cuidador evaluado
    private Long cuidadorId;
    private String cuidadorNombre;

    // Calificación
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaCreacion;
}