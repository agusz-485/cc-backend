package com.careconnect.service.impl;

import com.careconnect.dto.auth.AuthResponseDTO;
import com.careconnect.dto.auth.LoginRequestDTO;
import com.careconnect.dto.auth.RegistroUsuarioDTO;
import com.careconnect.model.*;
import com.careconnect.model.enums.EstadoUsuario;
import com.careconnect.repository.UsuarioRepository;
import com.careconnect.service.JwtService;
import com.careconnect.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository, 
            JwtService jwtService, 
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResponseDTO registrar(RegistroUsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El email ya se encuentra registrado");
        }

        // 1. Normalizar el rol
        String rol = (dto.getRol() != null && !dto.getRol().isBlank()) 
                ? dto.getRol().trim().toUpperCase() 
                : "FAMILIAR";

        // 2. Bloqueo de escalada de privilegios
        if ("ADMIN".equals(rol) || "ADMINISTRADOR".equals(rol)) {
            throw new RuntimeException("No esta permitido registrarse con rol de Administrador");
        }

        // 3. Creacion de entidades permitidas
        Usuario usuario;
        switch (rol) {
            case "CUIDADOR" -> {
                Cuidador cuidador = new Cuidador();
                if (dto.getZonaPrincipal() != null && !dto.getZonaPrincipal().isBlank()) {
                    cuidador.setZonaPrincipal(dto.getZonaPrincipal());
                }
                if (dto.getPrecioHora() != null) {
                    cuidador.setPrecioHora(dto.getPrecioHora());
                }
                cuidador.setDisponible(true);
                usuario = cuidador;
            }
            case "ENFERMERO" -> {
                Enfermero enfermero = new Enfermero();
                String matricula = (dto.getMatriculaProfesional() != null && !dto.getMatriculaProfesional().isBlank())
                        ? dto.getMatriculaProfesional()
                        : "MAT-PENDIENTE";
                enfermero.setMatriculaProfesional(matricula);
                if (dto.getZonaPrincipal() != null && !dto.getZonaPrincipal().isBlank()) {
                    enfermero.setZonaPrincipal(dto.getZonaPrincipal());
                }
                if (dto.getPrecioHora() != null) {
                    enfermero.setPrecioHora(dto.getPrecioHora());
                }
                enfermero.setVisible(true);
                usuario = enfermero;
            }
            case "FAMILIAR" -> usuario = new Familiar();
            default -> throw new RuntimeException("Rol no valido: " + rol);
        }

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEmail(dto.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);

        // 4. REGLA DE NEGOCIO: Cuidadores/Enfermeros nacen en PENDIENTE_VERIFICACION
        if ("CUIDADOR".equals(rol) || "ENFERMERO".equals(rol)) {
            usuario.setEstadoUser(EstadoUsuario.PENDIENTE_VERIFICACION);
        } else {
            usuario.setEstadoUser(EstadoUsuario.ACTIVO);
        }

        usuario.setEmailVerificado(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(usuarioGuardado);

        return AuthResponseDTO.builder()
                .id(usuarioGuardado.getId())
                .nombre(usuarioGuardado.getNombre())
                .email(usuarioGuardado.getEmail())
                .rol(usuarioGuardado.getRol())
                .token(token)
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales invalidas");
        }

        // Bloqueo a cuentas suspendidas
        if (usuario.getEstadoUser() == EstadoUsuario.SUSPENDIDO) {
            throw new RuntimeException("Tu cuenta se encuentra suspendida. Contacta a soporte.");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .token(token)
                .build();
    }

    @Override
    public AuthResponseDTO obtenerPerfilPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        usuario.setEstadoUser(nuevoEstado);
        usuarioRepository.save(usuario);
    }
}
