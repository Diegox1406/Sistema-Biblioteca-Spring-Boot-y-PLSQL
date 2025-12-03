package com.biblioteca.repository;

import com.biblioteca.dto.EstadisticasDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class StoredProcedureRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Llama al procedimiento almacenado pkg_gestion_prestamos.registrar_prestamo
     */
    @Transactional
    public void registrarPrestamoSP(Long idUsuario, Long idLibro, Integer diasPrestamo) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("pkg_gestion_prestamos.registrar_prestamo");

        query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(2, Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(3, Integer.class, ParameterMode.IN);

        query.setParameter(1, idUsuario);
        query.setParameter(2, idLibro);
        query.setParameter(3, diasPrestamo);

        query.execute();
    }

    /**
     * Llama al procedimiento almacenado pkg_gestion_prestamos.registrar_devolucion
     */
    @Transactional
    public void registrarDevolucionSP(Long idPrestamo) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("pkg_gestion_prestamos.registrar_devolucion");

        query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
        query.setParameter(1, idPrestamo);

        query.execute();
    }

    /**
     * Llama al procedimiento almacenado pkg_gestion_libros.registrar_libro
     */
    @Transactional
    public void registrarLibroSP(String titulo, String isbn, Long idAutor,
                                 Long idCategoria, Integer anio, String editorial, Integer cantidad) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("pkg_gestion_libros.registrar_libro");

        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(3, Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(4, Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(5, Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(6, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(7, Integer.class, ParameterMode.IN);

        query.setParameter(1, titulo);
        query.setParameter(2, isbn);
        query.setParameter(3, idAutor);
        query.setParameter(4, idCategoria);
        query.setParameter(5, anio);
        query.setParameter(6, editorial);
        query.setParameter(7, cantidad);

        query.execute();
    }

    /**
     * Llama al procedimiento almacenado pkg_gestion_prestamos.calcular_multas
     */
    @Transactional
    public void calcularMultasSP() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("pkg_gestion_prestamos.calcular_multas");
        query.execute();
    }
}