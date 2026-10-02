package com.careconnect.controller;

import com.careconnect.dto.admin.AdminDashboardMetricsDTO;
import com.careconnect.dto.admin.UsuarioModeracionDTO;
import com.careconnect.dto.reporte.ReporteResolucionDTO;
import com.careconnect.dto.reporte.ReporteResponseDTO;
import com.careconnect.service.AdminService;
import com.careconnect.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ReporteService reporteService;

    public AdminController(AdminService adminService, ReporteService reporteService) {
        this.adminService = adminService;
        this.reporteService = reporteService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioModeracionDTO>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuariosParaModerar());
    }

    @PatchMapping("/usuarios/{id}/aprobar")
    public ResponseEntity<Void> aprobarUsuario(@PathVariable Long id) {
        adminService.aprobarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/usuarios/{id}/suspender")
    public ResponseEntity<Void> suspenderUsuario(@PathVariable Long id) {
        adminService.suspenderUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metricas")
    public ResponseEntity<AdminDashboardMetricsDTO> obtenerMetricas() {
        return ResponseEntity.ok(adminService.obtenerMetricas());
    }

    @GetMapping("/reportes")
    public ResponseEntity<List<ReporteResponseDTO>> listarReportes() {
        return ResponseEntity.ok(reporteService.listarTodosParaAdmin());
    }

    @GetMapping("/reportes/{id}")
    public ResponseEntity<ReporteResponseDTO> obtenerReporte(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @RequestMapping(value = "/reportes/{id}/resolver", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ReporteResponseDTO> resolverReporte(
            @PathVariable Long id,
            @Valid @RequestBody ReporteResolucionDTO dto) {
        return ResponseEntity.ok(reporteService.resolverReporte(id, dto));
    }

    @GetMapping("/reportes/metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricasReportes() {
        return ResponseEntity.ok(reporteService.obtenerMetricasReportes());
    }
}
