package com.careconnect.dto.resenia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReseniaRequestDTO {

    @NotNull(message = "El ID del turno es obligatorio")
    private Long turnoId;

    private Long autorId;

    @NotNull(message = "La puntuación en estrellas es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1 estrella")
    @Max(value = 5, message = "La puntuación máxima es 5 estrellas")
    private Integer puntuacion;

    private String comentario;
}