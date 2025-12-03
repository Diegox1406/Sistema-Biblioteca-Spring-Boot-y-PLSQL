package com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    private Long totalLibros;
    private Long totalUsuarios;
    private Long prestamosActivos;
    private Long prestamosMora;
    private BigDecimal totalMultas;
}