package com.careconnect.controller;

import com.careconnect.dto.chat.ConversacionResponseDTO;
import com.careconnect.dto.chat.CrearConversacionDTO;
import com.careconnect.dto.chat.CrearMensajeDTO;
import com.careconnect.dto.chat.MensajeResponseDTO;
import com.careconnect.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversaciones")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ConversacionController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ConversacionResponseDTO>> obtenerConversaciones(
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(chatService.obtenerConversacionesDeUsuario(usuarioId, email));
    }

    @PostMapping
    public ResponseEntity<ConversacionResponseDTO> iniciarConversacion(
            @RequestBody CrearConversacionDTO dto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        ConversacionResponseDTO response = chatService.obtenerOCrearConversacion(dto, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeResponseDTO>> obtenerMensajes(
            @PathVariable Long id,
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(chatService.obtenerMensajes(id, usuarioId, email));
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeResponseDTO> enviarMensaje(
            @PathVariable Long id,
            @Valid @RequestBody CrearMensajeDTO dto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        MensajeResponseDTO response = chatService.enviarMensaje(id, dto, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}