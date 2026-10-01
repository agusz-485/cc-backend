package com.careconnect.repository;

import com.careconnect.model.Conversacion;
import com.careconnect.model.Mensaje;
import com.careconnect.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByConversacionOrderByCreatedAtAsc(Conversacion conversacion);

    List<Mensaje> findByConversacionIdOrderByCreatedAtAsc(Long conversacionId);

    long countByConversacionAndDestinatarioAndLeidoFalse(Conversacion conversacion, Usuario destinatario);

    @Modifying
    @Query("UPDATE Mensaje m SET m.leido = true WHERE m.conversacion.id = :conversacionId AND m.destinatario.id = :destinatarioId AND m.leido = false")
    void marcarComoLeidos(@Param("conversacionId") Long conversacionId, @Param("destinatarioId") Long destinatarioId);
}