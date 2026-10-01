package com.careconnect.controller;

import com.careconnect.dto.auth.AuthResponseDTO;
import com.careconnect.dto.auth.LoginRequestDTO;
import com.careconnect.dto.auth.RegistroUsuarioDTO;
import com.careconnect.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;

    @Autowired
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDTO> registrar(@Valid @RequestBody RegistroUsuarioDTO dto) {
        AuthResponseDTO response = usuarioService.registrar(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponseDTO response = usuarioService.obtenerPerfilPorEmail(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponseDTO> actualizarPerfil(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponseDTO response = usuarioService.actualizarPerfilPorEmail(authentication.getName(), body);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<AuthResponseDTO> parchearPerfil(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponseDTO response = usuarioService.actualizarPerfilPorEmail(authentication.getName(), body);
        return ResponseEntity.ok(response);
    }
}
