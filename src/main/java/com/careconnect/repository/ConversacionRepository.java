package com.careconnect.repository;

import com.careconnect.model.Conversacion;
import com.careconnect.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    @Query("SELECT c FROM Conversacion c WHERE (c.usuario1 = :user1 AND c.usuario2 = :user2) OR (c.usuario1 = :user2 AND c.usuario2 = :user1)")
    Optional<Conversacion> findEntreUsuarios(@Param("user1") Usuario user1, @Param("user2") Usuario user2);

    @Query("SELECT c FROM Conversacion c WHERE c.usuario1 = :user OR c.usuario2 = :user ORDER BY c.updatedAt DESC, c.createdAt DESC")
    List<Conversacion> findAllByUsuario(@Param("user") Usuario user);

    @Query("SELECT c FROM Conversacion c WHERE c.usuario1.id = :userId OR c.usuario2.id = :userId ORDER BY c.updatedAt DESC, c.createdAt DESC")
    List<Conversacion> findAllByUsuarioId(@Param("userId") Long userId);
}