package com.careconnect.service;

import com.careconnect.dto.resenia.ReseniaRequestDTO;
import com.careconnect.dto.resenia.ReseniaResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReseniaService {

    ReseniaResponseDTO crearOActualizar(ReseniaRequestDTO dto, String email);

    Optional<ReseniaResponseDTO> obtenerPorTurnoYAutor(Long turnoId, Long autorId, String email);

    List<ReseniaResponseDTO> obtenerPorCuidador(Long cuidadorId);

    List<ReseniaResponseDTO> obtenerPorTurno(Long turnoId);

    Map<String, Object> obtenerMetricasCuidador(Long cuidadorId);
}