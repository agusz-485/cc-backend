package com.careconnect.service.impl;

import com.careconnect.dto.reporte.ReporteRequestDTO;
import com.careconnect.dto.reporte.ReporteResolucionDTO;
import com.careconnect.dto.reporte.ReporteResponseDTO;
import com.careconnect.exception.ResourceNotFoundException;
import com.careconnect.model.Reporte;
import com.careconnect.model.Turno;
import com.careconnect.model.Usuario;
import com.careconnect.model.enums.EstadoReporte;
import com.careconnect.repository.ReporteRepository;
import com.careconnect.repository.TurnoRepository;
import com.careconnect.repository.UsuarioRepository;
import com.careconnect.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;

    @Override
    @Transactional
    public ReporteResponseDTO crearReporte(ReporteRequestDTO dto, String emailReportante) {
        Usuario reportante = usuarioRepository.findByEmail(emailReportante)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + emailReportante));

        Usuario reportado = null;
        if (dto.getReportadoId() != null) {
            reportado = usuarioRepository.findById(dto.getReportadoId()).orElse(null);
        }

        Turno turno = null;
        if (dto.getTurnoId() != null) {
            turno = turnoRepository.findById(dto.getTurnoId()).orElse(null);
            if (turno != null && reportado == null) {
                // Si reporta el familiar, el reportado es el cuidador; si reporta el cuidador, el reportado es el familiar
                if (turno.getFamiliar() != null && turno.getFamiliar().getId().equals(reportante.getId())) {
                    reportado = turno.getCuidador();
                } else if (turno.getCuidador() != null && turno.getCuidador().getId().equals(reportante.getId())) {
                    reportado = turno.getFamiliar();
                }
            }
        }

        Reporte reporte = Reporte.builder()
                .reportante(reportante)
                .reportado(reportado)
                .turno(turno)
                .tipo(dto.getTipo())
                .motivo(dto.getMotivo().trim())
                .descripcion(dto.getDescripcion().trim())
                .estado(EstadoReporte.PENDIENTE)
                .build();

        reporte = reporteRepository.save(reporte);
        return mapearADTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarMisReportes(String emailReportante) {
        Usuario reportante = usuarioRepository.findByEmail(emailReportante)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return reporteRepository.findByReportanteIdOrderByCreatedAtDesc(reportante.getId())
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarTodosParaAdmin() {
        return reporteRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarPorEstado(EstadoReporte estado) {
        return reporteRepository.findByEstadoOrderByCreatedAtDesc(estado)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDTO obtenerPorId(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + id));
        return mapearADTO(reporte);
    }

    @Override
    @Transactional
    public ReporteResponseDTO resolverReporte(Long id, ReporteResolucionDTO dto) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + id));

        reporte.setEstado(dto.getEstado());
        if (dto.getRespuestaAdmin() != null && !dto.getRespuestaAdmin().isBlank()) {
            reporte.setRespuestaAdmin(dto.getRespuestaAdmin().trim());
        }

        reporte = reporteRepository.save(reporte);
        return mapearADTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerMetricasReportes() {
        long total = reporteRepository.count();
        long pendientes = reporteRepository.countByEstado(EstadoReporte.PENDIENTE);
        long enRevision = reporteRepository.countByEstado(EstadoReporte.EN_REVISION);
        long resueltos = reporteRepository.countByEstado(EstadoReporte.RESUELTO);
        long desestimados = reporteRepository.countByEstado(EstadoReporte.DESESTIMADO);

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalReportes", total);
        metricas.put("pendientes", pendientes);
        metricas.put("enRevision", enRevision);
        metricas.put("resueltos", resueltos);
        metricas.put("desestimados", desestimados);
        return metricas;
    }

    private ReporteResponseDTO mapearADTO(Reporte r) {
        Usuario reportante = r.getReportante();
        String reportanteNombre = reportante != null
                ? ((reportante.getNombre() != null ? reportante.getNombre() : "") + " " + (reportante.getApellido() != null ? reportante.getApellido() : "")).trim()
                : "Usuario";

        Usuario reportado = r.getReportado();
        String reportadoNombre = reportado != null
                ? ((reportado.getNombre() != null ? reportado.getNombre() : "") + " " + (reportado.getApellido() != null ? reportado.getApellido() : "")).trim()
                : null;

        return ReporteResponseDTO.builder()
                .id(r.getId())
                .codigoTicket("REP-" + String.format("%04d", r.getId()))
                .reportanteId(reportante != null ? reportante.getId() : null)
                .reportanteNombre(reportanteNombre)
                .reportanteEmail(reportante != null ? reportante.getEmail() : null)
                .reportanteRol(reportante != null ? reportante.getRol() : null)
                .reportadoId(reportado != null ? reportado.getId() : null)
                .reportadoNombre(reportadoNombre)
                .reportadoEmail(reportado != null ? reportado.getEmail() : null)
                .turnoId(r.getTurno() != null ? r.getTurno().getId() : null)
                .tipo(r.getTipo())
                .motivo(r.getMotivo())
                .descripcion(r.getDescripcion())
                .estado(r.getEstado())
                .respuestaAdmin(r.getRespuestaAdmin())
                .fechaCreacion(r.getCreatedAt())
                .fechaActualizacion(r.getUpdatedAt())
                .build();
    }
}
