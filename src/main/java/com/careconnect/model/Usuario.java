package com.careconnect.model;

import com.careconnect.model.enums.EstadoUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario extends BaseAuditableEntity implements UserDetails {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String rol;

    @Column(length = 30)
    private String telefono;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_user", nullable = false)
    private EstadoUsuario estadoUser;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRol> roles = new ArrayList<>();

    // ================= METODOS DE USERDETAILS =================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Bloquea el login si el usuario fue sancionado temporalmente
        return this.estadoUser != EstadoUsuario.SUSPENDIDO;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Habilita el acceso si esta activo o pendiente_verificacion, no fue borrado y confirmo su email
        return this.deletedAt == null 
                && (this.estadoUser == EstadoUsuario.ACTIVO || this.estadoUser == EstadoUsuario.PENDIENTE_VERIFICACION) 
                && this.emailVerificado;
    }
}
