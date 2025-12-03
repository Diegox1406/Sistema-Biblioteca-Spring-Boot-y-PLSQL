package com.biblioteca.controller;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public ResponseEntity<List<LibroDTO>> listarLibrosDisponibles() {
        List<LibroDTO> libros = libroService.listarLibrosDisponibles();
        return ResponseEntity.ok(libros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroDTO> obtenerLibro(@PathVariable Long id) {
        LibroDTO libro = libroService.obtenerLibroPorId(id);
        return ResponseEntity.ok(libro);
    }

    @PostMapping
    public ResponseEntity<LibroDTO> registrarLibro(@Valid @RequestBody LibroDTO libroDTO) {
        LibroDTO nuevoLibro = libroService.registrarLibro(libroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLibro);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<LibroDTO>> buscarPorTitulo(@RequestParam String titulo) {
        List<LibroDTO> libros = libroService.buscarPorTitulo(titulo);
        return ResponseEntity.ok(libros);
    }
}