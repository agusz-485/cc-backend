package com.careconnect.service.impl;

import com.careconnect.dto.resenia.ReseniaRequestDTO;
import com.careconnect.dto.resenia.ReseniaResponseDTO;
import com.careconnect.exception.ResourceNotFoundException;
import com.careconnect.model.Resenia;
import com.careconnect.model.Turno;
import com.careconnect.model.Usuario;
import com.careconnect.repository.ReseniaRepository;
import com.careconnect.repository.TurnoRepository;
import com.careconnect.repository.UsuarioRepository;
import com.careconnect.service.ReseniaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReseniaServiceImpl implements ReseniaService {

    private final ReseniaRepository reseniaRepository;
    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ReseniaResponseDTO crearOActualizar(ReseniaRequestDTO dto, String email) {
        Turno turno = turnoRepository.findById(dto.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + dto.getTurnoId()));

        Usuario autor = null;
        if (dto.getAutorId() != null) {
            autor = usuarioRepository.findById(dto.getAutorId()).orElse(null);
        }
        if (autor == null && email != null) {
            autor = usuarioRepository.findByEmail(email).orElse(null);
        }
        if (autor == null) {
            autor = turno.getFamiliar();
        }
        if (autor == null) {
            throw new ResourceNotFoundException("No se pudo identificar el usuario autor de la reseña");
        }

        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5) {
            throw new IllegalArgumentException("La puntuación debe ser un número entero entre 1 y 5 estrellas");
        }

        Usuario targetCuidador = turno.getCuidador();
        if (targetCuidador == null && turno.getCuidador() != null) {
            targetCuidador = usuarioRepository.findById(turno.getCuidador().getId()).orElse(null);
        }

        Optional<Resenia> existente = reseniaRepository.findByTurnoIdAndAutorId(turno.getId(), autor.getId());
        Resenia resenia;
        String comentarioLimpio = (dto.getComentario() != null && !dto.getComentario().trim().isEmpty())
                ? dto.getComentario().trim()
                : null;

        if (existente.isPresent()) {
            resenia = existente.get();
            resenia.setPuntuacion(dto.getPuntuacion());
            resenia.setComentario(comentarioLimpio);
        } else {
            resenia = Resenia.builder()
                    .turno(turno)
                    .autor(autor)
                    .cuidador(targetCuidador)
                    .puntuacion(dto.getPuntuacion())
                    .comentario(comentarioLimpio)
                    .visible(true)
                    .reportada(false)
                    .build();
        }

        resenia = reseniaRepository.save(resenia);
        return mapearADTO(resenia);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReseniaResponseDTO> obtenerPorTurnoYAutor(Long turnoId, Long autorId, String email) {
        Usuario autor = null;
        if (autorId != null) {
            autor = usuarioRepository.findById(autorId).orElse(null);
        }
        if (autor == null && email != null) {
            autor = usuarioRepository.findByEmail(email).orElse(null);
        }
        if (autor == null) {
            return Optional.empty();
        }

        return reseniaRepository.findByTurnoIdAndAutorId(turnoId, autor.getId())
                .map(this::mapearADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReseniaResponseDTO> obtenerPorCuidador(Long cuidadorId) {
        return reseniaRepository.findByCuidadorIdAndVisibleTrueOrderByCreatedAtDesc(cuidadorId)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReseniaResponseDTO> obtenerPorTurno(Long turnoId) {
        return reseniaRepository.findByTurnoId(turnoId)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerMetricasCuidador(Long cuidadorId) {
        Double promedio = reseniaRepository.calcularPromedioCuidador(cuidadorId);
        Long total = reseniaRepository.contarReseniasCuidador(cuidadorId);

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("promedio", promedio != null ? Math.round(promedio * 10.0) / 10.0 : 5.0);
        metricas.put("totalResenias", total != null ? total : 0L);
        return metricas;
    }

    private ReseniaResponseDTO mapearADTO(Resenia r) {
        Usuario autor = r.getAutor();
        String autorNombre = autor != null ? ((autor.getNombre() != null ? autor.getNombre() : "") + " " + (autor.getApellido() != null ? autor.getApellido() : "")).trim() : "Usuario";

        String cuidadorNombre = r.getCuidador() != null ? ((r.getCuidador().getNombre() != null ? r.getCuidador().getNombre() : "") + " " + (r.getCuidador().getApellido() != null ? r.getCuidador().getApellido() : "")).trim() : "Cuidador";

        return ReseniaResponseDTO.builder()
                .id(r.getId())
                .turnoId(r.getTurno().getId())
                .autorId(autor != null ? autor.getId() : null)
                .autorNombre(autorNombre)
                .autorFoto(autor != null ? autor.getFotoPerfil() : null)
                .autorRol(autor != null ? autor.getRol() : "FAMILIAR")
                .cuidadorId(r.getCuidador() != null ? r.getCuidador().getId() : null)
                .cuidadorNombre(cuidadorNombre)
                .puntuacion(r.getPuntuacion())
                .comentario(r.getComentario())
                .fechaCreacion(r.getCreatedAt())
                .build();
    }
}
