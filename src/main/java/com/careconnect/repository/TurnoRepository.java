package com.careconnect.repository;

import com.careconnect.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByFamiliarId(Long familiarId);
    List<Turno> findByCuidadorId(Long cuidadorId);
    List<Turno> findByAdultoMayorId(Long adultoMayorId);
}