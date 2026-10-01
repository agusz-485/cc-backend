package com.careconnect.service.impl;

import com.careconnect.dto.turno.TurnoCambioEstadoDTO;
import com.careconnect.dto.turno.TurnoRequestDTO;
import com.careconnect.dto.turno.TurnoResponseDTO;
import com.careconnect.exception.ResourceNotFoundException;
import com.careconnect.model.*;
import com.careconnect.model.enums.EstadoTurno;
import com.careconnect.repository.*;
import com.careconnect.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final FamiliarRepository familiarRepository;
    private final CuidadorRepository cuidadorRepository;
    private final AdultoMayorRepository adultoMayorRepository;
    private final TipoServicioRepository tipoServicioRepository;

    @Override
    @Transactional
    public TurnoResponseDTO crear(TurnoRequestDTO dto) {
        Familiar familiar = familiarRepository.findById(dto.getFamiliarId())
                .orElseThrow(() -> new ResourceNotFoundException("Familiar no encontrado con ID: " + dto.getFamiliarId()));

        Cuidador cuidador = cuidadorRepository.findById(dto.getCuidadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado con ID: " + dto.getCuidadorId()));

        AdultoMayor adultoMayor = adultoMayorRepository.findById(dto.getAdultoMayorId())
                .orElseThrow(() -> new ResourceNotFoundException("Adulto Mayor no encontrado con ID: " + dto.getAdultoMayorId()));

        TipoServicio tipoServicio = null;
        if (dto.getTipoServicioId() != null) {
            tipoServicio = tipoServicioRepository.findById(dto.getTipoServicioId()).orElse(null);
        }
        if (tipoServicio == null && dto.getTipoServicioNombre() != null && !dto.getTipoServicioNombre().isBlank()) {
            tipoServicio = tipoServicioRepository.findByNombreServicioIgnoreCase(dto.getTipoServicioNombre())
                    .orElseGet(() -> tipoServicioRepository.save(TipoServicio.builder()
                            .nombreServicio(dto.getTipoServicioNombre())
                            .descripcionServicio("Servicio de cuidado general")
                            .build()));
        }
        if (tipoServicio == null) {
            tipoServicio = tipoServicioRepository.findAll().stream().findFirst()
                    .orElseGet(() -> tipoServicioRepository.save(TipoServicio.builder()
                            .nombreServicio("Cuidado Integral")
                            .descripcionServicio("Acompanamiento y asistencia general")
                            .build()));
        }

        LocalTime horaInicio = dto.getHoraInicio() != null ? dto.getHoraInicio() : LocalTime.of(8, 0);
        LocalTime horaFin = dto.getHoraFin() != null ? dto.getHoraFin() : LocalTime.of(16, 0);
        BigDecimal precio = dto.getPrecioTotal() != null ? dto.getPrecioTotal() : BigDecimal.valueOf(10000.0);

        Turno turno = Turno.builder()
                .familiar(familiar)
                .cuidador(cuidador)
                .adultoMayor(adultoMayor)
                .tipoServicio(tipoServicio)
                .fecha(dto.getFecha())
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .duracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 480)
                .estadoTurno(EstadoTurno.PENDIENTE)
                .descripcionServicio(dto.getDescripcionServicio() != null ? dto.getDescripcionServicio() : "Cuidado personalizado")
                .precioTotal(precio)
                .build();

        Turno guardado = turnoRepository.save(turno);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponseDTO buscarPorId(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + id));
        return mapToDTO(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> buscarPorFamiliar(Long familiarId) {
        return turnoRepository.findByFamiliarId(familiarId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> buscarPorCuidador(Long cuidadorId) {
        return turnoRepository.findByCuidadorId(cuidadorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> buscarTodos() {
        return turnoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TurnoResponseDTO cambiarEstado(Long id, TurnoCambioEstadoDTO dto) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + id));

        turno.setEstadoTurno(dto.getEstado());
        if (dto.getMotivoCancelacion() != null) {
            turno.setMotivoCancelacion(dto.getMotivoCancelacion());
        }

        Turno actualizado = turnoRepository.save(turno);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!turnoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Turno no encontrado con ID: " + id);
        }
        turnoRepository.deleteById(id);
    }

    private TurnoResponseDTO mapToDTO(Turno turno) {
        String familiarNombre = (turno.getFamiliar() != null) 
                ? (turno.getFamiliar().getNombre() + " " + turno.getFamiliar().getApellido()).trim()
                : "Familiar";
        String familiarFoto = (turno.getFamiliar() != null) ? turno.getFamiliar().getFotoPerfil() : null;
        String familiarTelefono = (turno.getFamiliar() != null) ? turno.getFamiliar().getTelefono() : null;
        String direccion = (turno.getFamiliar() != null && turno.getFamiliar().getZona() != null)
                ? turno.getFamiliar().getZona()
                : null;

        String cuidadorNombre = (turno.getCuidador() != null)
                ? (turno.getCuidador().getNombre() + " " + turno.getCuidador().getApellido()).trim()
                : "Cuidador";
        String cuidadorFoto = (turno.getCuidador() != null) ? turno.getCuidador().getFotoPerfil() : null;
        String cuidadorTelefono = (turno.getCuidador() != null) ? turno.getCuidador().getTelefono() : null;

        String adultoMayorNombre = (turno.getAdultoMayor() != null)
                ? (turno.getAdultoMayor().getNombre() + " " + turno.getAdultoMayor().getApellido()).trim()
                : "Adulto Mayor";

        String tipoServicio = (turno.getTipoServicio() != null) 
                ? turno.getTipoServicio().getNombreServicio() 
                : "Cuidado";

        return TurnoResponseDTO.builder()
                .id(turno.getId())
                .familiarId(turno.getFamiliar() != null ? turno.getFamiliar().getId() : null)
                .familiarNombre(familiarNombre)
                .familiarFoto(familiarFoto)
                .familiarTelefono(familiarTelefono)
                .direccion(direccion)
                .cuidadorId(turno.getCuidador() != null ? turno.getCuidador().getId() : null)
                .cuidadorNombre(cuidadorNombre)
                .cuidadorFoto(cuidadorFoto)
                .cuidadorTelefono(cuidadorTelefono)
                .adultoMayorId(turno.getAdultoMayor() != null ? turno.getAdultoMayor().getId() : null)
                .adultoMayorNombre(adultoMayorNombre)
                .fecha(turno.getFecha())
                .horaInicio(turno.getHoraInicio())
                .horaFin(turno.getHoraFin())
                .duracionMinutos(turno.getDuracionMinutos())
                .estadoTurno(turno.getEstadoTurno())
                .tipoServicio(tipoServicio)
                .descripcionServicio(turno.getDescripcionServicio())
                .precioTotal(turno.getPrecioTotal())
                .timestampInicio(turno.getTimestampInicio())
                .motivoCancelacion(turno.getMotivoCancelacion())
                .build();
    }
}
