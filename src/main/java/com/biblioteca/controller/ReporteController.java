package com.biblioteca.controller;

import com.biblioteca.dto.EstadisticasDTO;
import com.biblioteca.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticas() {
        EstadisticasDTO stats = reporteService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }
}