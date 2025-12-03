package com.biblioteca.service;

import com.biblioteca.dto.PrestamoRequestDTO;
import com.biblioteca.dto.PrestamoResponseDTO;
import com.biblioteca.exception.BibliotecaException;
import com.biblioteca.model.Prestamo;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.StoredProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final StoredProcedureRepository storedProcedureRepository;

    @Transactional
    public PrestamoResponseDTO registrarPrestamo(PrestamoRequestDTO request) {
        try {
            // Usar procedimiento almacenado de PL/SQL
            storedProcedureRepository.registrarPrestamoSP(
                    request.getIdUsuario(),
                    request.getIdLibro(),
                    request.getDiasPrestamo()
            );

            // Obtener el préstamo recién creado
            List<Prestamo> prestamos = prestamoRepository.findByUsuarioIdUsuarioAndEstado(
                    request.getIdUsuario(), "ACTIVO"
            );

            if (prestamos.isEmpty()) {
                throw new BibliotecaException("Error al recuperar el préstamo registrado");
            }

            // Retornar el último préstamo registrado
            Prestamo prestamo = prestamos.get(prestamos.size() - 1);
            return convertirADTO(prestamo);

        } catch (Exception e) {
            throw new BibliotecaException("Error al registrar préstamo: " + e.getMessage());
        }
    }

    @Transactional
    public PrestamoResponseDTO registrarDevolucion(Long idPrestamo) {
        try {
            // Usar procedimiento almacenado de PL/SQL
            storedProcedureRepository.registrarDevolucionSP(idPrestamo);

            // Obtener el préstamo actualizado
            Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                    .orElseThrow(() -> new BibliotecaException("Préstamo no encontrado"));

            return convertirADTO(prestamo);

        } catch (Exception e) {
            throw new BibliotecaException("Error al registrar devolución: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPrestamosUsuario(Long idUsuario) {
        return prestamoRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, "ACTIVO")
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPrestamosEnMora() {
        return prestamoRepository.findPrestamosEnMora(LocalDate.now())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void calcularMultas() {
        storedProcedureRepository.calcularMultasSP();
    }

    private PrestamoResponseDTO convertirADTO(Prestamo prestamo) {
        PrestamoResponseDTO dto = new PrestamoResponseDTO();
        dto.setIdPrestamo(prestamo.getIdPrestamo());
        dto.setNombreUsuario(prestamo.getUsuario().getNombres() + " " +
                prestamo.getUsuario().getApellidos());
        dto.setDniUsuario(prestamo.getUsuario().getDni());
        dto.setTituloLibro(prestamo.getLibro().getTitulo());
        dto.setIsbnLibro(prestamo.getLibro().getIsbn());
        dto.setFechaPrestamo(prestamo.getFechaPrestamo());
        dto.setFechaDevolucionEsperada(prestamo.getFechaDevolucionEsperada());
        dto.setFechaDevolucionReal(prestamo.getFechaDevolucionReal());
        dto.setEstado(prestamo.getEstado());
        dto.setDiasRetraso(prestamo.getDiasRetraso());
        dto.setMulta(prestamo.getMulta());

        // Calcular situación
        if ("ACTIVO".equals(prestamo.getEstado())) {
            if (LocalDate.now().isAfter(prestamo.getFechaDevolucionEsperada())) {
                dto.setSituacion("EN MORA");
            } else {
                dto.setSituacion("AL DÍA");
            }
        } else {
            dto.setSituacion("DEVUELTO");
        }

        return dto;
    }
}