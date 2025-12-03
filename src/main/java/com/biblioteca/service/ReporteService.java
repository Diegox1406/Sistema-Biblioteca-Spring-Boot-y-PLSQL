package com.biblioteca.service;

import com.biblioteca.dto.EstadisticasDTO;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional(readOnly = true)
    public EstadisticasDTO obtenerEstadisticas() {
        EstadisticasDTO stats = new EstadisticasDTO();

        // Total de libros disponibles
        stats.setTotalLibros(libroRepository.count());

        // Total de usuarios activos
        stats.setTotalUsuarios(usuarioRepository.count());

        // Préstamos activos
        Long prestamosActivos = prestamoRepository.findAll()
                .stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .count();
        stats.setPrestamosActivos(prestamosActivos);

        // Préstamos en mora
        Long prestamosMora = prestamoRepository.findPrestamosEnMora(LocalDate.now())
                .size();
        stats.setPrestamosMora((long) prestamosMora);

        // Total de multas pendientes
        BigDecimal totalMultas = prestamoRepository.findAll()
                .stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()) && p.getMulta() != null)
                .map(p -> p.getMulta())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalMultas(totalMultas);

        return stats;
    }
}