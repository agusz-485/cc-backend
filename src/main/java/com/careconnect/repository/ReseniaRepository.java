package com.careconnect.repository;

import com.careconnect.model.Resenia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReseniaRepository extends JpaRepository<Resenia, Long> {

    Optional<Resenia> findByTurnoIdAndAutorId(Long turnoId, Long autorId);

    List<Resenia> findByCuidadorIdAndVisibleTrueOrderByCreatedAtDesc(Long cuidadorId);

    List<Resenia> findByTurnoId(Long turnoId);

    List<Resenia> findByAutorId(Long autorId);

    @Query("SELECT AVG(r.puntuacion) FROM Resenia r WHERE r.cuidador.id = :cuidadorId AND r.visible = true")
    Double calcularPromedioCuidador(@Param("cuidadorId") Long cuidadorId);

    @Query("SELECT COUNT(r) FROM Resenia r WHERE r.cuidador.id = :cuidadorId AND r.visible = true")
    Long contarReseniasCuidador(@Param("cuidadorId") Long cuidadorId);
}