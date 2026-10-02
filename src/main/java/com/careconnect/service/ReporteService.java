package com.careconnect.service;

import com.careconnect.dto.reporte.ReporteRequestDTO;
import com.careconnect.dto.reporte.ReporteResolucionDTO;
import com.careconnect.dto.reporte.ReporteResponseDTO;
import com.careconnect.model.enums.EstadoReporte;

import java.util.List;
import java.util.Map;

public interface ReporteService {

    ReporteResponseDTO crearReporte(ReporteRequestDTO dto, String emailReportante);

    List<ReporteResponseDTO> listarMisReportes(String emailReportante);

    List<ReporteResponseDTO> listarTodosParaAdmin();

    List<ReporteResponseDTO> listarPorEstado(EstadoReporte estado);

    ReporteResponseDTO obtenerPorId(Long id);

    ReporteResponseDTO resolverReporte(Long id, ReporteResolucionDTO dto);

    Map<String, Object> obtenerMetricasReportes();
}
