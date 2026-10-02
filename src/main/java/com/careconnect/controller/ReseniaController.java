package com.careconnect.controller;

import com.careconnect.dto.resenia.ReseniaRequestDTO;
import com.careconnect.dto.resenia.ReseniaResponseDTO;
import com.careconnect.service.ReseniaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resenias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReseniaController {

    private final ReseniaService reseniaService;

    @PostMapping
    public ResponseEntity<ReseniaResponseDTO> crearOActualizar(
            @Valid @RequestBody ReseniaRequestDTO dto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        ReseniaResponseDTO response = reseniaService.crearOActualizar(dto, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/turno/{turnoId}/mi-resenia")
    public ResponseEntity<ReseniaResponseDTO> obtenerMiResenia(
            @PathVariable Long turnoId,
            @RequestParam(required = false) Long autorId,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return reseniaService.obtenerPorTurnoYAutor(turnoId, autorId, email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/cuidador/{cuidadorId}")
    public ResponseEntity<List<ReseniaResponseDTO>> obtenerPorCuidador(@PathVariable Long cuidadorId) {
        return ResponseEntity.ok(reseniaService.obtenerPorCuidador(cuidadorId));
    }

    @GetMapping("/cuidador/{cuidadorId}/metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricasCuidador(@PathVariable Long cuidadorId) {
        return ResponseEntity.ok(reseniaService.obtenerMetricasCuidador(cuidadorId));
    }

    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<List<ReseniaResponseDTO>> obtenerPorTurno(@PathVariable Long turnoId) {
        return ResponseEntity.ok(reseniaService.obtenerPorTurno(turnoId));
    }
}