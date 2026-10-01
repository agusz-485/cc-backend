package com.careconnect.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearConversacionDTO {
    private Long familiarId;
    private Long cuidadorId;
    private Long usuario1Id;
    private Long usuario2Id;
    private Long destinatarioId;
    private String primerMensaje;
}