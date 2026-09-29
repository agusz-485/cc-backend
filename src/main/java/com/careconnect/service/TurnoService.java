package com.careconnect.service;

import com.careconnect.dto.turno.TurnoCambioEstadoDTO;
import com.careconnect.dto.turno.TurnoRequestDTO;
import com.careconnect.dto.turno.TurnoResponseDTO;

import java.util.List;

public interface TurnoService {
    TurnoResponseDTO crear(TurnoRequestDTO dto);
    TurnoResponseDTO buscarPorId(Long id);
    List<TurnoResponseDTO> buscarPorFamiliar(Long familiarId);
    List<TurnoResponseDTO> buscarPorCuidador(Long cuidadorId);
    List<TurnoResponseDTO> buscarTodos();
    TurnoResponseDTO cambiarEstado(Long id, TurnoCambioEstadoDTO dto);
    void eliminar(Long id);
}