package com.careconnect.service;

import com.careconnect.dto.chat.ConversacionResponseDTO;
import com.careconnect.dto.chat.CrearConversacionDTO;
import com.careconnect.dto.chat.CrearMensajeDTO;
import com.careconnect.dto.chat.MensajeResponseDTO;

import java.util.List;

public interface ChatService {
    List<ConversacionResponseDTO> obtenerConversacionesDeUsuario(Long usuarioId, String email);
    ConversacionResponseDTO obtenerOCrearConversacion(CrearConversacionDTO dto, String email);
    List<MensajeResponseDTO> obtenerMensajes(Long conversacionId, Long usuarioId, String email);
    MensajeResponseDTO enviarMensaje(Long conversacionId, CrearMensajeDTO dto, String email);
}