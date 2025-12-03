package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "LIBROS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_libro")
    @SequenceGenerator(name = "seq_libro", sequenceName = "SEQ_LIBRO", allocationSize = 1)
    @Column(name = "ID_LIBRO")
    private Long idLibro;

    @Column(name = "TITULO", nullable = false, length = 200)
    private String titulo;

    @Column(name = "ISBN", unique = true, length = 20)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AUTOR", nullable = false)
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CATEGORIA", nullable = false)
    private Categoria categoria;

    @Column(name = "ANIO_PUBLICACION")
    private Integer anioPublicacion;

    @Column(name = "EDITORIAL", length = 100)
    private String editorial;

    @Column(name = "CANTIDAD_TOTAL")
    private Integer cantidadTotal;

    @Column(name = "CANTIDAD_DISPONIBLE")
    private Integer cantidadDisponible;

    @Column(name = "FECHA_REGISTRO")
    private LocalDate fechaRegistro;

    @Column(name = "ESTADO", length = 20)
    private String estado;
}