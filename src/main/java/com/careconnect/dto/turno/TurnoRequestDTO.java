package com.careconnect.dto.turno;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {

    @NotNull(message = "El ID del familiar es obligatorio")
    private Long familiarId;

    @NotNull(message = "El ID del cuidador es obligatorio")
    private Long cuidadorId;

    @NotNull(message = "El ID del adulto mayor es obligatorio")
    private Long adultoMayorId;

    @NotNull(message = "La fecha del turno es obligatoria")
    private LocalDate fecha;

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionMinutos;

    private Long tipoServicioId;
    private String tipoServicioNombre;
    private String descripcionServicio;
    private BigDecimal precioTotal;
}