package com.careconnect.config;

import com.careconnect.model.Administrador;
import com.careconnect.model.Usuario;
import com.careconnect.model.enums.EstadoUsuario;
import com.careconnect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nombre}")
    private String adminNombre;

    @Value("${app.admin.apellido}")
    private String adminApellido;

    @Bean
    public CommandLineRunner initDefaultAdmin(
            UsuarioRepository usuarioRepository, 
            PasswordEncoder passwordEncoder) {
        return args -> {
            Usuario existingAdmin = usuarioRepository.findByEmail(adminEmail).orElse(null);
            if (existingAdmin == null) {
                Administrador admin = new Administrador();
                admin.setNombre(adminNombre);
                admin.setApellido(adminApellido);
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRol("ADMIN");
                admin.setEstadoUser(EstadoUsuario.ACTIVO);
                admin.setEmailVerificado(true);

                usuarioRepository.save(admin);
                System.out.println(">>> ADMIN CREADO: " + adminEmail);
            } else {
                existingAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
                existingAdmin.setEstadoUser(EstadoUsuario.ACTIVO);
                existingAdmin.setRol("ADMIN");
                usuarioRepository.save(existingAdmin);
                System.out.println(">>> ADMIN ACTUALIZADO CON NUEVA CLAVE: " + adminEmail);
            }
        };
    }
}
