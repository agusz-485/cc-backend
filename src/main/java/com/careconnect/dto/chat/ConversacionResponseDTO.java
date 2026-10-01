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
public class ConversacionResponseDTO {
    private Long id;
    
    // Participantes genericos
    private Long usuario1Id;
    private String usuario1Nombre;
    private String usuario1Foto;
    private String usuario1Rol;
    
    private Long usuario2Id;
    private String usuario2Nombre;
    private String usuario2Foto;
    private String usuario2Rol;

    // Aliases para frontend
    private Long familiarId;
    private String familiarNombre;
    private String familiarFoto;
    private Long cuidadorId;
    private String cuidadorNombre;
    private String cuidadorFoto;

    // Contacto interlocutor (el otro participante)
    private Long destinatarioId;
    private String otroUsuarioNombre;
    private String otroUsuarioFoto;
    private String otroUsuarioTelefono;
    private String otroUsuarioRol;

    private String ultimoMensaje;
    private LocalDateTime fechaActualizacion;
    private Long mensajesNoLeidos;
}