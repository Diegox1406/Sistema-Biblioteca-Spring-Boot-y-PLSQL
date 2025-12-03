package com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoResponseDTO {

    private Long idPrestamo;
    private String nombreUsuario;
    private String dniUsuario;
    private String tituloLibro;
    private String isbnLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private LocalDate fechaDevolucionReal;
    private String estado;
    private Integer diasRetraso;
    private BigDecimal multa;
    private String situacion;
}