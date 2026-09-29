package com.careconnect.dto.turno;

import com.careconnect.model.enums.EstadoTurno;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoCambioEstadoDTO {

    @NotNull(message = "El nuevo estado del turno es obligatorio")
    private EstadoTurno estado;

    private String motivoCancelacion;
}