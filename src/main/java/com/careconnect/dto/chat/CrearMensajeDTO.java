package com.careconnect.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearMensajeDTO {
    private Long remitenteId;
    private Long destinatarioId;
    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    private String contenido;
}