package com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibroDTO {

    private Long idLibro;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^[0-9-]+$", message = "ISBN inválido")
    private String isbn;

    @NotNull(message = "El autor es obligatorio")
    private Long idAutor;

    private String nombreAutor;

    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    private String nombreCategoria;

    @Min(value = 1900, message = "Año de publicación inválido")
    @Max(value = 2100, message = "Año de publicación inválido")
    private Integer anioPublicacion;

    @Size(max = 100, message = "El nombre de la editorial no puede exceder 100 caracteres")
    private String editorial;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidadTotal;

    private Integer cantidadDisponible;

    private String estado;
}