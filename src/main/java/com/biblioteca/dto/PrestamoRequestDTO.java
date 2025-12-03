package com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;

    @NotNull(message = "El ID del libro es obligatorio")
    private Long idLibro;

    @Min(value = 1, message = "Los días de préstamo deben ser al menos 1")
    @Max(value = 30, message = "Los días de préstamo no pueden exceder 30")
    private Integer diasPrestamo = 15;
}