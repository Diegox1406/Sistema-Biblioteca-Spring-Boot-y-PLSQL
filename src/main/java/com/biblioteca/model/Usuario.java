package com.biblioteca.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "USUARIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usuario")
    @SequenceGenerator(name = "seq_usuario", sequenceName = "SEQ_USUARIO", allocationSize = 1)
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "NOMBRES", nullable = false, length = 100)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "DNI", unique = true, nullable = false, length = 8)
    private String dni;

    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @Column(name = "TELEFONO", length = 15)
    private String telefono;

    @Column(name = "DIRECCION", length = 200)
    private String direccion;

    @Column(name = "FECHA_REGISTRO")
    private LocalDate fechaRegistro;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Column(name = "CANT_PRESTAMOS_ACTIVOS")
    private Integer cantPrestamosActivos;
}