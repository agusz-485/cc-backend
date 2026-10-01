package com.careconnect.service.impl;

import com.careconnect.dto.cuidador.CuidadorBusquedaRequestDTO;
import com.careconnect.dto.cuidador.CuidadorPerfilResponseDTO;
import com.careconnect.dto.cuidador.CuidadorPerfilUpdateDTO;
import com.careconnect.model.Cuidador;
import com.careconnect.model.Especialidad;
import com.careconnect.model.Zona;
import com.careconnect.repository.CuidadorRepository;
import com.careconnect.repository.EspecialidadRepository;
import com.careconnect.repository.ZonaRepository;
import com.careconnect.service.CuidadorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.careconnect.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuidadorServiceImpl implements CuidadorService {

    private final CuidadorRepository cuidadorRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ZonaRepository zonaRepository;

    public CuidadorServiceImpl(CuidadorRepository cuidadorRepository,
                                EspecialidadRepository especialidadRepository,
                                ZonaRepository zonaRepository) {
        this.cuidadorRepository = cuidadorRepository;
        this.especialidadRepository = especialidadRepository;
        this.zonaRepository = zonaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuidadorPerfilResponseDTO> buscar(CuidadorBusquedaRequestDTO filtro) {
        return cuidadorRepository.buscarConFiltros(
                        filtro.getZonaId(),
                        filtro.getEspecialidadId(),
                        filtro.getPrecioMin(),
                        filtro.getPrecioMax(),
                        filtro.getDisponible())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CuidadorPerfilResponseDTO obtenerPerfil(Long id) {
        return toResponseDTO(buscarCuidadorOFallar(id));
    }

    @Override
    @Transactional
    public CuidadorPerfilResponseDTO actualizarPerfil(Long id, CuidadorPerfilUpdateDTO dto) {
        Cuidador cuidador = buscarCuidadorOFallar(id);

        if (dto.getFotoPerfil() != null) {
            cuidador.setFotoPerfil(dto.getFotoPerfil());
        }
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            cuidador.setNombre(dto.getNombre());
        }
        if (dto.getApellido() != null && !dto.getApellido().isBlank()) {
            cuidador.setApellido(dto.getApellido());
        }
        if (dto.getTelefono() != null) {
            cuidador.setTelefono(dto.getTelefono());
        }
        if (dto.getDescripcion() != null) {
            cuidador.setDescripcion(dto.getDescripcion());
        }
        if (dto.getAniosExperiencia() != null) {
            cuidador.setAniosExperiencia(dto.getAniosExperiencia());
        }
        if (dto.getZonaPrincipal() != null) {
            cuidador.setZonaPrincipal(dto.getZonaPrincipal());
        }
        if (dto.getPrecioHora() != null) {
            cuidador.setPrecioHora(dto.getPrecioHora());
        }
        if (dto.getDisponible() != null) {
            cuidador.setDisponible(dto.getDisponible());
        }
        if (dto.getEspecialidadesIds() != null) {
            List<Especialidad> especialidades = especialidadRepository.findAllById(dto.getEspecialidadesIds());
            cuidador.setEspecialidades(especialidades);
        }
        if (dto.getZonasCoberturaIds() != null) {
            List<Zona> zonas = zonaRepository.findAllById(dto.getZonasCoberturaIds());
            cuidador.setZonasCobertura(zonas);
        }

        return toResponseDTO(cuidadorRepository.save(cuidador));
    }

    @Override
    @Transactional
    public CuidadorPerfilResponseDTO cambiarDisponibilidad(Long id, boolean disponible) {
        Cuidador cuidador = buscarCuidadorOFallar(id);
        cuidador.setDisponible(disponible);
        return toResponseDTO(cuidadorRepository.save(cuidador));
    }

    private Cuidador buscarCuidadorOFallar(Long id) {
        return cuidadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado con id: " + id));
    }

    private CuidadorPerfilResponseDTO toResponseDTO(Cuidador c) {
        return CuidadorPerfilResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .apellido(c.getApellido())
                .email(c.getEmail())
                .telefono(c.getTelefono())
                .fotoPerfil(c.getFotoPerfil())
                .descripcion(c.getDescripcion())
                .aniosExperiencia(c.getAniosExperiencia())
                .zonaPrincipal(c.getZonaPrincipal())
                .precioHora(c.getPrecioHora())
                .disponible(c.isDisponible())
                .especialidades(c.getEspecialidades().stream()
                        .map(Especialidad::getNomEspecialidad)
                        .collect(Collectors.toList()))
                .zonasCobertura(c.getZonasCobertura().stream()
                        .map(Zona::getZona)
                        .collect(Collectors.toList()))
                .build();
    }
}
