package com.careconnect.controller;

import com.careconnect.dto.turno.TurnoCambioEstadoDTO;
import com.careconnect.dto.turno.TurnoRequestDTO;
import com.careconnect.dto.turno.TurnoResponseDTO;
import com.careconnect.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/turnos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping
    public ResponseEntity<TurnoResponseDTO> crear(@Valid @RequestBody TurnoRequestDTO dto) {
        TurnoResponseDTO response = turnoService.crear(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> buscarPorId(@PathVariable Long id) {
        TurnoResponseDTO response = turnoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> buscar(
            @RequestParam(required = false) Long familiarId,
            @RequestParam(required = false) Long cuidadorId) {
        if (familiarId != null) {
            return ResponseEntity.ok(turnoService.buscarPorFamiliar(familiarId));
        }
        if (cuidadorId != null) {
            return ResponseEntity.ok(turnoService.buscarPorCuidador(cuidadorId));
        }
        return ResponseEntity.ok(turnoService.buscarTodos());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody TurnoCambioEstadoDTO dto) {
        TurnoResponseDTO response = turnoService.cambiarEstado(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        turnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}