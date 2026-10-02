package com.careconnect.dto.reporte;

import com.careconnect.model.enums.EstadoReporte;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResolucionDTO {

    @NotNull(message = "El nuevo estado del reporte es obligatorio")
    private EstadoReporte estado;

    private String respuestaAdmin;
}
