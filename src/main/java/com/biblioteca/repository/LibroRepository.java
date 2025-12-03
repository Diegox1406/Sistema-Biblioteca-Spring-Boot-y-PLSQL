package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIsbn(String isbn);

    List<Libro> findByEstadoAndCantidadDisponibleGreaterThan(String estado, Integer cantidad);

    @Query("SELECT l FROM Libro l WHERE l.autor.idAutor = :idAutor AND l.estado = 'DISPONIBLE'")
    List<Libro> findLibrosByAutor(@Param("idAutor") Long idAutor);

    @Query("SELECT l FROM Libro l WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Libro> buscarPorTitulo(@Param("titulo") String titulo);
}