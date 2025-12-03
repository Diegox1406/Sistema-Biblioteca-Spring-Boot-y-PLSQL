package com.biblioteca.repository;

import com.biblioteca.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioIdUsuarioAndEstado(Long idUsuario, String estado);

    @Query("SELECT p FROM Prestamo p WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionEsperada < :fecha")
    List<Prestamo> findPrestamosEnMora(@Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.idUsuario = :idUsuario AND p.estado = 'ACTIVO'")
    Long contarPrestamosActivos(@Param("idUsuario") Long idUsuario);
}