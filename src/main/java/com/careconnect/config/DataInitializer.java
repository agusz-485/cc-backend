package com.careconnect.config;

import com.careconnect.model.*;
import com.careconnect.model.enums.EstadoUsuario;
import com.careconnect.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository,
            CuidadorRepository cuidadorRepository,
            EnfermeroRepository enfermeroRepository,
            EspecialidadRepository especialidadRepository,
            ZonaRepository zonaRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        return args -> {
            // 1. Inicializar / Actualizar Admin
            initAdmin(usuarioRepository, passwordEncoder);

            // 2. Poblar Zonas y Especialidades
            Map<String, Especialidad> especialidadesMap = initEspecialidades(especialidadRepository);
            Map<String, Zona> zonasMap = initZonas(zonaRepository);

            // 3. Limpieza de Cuidadores y Enfermeros de prueba/corruptos
            limpiarCuidadoresDePrueba(jdbcTemplate);

            // 4. Crear o Actualizar Perfiles Reales de Cuidadores y Enfermeros
            initPerfilesReales(cuidadorRepository, enfermeroRepository, especialidadesMap, zonasMap, passwordEncoder);
        };
    }

    private void initAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
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
            System.out.println(">>> [INIT] ADMIN CREADO: " + adminEmail);
        } else {
            existingAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
            existingAdmin.setEstadoUser(EstadoUsuario.ACTIVO);
            existingAdmin.setRol("ADMIN");
            usuarioRepository.save(existingAdmin);
            System.out.println(">>> [INIT] ADMIN ACTUALIZADO: " + adminEmail);
        }
    }

    private Map<String, Especialidad> initEspecialidades(EspecialidadRepository repo) {
        List<String> nombres = List.of(
                "Alzheimer",
                "Parkinson",
                "Post-operatorio",
                "Rehabilitacion",
                "Cuidados Paliativos",
                "Acompanamiento",
                "Estimulacion Cognitiva",
                "Movilidad Reducida",
                "Higiene y Confort",
                "Control de Signos Vitales"
        );
        Map<String, Especialidad> map = new HashMap<>();
        List<Especialidad> existentes = repo.findAll();
        for (Especialidad e : existentes) {
            map.put(e.getNomEspecialidad(), e);
        }
        for (String nom : nombres) {
            if (!map.containsKey(nom)) {
                Especialidad nueva = Especialidad.builder().nomEspecialidad(nom).build();
                Especialidad guardada = repo.save(nueva);
                map.put(nom, guardada);
                System.out.println(">>> [INIT] Especialidad creada: " + nom);
            }
        }
        return map;
    }

    private Map<String, Zona> initZonas(ZonaRepository repo) {
        List<String> nombres = List.of(
                "Palermo",
                "Belgrano",
                "Recoleta",
                "Caballito",
                "Almagro",
                "San Telmo",
                "San Isidro",
                "Vicente Lopez",
                "Villa Urquiza",
                "Villa Crespo",
                "Lanus",
                "Quilmes"
        );
        Map<String, Zona> map = new HashMap<>();
        List<Zona> existentes = repo.findAll();
        for (Zona z : existentes) {
            map.put(z.getZona(), z);
        }
        for (String nom : nombres) {
            if (!map.containsKey(nom)) {
                Zona nueva = Zona.builder().zona(nom).build();
                Zona guardada = repo.save(nueva);
                map.put(nom, guardada);
                System.out.println(">>> [INIT] Zona creada: " + nom);
            }
        }
        return map;
    }

    private void limpiarCuidadoresDePrueba(JdbcTemplate jdbc) {
        try {
            // IDs de usuarios de prueba / corruptos que queremos eliminar
            List<Long> idsAEliminar = jdbc.queryForList(
                    "SELECT id FROM usuarios WHERE email LIKE '%@careconnect.test' OR email = 'Alberto@gmail.com' OR email = 'cuidador.a.moderar.1790961650678@careconnect.test'",
                    Long.class
            );

            if (idsAEliminar.isEmpty()) {
                System.out.println(">>> [INIT] No hay cuidadores de prueba antiguos para limpiar.");
                return;
            }

            System.out.println(">>> [INIT] Limpiando usuarios de prueba obsoletos: " + idsAEliminar);

            for (Long id : idsAEliminar) {
                try {
                    // Limpiar pagos vinculados a turnos de este cuidador
                    jdbc.update("DELETE FROM pagos WHERE turno_id IN (SELECT id FROM turnos WHERE cuidador_id = ?)", id);
                } catch (Exception e) {}
                try {
                    // Limpiar resenas y resenias
                    jdbc.update("DELETE FROM resenas WHERE turno_id IN (SELECT id FROM turnos WHERE cuidador_id = ?)", id);
                    jdbc.update("DELETE FROM resenias WHERE cuidador_id = ? OR autor_id = ?", id, id);
                } catch (Exception e) {}
                try {
                    // Limpiar reportes
                    jdbc.update("DELETE FROM reportes WHERE reportado_id = ? OR reportante_id = ?", id, id);
                } catch (Exception e) {}
                try {
                    // Limpiar mensajes y conversaciones
                    jdbc.update("DELETE FROM mensajes WHERE remitente_id = ?", id);
                    jdbc.update("DELETE FROM conversaciones WHERE usuario1_id = ? OR usuario2_id = ?", id, id);
                } catch (Exception e) {}
                try {
                    // Limpiar turnos
                    jdbc.update("DELETE FROM turnos WHERE cuidador_id = ?", id);
                } catch (Exception e) {}
                try {
                    // Limpiar tablas intermedias de cuidador
                    jdbc.update("DELETE FROM cuidador_especialidades WHERE cuidador_id = ?", id);
                    jdbc.update("DELETE FROM cuidador_zonas WHERE cuidador_id = ?", id);
                    jdbc.update("DELETE FROM certificaciones WHERE cuidador_id = ?", id);
                    jdbc.update("DELETE FROM user_roles WHERE usuario_id = ?", id);
                    // Limpiar cuidador / enfermero
                    jdbc.update("DELETE FROM cuidadores WHERE id = ?", id);
                    jdbc.update("DELETE FROM enfermeros WHERE usuario_id = ?", id);
                    // Limpiar usuario principal
                    jdbc.update("DELETE FROM usuarios WHERE id = ?", id);
                } catch (Exception e) {
                    System.err.println(">>> [INIT] Error al eliminar usuario ID " + id + ": " + e.getMessage());
                }
            }
            System.out.println(">>> [INIT] Limpieza de usuarios de prueba completada exitosamente.");
        } catch (Exception e) {
            System.err.println(">>> [INIT] Advertencia durante limpieza de cuidadores de prueba: " + e.getMessage());
        }
    }

    private void initPerfilesReales(
            CuidadorRepository cuidadorRepo,
            EnfermeroRepository enfermeroRepo,
            Map<String, Especialidad> espMap,
            Map<String, Zona> zonMap,
            PasswordEncoder passwordEncoder) {

        // ==========================================
        // 1. MARTÍN ALEJANDRO GÓMEZ (Cuidador)
        // ==========================================
        String emailMartin = "martin.gomez@careconnect.com";
        Cuidador martin = cuidadorRepo.findAll().stream()
                .filter(c -> emailMartin.equalsIgnoreCase(c.getEmail()))
                .findFirst().orElse(null);

        if (martin == null) {
            martin = new Cuidador();
            martin.setEmail(emailMartin);
        }
        martin.setNombre("Martin Alejandro");
        martin.setApellido("Gomez");
        martin.setPasswordHash(passwordEncoder.encode("MartinCare2026!"));
        martin.setRol("CUIDADOR");
        martin.setTelefono("+54 11 5932-1188");
        martin.setEstadoUser(EstadoUsuario.ACTIVO);
        martin.setEmailVerificado(true);
        martin.setZonaPrincipal("Caballito");
        martin.setAniosExperiencia(5);
        martin.setPrecioHora(new BigDecimal("4800.00"));
        martin.setDisponible(true);
        martin.setDescripcion(
                "Acompañante Terapéutico recibido con especialización en Gerontología y Neurorehabilitación. " +
                "Cuento con 5 años de experiencia asistiendo a adultos mayores con demencias, Alzheimer y Parkinson. " +
                "Enfoque centrado en la estimulación cognitiva a través de ejercicios lúdicos, acompañamiento en paseos seguros, " +
                "supervisión y asistencia en las actividades de la vida diaria (higiene, confort y alimentación), y contención emocional continua."
        );

        martin.setEspecialidades(getEspecialidades(espMap, "Alzheimer", "Parkinson", "Acompanamiento", "Estimulacion Cognitiva"));
        martin.setZonasCobertura(getZonas(zonMap, "Caballito", "Almagro", "Villa Urquiza", "Palermo"));

        List<Certificacion> certsMartin = new ArrayList<>();
        certsMartin.add(Certificacion.builder()
                .descripcion("Titulo Oficial de Acompanante Terapeutico - Cruz Roja Argentina")
                .urlCertificado("https://careconnect.com/certs/at-martin-gomez.pdf")
                .validoHasta(LocalDateTime.now().plusYears(4))
                .activo(true)
                .build());
        certsMartin.add(Certificacion.builder()
                .descripcion("Certificacion en Cuidados Gerontologicos y Neurodemencias")
                .urlCertificado("https://careconnect.com/certs/gerontologia-martin.pdf")
                .validoHasta(LocalDateTime.now().plusYears(3))
                .activo(true)
                .build());
        martin.setCertificaciones(certsMartin);

        cuidadorRepo.save(martin);
        System.out.println(">>> [INIT] Perfil CUIDADOR creado/actualizado: " + emailMartin);

        // ==========================================
        // 2. VALENTINA ROCÍO MORALES (Cuidador)
        // ==========================================
        String emailValentina = "valentina.morales@careconnect.com";
        Cuidador valentina = cuidadorRepo.findAll().stream()
                .filter(c -> emailValentina.equalsIgnoreCase(c.getEmail()))
                .findFirst().orElse(null);

        if (valentina == null) {
            valentina = new Cuidador();
            valentina.setEmail(emailValentina);
        }
        valentina.setNombre("Valentina Rocio");
        valentina.setApellido("Morales");
        valentina.setPasswordHash(passwordEncoder.encode("ValentinaCare2026!"));
        valentina.setRol("CUIDADOR");
        valentina.setTelefono("+54 11 6744-8822");
        valentina.setEstadoUser(EstadoUsuario.ACTIVO);
        valentina.setEmailVerificado(true);
        valentina.setZonaPrincipal("Belgrano");
        valentina.setAniosExperiencia(6);
        valentina.setPrecioHora(new BigDecimal("5200.00"));
        valentina.setDisponible(true);
        valentina.setDescripcion(
                "Cuidadora profesional certificada con 6 años de experiencia en asistencia integral domiciliaria e internación domiciliaria. " +
                "Capacitada en técnicas de movilización de pacientes con movilidad reducida o postrados, prevención activa de caídas y úlceras por presión, " +
                "control estricto de medicación prescrita y preparación de dietas según indicación médica. Disponibilidad para guardias diurnas, nocturnas y fines de semana."
        );

        valentina.setEspecialidades(getEspecialidades(espMap, "Post-operatorio", "Rehabilitacion", "Acompanamiento", "Movilidad Reducida", "Higiene y Confort"));
        valentina.setZonasCobertura(getZonas(zonMap, "Belgrano", "Recoleta", "San Isidro", "Palermo", "Vicente Lopez"));

        List<Certificacion> certsValentina = new ArrayList<>();
        certsValentina.add(Certificacion.builder()
                .descripcion("Diplomatura en Asistencia y Cuidados de Adultos Mayores")
                .urlCertificado("https://careconnect.com/certs/cuidadora-valentina.pdf")
                .validoHasta(LocalDateTime.now().plusYears(3))
                .activo(true)
                .build());
        certsValentina.add(Certificacion.builder()
                .descripcion("Curso Avanzado de Primeros Auxilios y RCP - Cruz Roja")
                .urlCertificado("https://careconnect.com/certs/rcp-valentina-morales.pdf")
                .validoHasta(LocalDateTime.now().plusYears(2))
                .activo(true)
                .build());
        valentina.setCertificaciones(certsValentina);

        cuidadorRepo.save(valentina);
        System.out.println(">>> [INIT] Perfil CUIDADOR creado/actualizado: " + emailValentina);

        // ==========================================
        // 3. LIC. SOFÍA BELÉN ALBARRACÍN (Enfermera)
        // ==========================================
        String emailSofia = "sofia.albarracin@careconnect.com";
        Enfermero sofia = enfermeroRepo.findAll().stream()
                .filter(e -> emailSofia.equalsIgnoreCase(e.getEmail()))
                .findFirst().orElse(null);

        if (sofia == null) {
            sofia = new Enfermero();
            sofia.setEmail(emailSofia);
        }
        sofia.setNombre("Sofia Belen");
        sofia.setApellido("Albarracin");
        sofia.setPasswordHash(passwordEncoder.encode("SofiaCare2026!"));
        sofia.setRol("ENFERMERO");
        sofia.setTelefono("+54 11 4821-9304");
        sofia.setEstadoUser(EstadoUsuario.ACTIVO);
        sofia.setEmailVerificado(true);
        sofia.setMatriculaProfesional("MN-84921");
        sofia.setZonaPrincipal("Palermo");
        sofia.setAniosExperiencia(7);
        sofia.setPrecioHora(new BigDecimal("6500.00"));
        sofia.setVisible(true);
        sofia.setDescripcion(
                "Licenciada en Enfermería egresada de la UBA (Matrícula Nacional MN-84921) con más de 7 años de trayectoria en cuidados intensivos y atención domiciliaria integral. " +
                "Especializada en cuidados postquirúrgicos, control exhaustivo de signos vitales, administración segura de medicación por vía endovenosa/subcutánea, " +
                "manejo de sondas vesicales y nasogástricas, y curación avanzada de heridas y escaras. Trato empático, respetuoso y de máxima calidez humana."
        );

        enfermeroRepo.save(sofia);
        System.out.println(">>> [INIT] Perfil ENFERMERO creado/actualizado: " + emailSofia);

        // ==========================================
        // 4. LIC. CLARA INÉS BENÍTEZ (Enfermera)
        // ==========================================
        String emailClara = "clara.benitez@careconnect.com";
        Enfermero clara = enfermeroRepo.findAll().stream()
                .filter(e -> emailClara.equalsIgnoreCase(e.getEmail()))
                .findFirst().orElse(null);

        if (clara == null) {
            clara = new Enfermero();
            clara.setEmail(emailClara);
        }
        clara.setNombre("Clara Ines");
        clara.setApellido("Benitez");
        clara.setPasswordHash(passwordEncoder.encode("ClaraCare2026!"));
        clara.setRol("ENFERMERO");
        clara.setTelefono("+54 11 3320-7711");
        clara.setEstadoUser(EstadoUsuario.ACTIVO);
        clara.setEmailVerificado(true);
        clara.setMatriculaProfesional("MP-43209");
        clara.setZonaPrincipal("Recoleta");
        clara.setAniosExperiencia(9);
        clara.setPrecioHora(new BigDecimal("7000.00"));
        clara.setVisible(true);
        clara.setDescripcion(
                "Enfermera Universitaria (Matrícula Provincial MP-43209) con 9 años de experiencia hospitalaria y domiciliaria, con posgrado en Cuidados Paliativos y Manejo del Dolor. " +
                "Especializada en atención a pacientes oncológicos y crónicos, oxigenoterapia, aspiración de secreciones, manejo de traqueostomías, " +
                "alimentación enteral y parenteral, y monitoreo hemodinámico. Compromiso absoluto con el bienestar físico y el confort del paciente."
        );

        enfermeroRepo.save(clara);
        System.out.println(">>> [INIT] Perfil ENFERMERO creado/actualizado: " + emailClara);
    }

    private List<Especialidad> getEspecialidades(Map<String, Especialidad> map, String... names) {
        List<Especialidad> list = new ArrayList<>();
        for (String n : names) {
            if (map.containsKey(n)) {
                list.add(map.get(n));
            }
        }
        return list;
    }

    private List<Zona> getZonas(Map<String, Zona> map, String... names) {
        List<Zona> list = new ArrayList<>();
        for (String n : names) {
            if (map.containsKey(n)) {
                list.add(map.get(n));
            }
        }
        return list;
    }
}