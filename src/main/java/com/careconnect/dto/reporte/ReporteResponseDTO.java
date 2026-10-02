package com.careconnect.dto.reporte;

import com.careconnect.model.enums.EstadoReporte;
import com.careconnect.model.enums.TipoReporte;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponseDTO {

    private Long id;
    private String codigoTicket;
    private Long reportanteId;
    private String reportanteNombre;
    private String reportanteEmail;
    private String reportanteRol;
    private Long reportadoId;
    private String reportadoNombre;
    private String reportadoEmail;
    private Long turnoId;
    private TipoReporte tipo;
    private String motivo;
    private String descripcion;
    private EstadoReporte estado;
    private String respuestaAdmin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
