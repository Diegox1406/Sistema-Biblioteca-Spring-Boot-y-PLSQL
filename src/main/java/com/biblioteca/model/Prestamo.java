package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "PRESTAMOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_prestamo")
    @SequenceGenerator(name = "seq_prestamo", sequenceName = "SEQ_PRESTAMO", allocationSize = 1)
    @Column(name = "ID_PRESTAMO")
    private Long idPrestamo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LIBRO", nullable = false)
    private Libro libro;

    @Column(name = "FECHA_PRESTAMO")
    private LocalDate fechaPrestamo;

    @Column(name = "FECHA_DEVOLUCION_ESPERADA")
    private LocalDate fechaDevolucionEsperada;

    @Column(name = "FECHA_DEVOLUCION_REAL")
    private LocalDate fechaDevolucionReal;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Column(name = "DIAS_RETRASO")
    private Integer diasRetraso;

    @Column(name = "MULTA")
    private BigDecimal multa;
}