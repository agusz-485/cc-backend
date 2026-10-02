package com.careconnect.repository;

import com.careconnect.model.Reporte;
import com.careconnect.model.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByReportanteIdOrderByCreatedAtDesc(Long reportanteId);

    List<Reporte> findAllByOrderByCreatedAtDesc();

    List<Reporte> findByEstadoOrderByCreatedAtDesc(EstadoReporte estado);

    long countByEstado(EstadoReporte estado);
}
