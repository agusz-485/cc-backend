package com.careconnect.dto.reporte;

import com.careconnect.model.enums.TipoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRequestDTO {

    private Long reportadoId;
    private Long turnoId;

    @NotNull(message = "El tipo de reporte es obligatorio")
    private TipoReporte tipo;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 150, message = "El motivo no puede exceder 150 caracteres")
    private String motivo;

    @NotBlank(message = "La descripción del reporte es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String descripcion;
}
