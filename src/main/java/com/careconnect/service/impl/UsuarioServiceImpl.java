package com.careconnect.service.impl;

import com.careconnect.dto.auth.AuthResponseDTO;
import com.careconnect.dto.auth.LoginRequestDTO;
import com.careconnect.dto.auth.RegistroUsuarioDTO;
import com.careconnect.exception.ResourceNotFoundException;
import com.careconnect.model.*;
import com.careconnect.model.enums.EstadoUsuario;
import com.careconnect.repository.UsuarioRepository;
import com.careconnect.service.JwtService;
import com.careconnect.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
        String emailTrimmed = dto.getEmail() != null ? dto.getEmail().trim() : "";
        if (usuarioRepository.existsByEmail(emailTrimmed)) {
            throw new RuntimeException("El email ya se encuentra registrado");
        }

        String rol = (dto.getRol() != null && !dto.getRol().isBlank()) 
                ? dto.getRol().trim().toUpperCase() 
                : "FAMILIAR";

        if ("ADMIN".equals(rol) || "ADMINISTRADOR".equals(rol)) {
            throw new RuntimeException("No esta permitido registrarse con rol de Administrador");
        }

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
        usuario.setEmail(emailTrimmed);
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);

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
                .apellido(usuarioGuardado.getApellido())
                .email(usuarioGuardado.getEmail())
                .telefono(usuarioGuardado.getTelefono())
                .fotoPerfil(usuarioGuardado.getFotoPerfil())
                .rol(usuarioGuardado.getRol())
                .token(token)
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String emailTrimmed = dto.getEmail() != null ? dto.getEmail().trim() : "";
        Usuario usuario = usuarioRepository.findByEmail(emailTrimmed)
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas. Verificá email y contraseña."));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales incorrectas. Verificá email y contraseña.");
        }

        if (usuario.getEstadoUser() == EstadoUsuario.SUSPENDIDO) {
            throw new RuntimeException("Tu cuenta se encuentra suspendida. Contacta a soporte.");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .fotoPerfil(usuario.getFotoPerfil())
                .rol(usuario.getRol())
                .token(token)
                .build();
    }

    @Override
    public AuthResponseDTO obtenerPerfilPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .fotoPerfil(usuario.getFotoPerfil())
                .rol(usuario.getRol())
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO actualizarPerfilPorEmail(String email, Map<String, Object> body) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        if (body.containsKey("fotoPerfil") && body.get("fotoPerfil") != null) {
            usuario.setFotoPerfil(String.valueOf(body.get("fotoPerfil")));
        } else if (body.containsKey("fotoUrl") && body.get("fotoUrl") != null) {
            usuario.setFotoPerfil(String.valueOf(body.get("fotoUrl")));
        }

        if (body.containsKey("nombre") && body.get("nombre") != null && !String.valueOf(body.get("nombre")).isBlank()) {
            usuario.setNombre(String.valueOf(body.get("nombre")));
        }
        if (body.containsKey("apellido") && body.get("apellido") != null && !String.valueOf(body.get("apellido")).isBlank()) {
            usuario.setApellido(String.valueOf(body.get("apellido")));
        }
        if (body.containsKey("telefono") && body.get("telefono") != null) {
            usuario.setTelefono(String.valueOf(body.get("telefono")));
        }

        Usuario guardado = usuarioRepository.save(usuario);

        return AuthResponseDTO.builder()
                .id(guardado.getId())
                .nombre(guardado.getNombre())
                .apellido(guardado.getApellido())
                .email(guardado.getEmail())
                .telefono(guardado.getTelefono())
                .fotoPerfil(guardado.getFotoPerfil())
                .rol(guardado.getRol())
                .build();
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setEstadoUser(nuevoEstado);
        usuarioRepository.save(usuario);
    }
}