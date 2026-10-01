package com.careconnect.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {
    private Long id;
    private Long conversacionId;
    private Long remitenteId;
    private String remitenteNombre;
    private String remitenteFoto;
    private Long destinatarioId;
    private String destinatarioNombre;
    private String destinatarioFoto;
    private String contenido;
    private Boolean leido;
    private LocalDateTime fechaEnvio;
}