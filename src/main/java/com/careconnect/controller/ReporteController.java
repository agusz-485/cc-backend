package com.careconnect.controller;

import com.careconnect.dto.reporte.ReporteRequestDTO;
import com.careconnect.dto.reporte.ReporteResponseDTO;
import com.careconnect.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping
    public ResponseEntity<ReporteResponseDTO> crearReporte(
            @Valid @RequestBody ReporteRequestDTO dto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteService.crearReporte(dto, email));
    }

    @GetMapping("/mis-reportes")
    public ResponseEntity<List<ReporteResponseDTO>> listarMisReportes(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(reporteService.listarMisReportes(email));
    }
}
