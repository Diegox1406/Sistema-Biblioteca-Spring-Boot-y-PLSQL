
package com.biblioteca.service;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.BibliotecaException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Libro;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.StoredProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final StoredProcedureRepository storedProcedureRepository;

    @Transactional(readOnly = true)
    public List<LibroDTO> listarLibrosDisponibles() {
        return libroRepository.findByEstadoAndCantidadDisponibleGreaterThan("DISPONIBLE", 0)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LibroDTO obtenerLibroPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new BibliotecaException("Libro no encontrado con ID: " + id));
        return convertirADTO(libro);
    }

    @Transactional
    public LibroDTO registrarLibro(LibroDTO libroDTO) {
        // Validar ISBN único
        if (libroRepository.findByIsbn(libroDTO.getIsbn()).isPresent()) {
            throw new BibliotecaException("El ISBN ya existe en el sistema");
        }

        // Usar procedimiento almacenado de PL/SQL
        storedProcedureRepository.registrarLibroSP(
                libroDTO.getTitulo(),
                libroDTO.getIsbn(),
                libroDTO.getIdAutor(),
                libroDTO.getIdCategoria(),
                libroDTO.getAnioPublicacion(),
                libroDTO.getEditorial(),
                libroDTO.getCantidadTotal()
        );

        // Recuperar el libro registrado
        Libro libro = libroRepository.findByIsbn(libroDTO.getIsbn())
                .orElseThrow(() -> new BibliotecaException("Error al recuperar el libro registrado"));

        return convertirADTO(libro);
    }

    @Transactional(readOnly = true)
    public List<LibroDTO> buscarPorTitulo(String titulo) {
        return libroRepository.buscarPorTitulo(titulo)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private LibroDTO convertirADTO(Libro libro) {
        LibroDTO dto = new LibroDTO();
        dto.setIdLibro(libro.getIdLibro());
        dto.setTitulo(libro.getTitulo());
        dto.setIsbn(libro.getIsbn());
        dto.setIdAutor(libro.getAutor().getIdAutor());
        dto.setNombreAutor(libro.getAutor().getNombre() + " " + libro.getAutor().getApellido());
        dto.setIdCategoria(libro.getCategoria().getIdCategoria());
        dto.setNombreCategoria(libro.getCategoria().getNombre());
        dto.setAnioPublicacion(libro.getAnioPublicacion());
        dto.setEditorial(libro.getEditorial());
        dto.setCantidadTotal(libro.getCantidadTotal());
        dto.setCantidadDisponible(libro.getCantidadDisponible());
        dto.setEstado(libro.getEstado());
        return dto;
    }
}