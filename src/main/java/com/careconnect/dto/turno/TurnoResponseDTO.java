package com.careconnect.dto.turno;

import com.careconnect.model.enums.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseDTO {

    private Long id;
    private Long familiarId;
    private String familiarNombre;
    private Long cuidadorId;
    private String cuidadorNombre;
    private Long adultoMayorId;
    private String adultoMayorNombre;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionMinutos;

    private EstadoTurno estadoTurno;
    private String tipoServicio;
    private String descripcionServicio;
    private BigDecimal precioTotal;
    private LocalDateTime timestampInicio;
    private String motivoCancelacion;
}