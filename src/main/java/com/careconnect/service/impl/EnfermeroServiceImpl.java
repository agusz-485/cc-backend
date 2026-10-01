package com.careconnect.service.impl;

import com.careconnect.dto.enfermero.EnfermeroBusquedaRequestDTO;
import com.careconnect.dto.enfermero.EnfermeroPerfilResponseDTO;
import com.careconnect.dto.enfermero.EnfermeroPerfilUpdateDTO;
import com.careconnect.exception.ResourceNotFoundException;
import com.careconnect.model.Enfermero;
import com.careconnect.repository.EnfermeroRepository;
import com.careconnect.service.EnfermeroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnfermeroServiceImpl implements EnfermeroService {

    private final EnfermeroRepository enfermeroRepository;

    public EnfermeroServiceImpl(EnfermeroRepository enfermeroRepository) {
        this.enfermeroRepository = enfermeroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnfermeroPerfilResponseDTO> buscar(EnfermeroBusquedaRequestDTO filtro) {
        return enfermeroRepository.buscarConFiltros(
                        filtro.getZonaPrincipal(),
                        filtro.getPrecioMin(),
                        filtro.getPrecioMax(),
                        filtro.getVisible())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnfermeroPerfilResponseDTO obtenerPerfil(Long id) {
        return toResponseDTO(buscarEnfermeroOFallar(id));
    }

    @Override
    @Transactional
    public EnfermeroPerfilResponseDTO actualizarPerfil(Long id, EnfermeroPerfilUpdateDTO dto) {
        Enfermero enfermero = buscarEnfermeroOFallar(id);

        if (dto.getFotoPerfil() != null) {
            enfermero.setFotoPerfil(dto.getFotoPerfil());
        }
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            enfermero.setNombre(dto.getNombre());
        }
        if (dto.getApellido() != null && !dto.getApellido().isBlank()) {
            enfermero.setApellido(dto.getApellido());
        }
        if (dto.getTelefono() != null) {
            enfermero.setTelefono(dto.getTelefono());
        }
        if (dto.getMatriculaProfesional() != null) {
            enfermero.setMatriculaProfesional(dto.getMatriculaProfesional());
        }
        if (dto.getDescripcion() != null) {
            enfermero.setDescripcion(dto.getDescripcion());
        }
        if (dto.getAniosExperiencia() != null) {
            enfermero.setAniosExperiencia(dto.getAniosExperiencia());
        }
        if (dto.getZonaPrincipal() != null) {
            enfermero.setZonaPrincipal(dto.getZonaPrincipal());
        }
        if (dto.getPrecioHora() != null) {
            enfermero.setPrecioHora(dto.getPrecioHora());
        }
        if (dto.getVisible() != null) {
            enfermero.setVisible(dto.getVisible());
        }

        return toResponseDTO(enfermeroRepository.save(enfermero));
    }

    private Enfermero buscarEnfermeroOFallar(Long id) {
        return enfermeroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enfermero no encontrado con id: " + id));
    }

    private EnfermeroPerfilResponseDTO toResponseDTO(Enfermero e) {
        return EnfermeroPerfilResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .apellido(e.getApellido())
                .email(e.getEmail())
                .telefono(e.getTelefono())
                .fotoPerfil(e.getFotoPerfil())
                .matriculaProfesional(e.getMatriculaProfesional())
                .descripcion(e.getDescripcion())
                .aniosExperiencia(e.getAniosExperiencia())
                .zonaPrincipal(e.getZonaPrincipal())
                .precioHora(e.getPrecioHora())
                .visible(e.isVisible())
                .build();
    }
}
