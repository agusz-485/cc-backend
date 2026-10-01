package com.careconnect.service.impl;

import com.careconnect.dto.chat.ConversacionResponseDTO;
import com.careconnect.dto.chat.CrearConversacionDTO;
import com.careconnect.dto.chat.CrearMensajeDTO;
import com.careconnect.dto.chat.MensajeResponseDTO;
import com.careconnect.exception.ResourceNotFoundException;
import com.careconnect.model.Conversacion;
import com.careconnect.model.Mensaje;
import com.careconnect.model.Usuario;
import com.careconnect.repository.ConversacionRepository;
import com.careconnect.repository.MensajeRepository;
import com.careconnect.repository.UsuarioRepository;
import com.careconnect.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConversacionResponseDTO> obtenerConversacionesDeUsuario(Long usuarioId, String email) {
        Usuario usuario = resolverUsuario(usuarioId, email);
        if (usuario == null) {
            return new ArrayList<>();
        }

        List<Conversacion> conversaciones = conversacionRepository.findAllByUsuario(usuario);
        return conversaciones.stream()
                .map(c -> mapearConversacionADTO(c, usuario.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversacionResponseDTO obtenerOCrearConversacion(CrearConversacionDTO dto, String email) {
        Usuario remitente = null;
        if (dto.getUsuario1Id() != null) {
            remitente = usuarioRepository.findById(dto.getUsuario1Id()).orElse(null);
        } else if (dto.getFamiliarId() != null) {
            remitente = usuarioRepository.findById(dto.getFamiliarId()).orElse(null);
        }
        if (remitente == null && email != null) {
            remitente = usuarioRepository.findByEmail(email).orElse(null);
        }
        if (remitente == null) {
            throw new ResourceNotFoundException("No se pudo identificar el usuario emisor o solicitante de la conversación");
        }

        Long targetDestId = dto.getUsuario2Id();
        if (targetDestId == null) {
            targetDestId = dto.getCuidadorId();
        }
        if (targetDestId == null) {
            targetDestId = dto.getDestinatarioId();
        }
        if (targetDestId == null) {
            throw new IllegalArgumentException("Debe especificarse el destinatario de la conversación");
        }

        final Long finalDestinatarioId = targetDestId;
        Usuario destinatario = usuarioRepository.findById(finalDestinatarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario no encontrado con ID: " + finalDestinatarioId));

        if (remitente.getId().equals(destinatario.getId())) {
            throw new IllegalArgumentException("No es posible crear una conversación consigo mismo");
        }

        // Buscar si ya existe una conversación entre ambos
        Optional<Conversacion> existente = conversacionRepository.findEntreUsuarios(remitente, destinatario);
        Conversacion conversacion;
        if (existente.isPresent()) {
            conversacion = existente.get();
        } else {
            conversacion = Conversacion.builder()
                    .usuario1(remitente)
                    .usuario2(destinatario)
                    .ultimoMensaje("Conversación iniciada")
                    .ultimoMensajeFecha(LocalDateTime.now())
                    .build();
            conversacion = conversacionRepository.save(conversacion);
        }

        if (dto.getPrimerMensaje() != null && !dto.getPrimerMensaje().trim().isEmpty()) {
            Mensaje primerMsg = Mensaje.builder()
                    .conversacion(conversacion)
                    .remitente(remitente)
                    .destinatario(destinatario)
                    .contenido(dto.getPrimerMensaje().trim())
                    .leido(false)
                    .build();
            mensajeRepository.save(primerMsg);

            conversacion.setUltimoMensaje(dto.getPrimerMensaje().trim());
            conversacion.setUltimoMensajeFecha(LocalDateTime.now());
            conversacion = conversacionRepository.save(conversacion);
        }

        return mapearConversacionADTO(conversacion, remitente.getId());
    }

    @Override
    @Transactional
    public List<MensajeResponseDTO> obtenerMensajes(Long conversacionId, Long usuarioId, String email) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada con ID: " + conversacionId));

        Usuario usuarioActual = resolverUsuario(usuarioId, email);
        if (usuarioActual != null) {
            // Marcar mensajes recibidos como leídos
            mensajeRepository.marcarComoLeidos(conversacionId, usuarioActual.getId());
        }

        List<Mensaje> mensajes = mensajeRepository.findByConversacionOrderByCreatedAtAsc(conversacion);
        return mensajes.stream()
                .map(this::mapearMensajeADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MensajeResponseDTO enviarMensaje(Long conversacionId, CrearMensajeDTO dto, String email) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada con ID: " + conversacionId));

        Usuario remitente = null;
        if (dto.getRemitenteId() != null) {
            remitente = usuarioRepository.findById(dto.getRemitenteId()).orElse(null);
        }
        if (remitente == null && email != null) {
            remitente = usuarioRepository.findByEmail(email).orElse(null);
        }
        if (remitente == null) {
            throw new ResourceNotFoundException("No se pudo identificar el usuario remitente");
        }

        Usuario destinatario = null;
        if (dto.getDestinatarioId() != null) {
            destinatario = usuarioRepository.findById(dto.getDestinatarioId()).orElse(null);
        }
        if (destinatario == null) {
            // Deducir destinatario como el otro usuario de la conversación
            if (Objects.equals(conversacion.getUsuario1().getId(), remitente.getId())) {
                destinatario = conversacion.getUsuario2();
            } else {
                destinatario = conversacion.getUsuario1();
            }
        }

        Mensaje mensaje = Mensaje.builder()
                .conversacion(conversacion)
                .remitente(remitente)
                .destinatario(destinatario)
                .contenido(dto.getContenido().trim())
                .leido(false)
                .build();
        mensaje = mensajeRepository.save(mensaje);

        // Actualizar último mensaje en la conversación
        conversacion.setUltimoMensaje(dto.getContenido().trim());
        conversacion.setUltimoMensajeFecha(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        return mapearMensajeADTO(mensaje);
    }

    private Usuario resolverUsuario(Long usuarioId, String email) {
        if (usuarioId != null) {
            return usuarioRepository.findById(usuarioId).orElse(null);
        }
        if (email != null) {
            return usuarioRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    private ConversacionResponseDTO mapearConversacionADTO(Conversacion c, Long currentUserId) {
        Usuario u1 = c.getUsuario1();
        Usuario u2 = c.getUsuario2();

        Usuario otro = (currentUserId != null && Objects.equals(u1.getId(), currentUserId)) ? u2 : u1;
        long noLeidos = currentUserId != null ? mensajeRepository.countByConversacionAndDestinatarioAndLeidoFalse(c, otro) : 0;

        String u1Nombre = (u1.getNombre() != null ? u1.getNombre() : "") + " " + (u1.getApellido() != null ? u1.getApellido() : "");
        String u2Nombre = (u2.getNombre() != null ? u2.getNombre() : "") + " " + (u2.getApellido() != null ? u2.getApellido() : "");
        String otroNombre = (otro.getNombre() != null ? otro.getNombre() : "") + " " + (otro.getApellido() != null ? otro.getApellido() : "");

        return ConversacionResponseDTO.builder()
                .id(c.getId())
                .usuario1Id(u1.getId())
                .usuario1Nombre(u1Nombre.trim())
                .usuario1Foto(u1.getFotoPerfil())
                .usuario1Rol(u1.getRol())
                .usuario2Id(u2.getId())
                .usuario2Nombre(u2Nombre.trim())
                .usuario2Foto(u2.getFotoPerfil())
                .usuario2Rol(u2.getRol())
                .familiarId(u1.getId())
                .familiarNombre(u1Nombre.trim())
                .familiarFoto(u1.getFotoPerfil())
                .cuidadorId(u2.getId())
                .cuidadorNombre(u2Nombre.trim())
                .cuidadorFoto(u2.getFotoPerfil())
                .destinatarioId(otro.getId())
                .otroUsuarioNombre(otroNombre.trim())
                .otroUsuarioFoto(otro.getFotoPerfil())
                .otroUsuarioTelefono(otro.getTelefono())
                .otroUsuarioRol(otro.getRol())
                .ultimoMensaje(c.getUltimoMensaje())
                .fechaActualizacion(c.getUltimoMensajeFecha() != null ? c.getUltimoMensajeFecha() : c.getCreatedAt())
                .mensajesNoLeidos(noLeidos)
                .build();
    }

    private MensajeResponseDTO mapearMensajeADTO(Mensaje m) {
        Usuario rem = m.getRemitente();
        Usuario dest = m.getDestinatario();

        String remNombre = (rem.getNombre() != null ? rem.getNombre() : "") + " " + (rem.getApellido() != null ? rem.getApellido() : "");
        String destNombre = (dest.getNombre() != null ? dest.getNombre() : "") + " " + (dest.getApellido() != null ? dest.getApellido() : "");

        return MensajeResponseDTO.builder()
                .id(m.getId())
                .conversacionId(m.getConversacion().getId())
                .remitenteId(rem.getId())
                .remitenteNombre(remNombre.trim())
                .remitenteFoto(rem.getFotoPerfil())
                .destinatarioId(dest.getId())
                .destinatarioNombre(destNombre.trim())
                .destinatarioFoto(dest.getFotoPerfil())
                .contenido(m.getContenido())
                .leido(m.getLeido())
                .fechaEnvio(m.getCreatedAt())
                .build();
    }
}