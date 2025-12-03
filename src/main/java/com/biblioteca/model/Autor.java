package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "AUTORES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_autor")
    @SequenceGenerator(name = "seq_autor", sequenceName = "SEQ_AUTOR", allocationSize = 1)
    @Column(name = "ID_AUTOR")
    private Long idAutor;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;

    @Column(name = "FECHA_NACIMIENTO")
    private LocalDate fechaNacimiento;

    @Column(name = "NACIONALIDAD", length = 50)
    private String nacionalidad;

    @Column(name = "FECHA_REGISTRO")
    private LocalDate fechaRegistro;

    @Column(name = "ESTADO", length = 20)
    private String estado;
}