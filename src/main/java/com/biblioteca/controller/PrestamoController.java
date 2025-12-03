package com.biblioteca.controller;

import com.biblioteca.dto.PrestamoRequestDTO;
import com.biblioteca.dto.PrestamoResponseDTO;
import com.biblioteca.service.PrestamoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<PrestamoResponseDTO> registrarPrestamo(
            @Valid @RequestBody PrestamoRequestDTO request) {
        PrestamoResponseDTO prestamo = prestamoService.registrarPrestamo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamo);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<PrestamoResponseDTO> registrarDevolucion(@PathVariable Long id) {
        PrestamoResponseDTO prestamo = prestamoService.registrarDevolucion(id);
        return ResponseEntity.ok(prestamo);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<PrestamoResponseDTO>> listarPrestamosUsuario(
            @PathVariable Long idUsuario) {
        List<PrestamoResponseDTO> prestamos = prestamoService.listarPrestamosUsuario(idUsuario);
        return ResponseEntity.ok(prestamos);
    }

    @GetMapping("/mora")
    public ResponseEntity<List<PrestamoResponseDTO>> listarPrestamosEnMora() {
        List<PrestamoResponseDTO> prestamos = prestamoService.listarPrestamosEnMora();
        return ResponseEntity.ok(prestamos);
    }

    @PostMapping("/calcular-multas")
    public ResponseEntity<String> calcularMultas() {
        prestamoService.calcularMultas();
        return ResponseEntity.ok("Multas calculadas exitosamente");
    }
}
