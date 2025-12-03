
-- PL/SQL con Oracle

-- 1. CREACIÓN DE TABLAS
-- Tabla de Autores
CREATE TABLE autores
(
    id_autor         NUMBER PRIMARY KEY,
    nombre           VARCHAR2(100) NOT NULL,
    apellido         VARCHAR2(100) NOT NULL,
    fecha_nacimiento DATE,
    nacionalidad     VARCHAR2(50),
    fecha_registro   DATE DEFAULT SYSDATE,
    estado           VARCHAR2(20) DEFAULT 'ACTIVO'
);

-- Tabla de Categorías
CREATE TABLE categorias
(
    id_categoria NUMBER PRIMARY KEY,
    nombre       VARCHAR2(50) NOT NULL UNIQUE,
    descripcion  VARCHAR2(200)
);

-- Tabla de Libros
CREATE TABLE libros
(
    id_libro            NUMBER PRIMARY KEY,
    titulo              VARCHAR2(200) NOT NULL,
    isbn                VARCHAR2(20) UNIQUE,
    id_autor            NUMBER,
    id_categoria        NUMBER,
    anio_publicacion    NUMBER(4),
    editorial           VARCHAR2(100),
    cantidad_total      NUMBER DEFAULT 1,
    cantidad_disponible NUMBER DEFAULT 1,
    fecha_registro      DATE   DEFAULT SYSDATE,
    estado              VARCHAR2(20) DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_libro_autor FOREIGN KEY (id_autor) REFERENCES autores (id_autor),
    CONSTRAINT fk_libro_categoria FOREIGN KEY (id_categoria) REFERENCES categorias (id_categoria),
    CONSTRAINT chk_cantidad CHECK (cantidad_disponible <= cantidad_total)
);

-- Tabla de Usuarios
CREATE TABLE usuarios
(
    id_usuario             NUMBER PRIMARY KEY,
    nombres                VARCHAR2(100) NOT NULL,
    apellidos              VARCHAR2(100) NOT NULL,
    dni                    VARCHAR2(8) UNIQUE NOT NULL,
    email                  VARCHAR2(100) UNIQUE,
    telefono               VARCHAR2(15),
    direccion              VARCHAR2(200),
    fecha_registro         DATE   DEFAULT SYSDATE,
    estado                 VARCHAR2(20) DEFAULT 'ACTIVO',
    cant_prestamos_activos NUMBER DEFAULT 0
);

-- Tabla de Préstamos
CREATE TABLE prestamos
(
    id_prestamo               NUMBER PRIMARY KEY,
    id_usuario                NUMBER NOT NULL,
    id_libro                  NUMBER NOT NULL,
    fecha_prestamo            DATE   DEFAULT SYSDATE,
    fecha_devolucion_esperada DATE   NOT NULL,
    fecha_devolucion_real     DATE,
    estado                    VARCHAR2(20) DEFAULT 'ACTIVO',
    dias_retraso              NUMBER DEFAULT 0,
    multa                     NUMBER(10,2) DEFAULT 0,
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (id_libro) REFERENCES libros (id_libro)
);

-- Tabla de Historial de Auditoría
CREATE TABLE auditoria_prestamos
(
    id_auditoria     NUMBER PRIMARY KEY,
    id_prestamo      NUMBER,
    accion           VARCHAR2(50),
    usuario_bd       VARCHAR2(50),
    fecha_accion     DATE DEFAULT SYSDATE,
    datos_anteriores CLOB,
    datos_nuevos     CLOB
);

-- Secuencias
CREATE SEQUENCE seq_autor START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_categoria START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_libro START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_usuario START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_prestamo START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_auditoria START WITH 1 INCREMENT BY 1;

-- 2. PAQUETE DE GESTIÓN DE LIBROS

CREATE
OR REPLACE PACKAGE pkg_gestion_libros AS
    -- Procedimientos
    PROCEDURE registrar_libro(
        p_titulo VARCHAR2,
        p_isbn VARCHAR2,
        p_id_autor NUMBER,
        p_id_categoria NUMBER,
        p_anio NUMBER,
        p_editorial VARCHAR2,
        p_cantidad NUMBER
    );

    PROCEDURE actualizar_disponibilidad(
        p_id_libro NUMBER,
        p_incremento NUMBER
    );

    -- Funciones
    FUNCTION
obtener_disponibilidad(p_id_libro NUMBER) RETURN NUMBER;

    FUNCTION
buscar_libros_por_autor(p_id_autor NUMBER) RETURN SYS_REFCURSOR;

    FUNCTION
obtener_libros_populares RETURN SYS_REFCURSOR;
END pkg_gestion_libros;
/

CREATE
OR REPLACE PACKAGE BODY pkg_gestion_libros AS

    PROCEDURE registrar_libro(
        p_titulo VARCHAR2,
        p_isbn VARCHAR2,
        p_id_autor NUMBER,
        p_id_categoria NUMBER,
        p_anio NUMBER,
        p_editorial VARCHAR2,
        p_cantidad NUMBER
    ) IS
        v_count NUMBER;
BEGIN
        -- Validar que el ISBN no exista
SELECT COUNT(*)
INTO v_count
FROM libros
WHERE isbn = p_isbn;

IF
v_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'El ISBN ya existe en el sistema');
END IF;

        -- Insertar libro
INSERT INTO libros (id_libro, titulo, isbn, id_autor, id_categoria,
                    anio_publicacion, editorial, cantidad_total, cantidad_disponible)
VALUES (seq_libro.NEXTVAL, p_titulo, p_isbn, p_id_autor, p_id_categoria,
        p_anio, p_editorial, p_cantidad, p_cantidad);

COMMIT;
DBMS_OUTPUT
.
PUT_LINE
('Libro registrado exitosamente');
EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR
(-20002, 'Error al registrar libro: ' || SQLERRM);
END registrar_libro;

    PROCEDURE actualizar_disponibilidad(
        p_id_libro NUMBER,
        p_incremento NUMBER
    )
IS
BEGIN
UPDATE libros
SET cantidad_disponible = cantidad_disponible + p_incremento
WHERE id_libro = p_id_libro;

IF
SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Libro no encontrado');
END IF;

COMMIT;
END actualizar_disponibilidad;

    FUNCTION
obtener_disponibilidad(p_id_libro NUMBER) RETURN NUMBER IS
        v_disponible NUMBER;
BEGIN
SELECT cantidad_disponible
INTO v_disponible
FROM libros
WHERE id_libro = p_id_libro;

RETURN v_disponible;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN -1;
END obtener_disponibilidad;

    FUNCTION
buscar_libros_por_autor(p_id_autor NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT l.id_libro,
       l.titulo,
       l.isbn,
       l.cantidad_disponible,
       a.nombre || ' ' || a.apellido as autor,
       c.nombre                      as categoria
FROM libros l
         INNER JOIN autores a ON l.id_autor = a.id_autor
         INNER JOIN categorias c ON l.id_categoria = c.id_categoria
WHERE l.id_autor = p_id_autor
  AND l.estado = 'DISPONIBLE';

RETURN v_cursor;
END buscar_libros_por_autor;

    FUNCTION
obtener_libros_populares RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT l.titulo,
       l.isbn,
       COUNT(p.id_prestamo)          as total_prestamos,
       a.nombre || ' ' || a.apellido as autor
FROM libros l
         INNER JOIN autores a ON l.id_autor = a.id_autor
         LEFT JOIN prestamos p ON l.id_libro = p.id_libro
GROUP BY l.titulo, l.isbn, a.nombre, a.apellido
ORDER BY COUNT(p.id_prestamo) DESC
    FETCH FIRST 10 ROWS ONLY;

RETURN v_cursor;
END obtener_libros_populares;

END pkg_gestion_libros;
/

-- 3. PAQUETE DE GESTIÓN DE PRÉSTAMOS

CREATE
OR REPLACE PACKAGE pkg_gestion_prestamos AS
    -- Excepciones personalizadas
    ex_libro_no_disponible EXCEPTION;
    ex_usuario_inactivo
EXCEPTION;
    ex_limite_prestamos
EXCEPTION;

    -- Procedimientos
    PROCEDURE registrar_prestamo(
        p_id_usuario NUMBER,
        p_id_libro NUMBER,
        p_dias_prestamo NUMBER DEFAULT 15
    );

    PROCEDURE registrar_devolucion(
        p_id_prestamo NUMBER
    );

    PROCEDURE calcular_multas;

    -- Funciones
    FUNCTION
verificar_mora_usuario(p_id_usuario NUMBER) RETURN BOOLEAN;

    FUNCTION
obtener_prestamos_usuario(p_id_usuario NUMBER) RETURN SYS_REFCURSOR;

    FUNCTION
generar_reporte_morosos RETURN SYS_REFCURSOR;
END pkg_gestion_prestamos;
/

CREATE
OR REPLACE PACKAGE BODY pkg_gestion_prestamos AS

    PROCEDURE registrar_prestamo(
        p_id_usuario NUMBER,
        p_id_libro NUMBER,
        p_dias_prestamo NUMBER DEFAULT 15
    ) IS
        v_disponible NUMBER;
        v_estado_usuario
VARCHAR2(20);
        v_prestamos_activos
NUMBER;
        v_tiene_mora
BOOLEAN;
BEGIN
        -- Verificar estado del usuario
SELECT estado, cant_prestamos_activos
INTO v_estado_usuario, v_prestamos_activos
FROM usuarios
WHERE id_usuario = p_id_usuario;

IF
v_estado_usuario != 'ACTIVO' THEN
            RAISE ex_usuario_inactivo;
END IF;

        -- Verificar límite de préstamos
        IF
v_prestamos_activos >= 3 THEN
            RAISE ex_limite_prestamos;
END IF;

        -- Verificar mora
        v_tiene_mora
:= verificar_mora_usuario(p_id_usuario);
        IF
v_tiene_mora THEN
            RAISE_APPLICATION_ERROR(-20010, 'El usuario tiene préstamos en mora');
END IF;

        -- Verificar disponibilidad del libro
        v_disponible
:= pkg_gestion_libros.obtener_disponibilidad(p_id_libro);

        IF
v_disponible <= 0 THEN
            RAISE ex_libro_no_disponible;
END IF;

        -- Registrar préstamo
INSERT INTO prestamos (id_prestamo, id_usuario, id_libro,
                       fecha_prestamo, fecha_devolucion_esperada, estado)
VALUES (seq_prestamo.NEXTVAL, p_id_usuario, p_id_libro,
        SYSDATE, SYSDATE + p_dias_prestamo, 'ACTIVO');

-- Actualizar disponibilidad
pkg_gestion_libros
.
actualizar_disponibilidad
(p_id_libro, -1);

        -- Actualizar contador de préstamos del usuario
UPDATE usuarios
SET cant_prestamos_activos = cant_prestamos_activos + 1
WHERE id_usuario = p_id_usuario;

COMMIT;
DBMS_OUTPUT
.
PUT_LINE
('Préstamo registrado exitosamente');

EXCEPTION
        WHEN ex_libro_no_disponible THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR
(-20004, 'El libro no está disponible');
WHEN ex_usuario_inactivo THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR
(-20005, 'El usuario está inactivo');
WHEN ex_limite_prestamos THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR
(-20006, 'El usuario alcanzó el límite de préstamos activos');
WHEN OTHERS THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR
(-20007, 'Error al registrar préstamo: ' || SQLERRM);
END registrar_prestamo;

    PROCEDURE registrar_devolucion(
        p_id_prestamo NUMBER
    )
IS
        v_id_libro NUMBER;
        v_id_usuario
NUMBER;
        v_fecha_esperada
DATE;
        v_dias_retraso
NUMBER;
        v_multa
NUMBER := 0;
BEGIN
        -- Obtener datos del préstamo
SELECT id_libro, id_usuario, fecha_devolucion_esperada
INTO v_id_libro, v_id_usuario, v_fecha_esperada
FROM prestamos
WHERE id_prestamo = p_id_prestamo
  AND estado = 'ACTIVO';

-- Calcular días de retraso
v_dias_retraso
:= GREATEST(TRUNC(SYSDATE) - TRUNC(v_fecha_esperada), 0);

        -- Calcular multa (S/. 2 por día)
        IF
v_dias_retraso > 0 THEN
            v_multa := v_dias_retraso * 2;
END IF;

        -- Actualizar préstamo
UPDATE prestamos
SET fecha_devolucion_real = SYSDATE,
    estado                = 'DEVUELTO',
    dias_retraso          = v_dias_retraso,
    multa                 = v_multa
WHERE id_prestamo = p_id_prestamo;

-- Actualizar disponibilidad del libro
pkg_gestion_libros
.
actualizar_disponibilidad
(v_id_libro, 1);

        -- Actualizar contador de préstamos del usuario
UPDATE usuarios
SET cant_prestamos_activos = cant_prestamos_activos - 1
WHERE id_usuario = v_id_usuario;

COMMIT;

IF
v_multa > 0 THEN
            DBMS_OUTPUT.PUT_LINE('Devolución registrada con multa de S/. ' || v_multa);
ELSE
            DBMS_OUTPUT.PUT_LINE('Devolución registrada exitosamente');
END IF;

EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20008, 'Préstamo no encontrado o ya devuelto');
WHEN OTHERS THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR
(-20009, 'Error al registrar devolución: ' || SQLERRM);
END registrar_devolucion;

    PROCEDURE calcular_multas
IS
        CURSOR c_prestamos_mora IS
SELECT id_prestamo,
       TRUNC(SYSDATE) - TRUNC(fecha_devolucion_esperada) as dias_retraso
FROM prestamos
WHERE estado = 'ACTIVO'
  AND TRUNC(SYSDATE) > TRUNC(fecha_devolucion_esperada);

v_multa
NUMBER;
BEGIN
FOR rec IN c_prestamos_mora LOOP
            v_multa := rec.dias_retraso * 2;

UPDATE prestamos
SET dias_retraso = rec.dias_retraso,
    multa        = v_multa
WHERE id_prestamo = rec.id_prestamo;
END LOOP;

COMMIT;
DBMS_OUTPUT
.
PUT_LINE
('Multas calculadas exitosamente');
END calcular_multas;

    FUNCTION
verificar_mora_usuario(p_id_usuario NUMBER) RETURN BOOLEAN IS
        v_count NUMBER;
BEGIN
SELECT COUNT(*)
INTO v_count
FROM prestamos
WHERE id_usuario = p_id_usuario
  AND estado = 'ACTIVO'
  AND TRUNC(SYSDATE) > TRUNC(fecha_devolucion_esperada);

RETURN v_count > 0;
END verificar_mora_usuario;

    FUNCTION
obtener_prestamos_usuario(p_id_usuario NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT p.id_prestamo,
       l.titulo,
       l.isbn,
       p.fecha_prestamo,
       p.fecha_devolucion_esperada,
       p.fecha_devolucion_real,
       p.estado,
       p.dias_retraso,
       p.multa
FROM prestamos p
         INNER JOIN libros l ON p.id_libro = l.id_libro
WHERE p.id_usuario = p_id_usuario
ORDER BY p.fecha_prestamo DESC;

RETURN v_cursor;
END obtener_prestamos_usuario;

    FUNCTION
generar_reporte_morosos RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT u.dni,
       u.nombres || ' ' || u.apellidos as usuario,
       u.email,
       u.telefono,
       COUNT(p.id_prestamo)            as prestamos_en_mora,
       SUM(p.multa)                    as total_multas
FROM usuarios u
         INNER JOIN prestamos p ON u.id_usuario = p.id_usuario
WHERE p.estado = 'ACTIVO'
  AND TRUNC(SYSDATE) > TRUNC(p.fecha_devolucion_esperada)
GROUP BY u.dni, u.nombres, u.apellidos, u.email, u.telefono
ORDER BY SUM(p.multa) DESC;

RETURN v_cursor;
END generar_reporte_morosos;

END pkg_gestion_prestamos;
/

-- 4. TRIGGERS

-- Trigger para auditoría de préstamos
CREATE
OR REPLACE TRIGGER trg_auditoria_prestamos
AFTER INSERT OR
UPDATE OR
DELETE
ON prestamos
    FOR EACH ROW
DECLARE
v_accion VARCHAR2(50);
    v_datos_anteriores
CLOB;
    v_datos_nuevos
CLOB;
BEGIN
    IF
INSERTING THEN
        v_accion := 'INSERT';
        v_datos_nuevos
:= 'ID_PRESTAMO: ' || :NEW.id_prestamo ||
                         ', ID_USUARIO: ' || :NEW.id_usuario ||
                         ', ID_LIBRO: ' || :NEW.id_libro ||
                         ', ESTADO: ' || :NEW.estado;
    ELSIF
UPDATING THEN
        v_accion := 'UPDATE';
        v_datos_anteriores
:= 'ESTADO: ' || :OLD.estado ||
                             ', MULTA: ' || :OLD.multa;
        v_datos_nuevos
:= 'ESTADO: ' || :NEW.estado ||
                         ', MULTA: ' || :NEW.multa;
    ELSIF
DELETING THEN
        v_accion := 'DELETE';
        v_datos_anteriores
:= 'ID_PRESTAMO: ' || :OLD.id_prestamo;
END IF;

INSERT INTO auditoria_prestamos (id_auditoria, id_prestamo, accion, usuario_bd,
                                 datos_anteriores, datos_nuevos)
VALUES (seq_auditoria.NEXTVAL,
        COALESCE(:NEW.id_prestamo, :OLD.id_prestamo),
        v_accion, USER, v_datos_anteriores, v_datos_nuevos);
END;
/

-- Trigger para validar disponibilidad antes de préstamo
CREATE
OR REPLACE TRIGGER trg_validar_disponibilidad
BEFORE INSERT ON prestamos
FOR EACH ROW
DECLARE
v_disponible NUMBER;
BEGIN
SELECT cantidad_disponible
INTO v_disponible
FROM libros
WHERE id_libro = :NEW.id_libro;

IF
v_disponible <= 0 THEN
        RAISE_APPLICATION_ERROR(-20011,
            'No hay copias disponibles del libro');
END IF;
END;
/

-- ============================================
-- 5. VISTAS
-- ============================================

-- Vista de libros disponibles
CREATE
OR REPLACE VIEW v_libros_disponibles AS
SELECT l.id_libro,
       l.titulo,
       l.isbn,
       a.nombre || ' ' || a.apellido as autor,
       c.nombre                      as categoria,
       l.cantidad_disponible,
       l.editorial,
       l.anio_publicacion
FROM libros l
         INNER JOIN autores a ON l.id_autor = a.id_autor
         INNER JOIN categorias c ON l.id_categoria = c.id_categoria
WHERE l.cantidad_disponible > 0
  AND l.estado = 'DISPONIBLE';

-- Vista de préstamos activos
CREATE
OR REPLACE VIEW v_prestamos_activos AS
SELECT p.id_prestamo,
       u.nombres || ' ' || u.apellidos                                  as usuario,
       u.dni,
       l.titulo                                                         as libro,
       p.fecha_prestamo,
       p.fecha_devolucion_esperada,
       CASE
           WHEN TRUNC(SYSDATE) > TRUNC(p.fecha_devolucion_esperada)
               THEN 'EN MORA'
           ELSE 'AL DÍA'
           END                                                          as situacion,
       GREATEST(TRUNC(SYSDATE) - TRUNC(p.fecha_devolucion_esperada), 0) as dias_retraso
FROM prestamos p
         INNER JOIN usuarios u ON p.id_usuario = u.id_usuario
         INNER JOIN libros l ON p.id_libro = l.id_libro
WHERE p.estado = 'ACTIVO';

-- 6. PROCEDIMIENTOS DE REPORTES


CREATE
OR REPLACE PROCEDURE sp_reporte_estadisticas IS
    v_total_libros NUMBER;
    v_total_usuarios
NUMBER;
    v_prestamos_activos
NUMBER;
    v_total_multas
NUMBER;
BEGIN
SELECT COUNT(*)
INTO v_total_libros
FROM libros
WHERE estado = 'DISPONIBLE';
SELECT COUNT(*)
INTO v_total_usuarios
FROM usuarios
WHERE estado = 'ACTIVO';
SELECT COUNT(*)
INTO v_prestamos_activos
FROM prestamos
WHERE estado = 'ACTIVO';
SELECT NVL(SUM(multa), 0)
INTO v_total_multas
FROM prestamos
WHERE estado = 'ACTIVO';

DBMS_OUTPUT
.
PUT_LINE
('====== ESTADÍSTICAS DEL SISTEMA ======');
    DBMS_OUTPUT.PUT_LINE
('Total de libros: ' || v_total_libros);
    DBMS_OUTPUT.PUT_LINE
('Total de usuarios: ' || v_total_usuarios);
    DBMS_OUTPUT.PUT_LINE
('Préstamos activos: ' || v_prestamos_activos);
    DBMS_OUTPUT.PUT_LINE
('Total multas pendientes: S/. ' || v_total_multas);
    DBMS_OUTPUT.PUT_LINE
('======================================');
END;
/

-- 7. DATOS DE PRUEBA


-- Insertar categorías
INSERT INTO categorias VALUES (seq_categoria.NEXTVAL, 'Ficción', 'Novelas y cuentos');
INSERT INTO categorias
VALUES (seq_categoria.NEXTVAL, 'Tecnología', 'Libros de programación y tecnología');
INSERT INTO categorias
VALUES (seq_categoria.NEXTVAL, 'Ciencia', 'Libros científicos');

-- Insertar autores
INSERT INTO autores
VALUES (seq_autor.NEXTVAL, 'Gabriel', 'García Márquez',
        TO_DATE('1927-03-06', 'YYYY-MM-DD'), 'Colombiano', SYSDATE, 'ACTIVO');
INSERT INTO autores
VALUES (seq_autor.NEXTVAL, 'Robert', 'Martin',
        TO_DATE('1952-12-05', 'YYYY-MM-DD'), 'Estadounidense', SYSDATE, 'ACTIVO');

-- Insertar libros
BEGIN
    pkg_gestion_libros.registrar_libro
(
        'Cien Años de Soledad', '978-0307474728', 1, 1, 1967, 'Editorial Sudamericana', 3
    );
    pkg_gestion_libros.registrar_libro
(
        'Clean Code', '978-0132350884', 2, 2, 2008, 'Prentice Hall', 5
    );
END;
/

-- Insertar usuarios
INSERT INTO usuarios VALUES (seq_usuario.NEXTVAL, 'Juan', 'Pérez García',
    '12345678', 'juan.perez@email.com', '987654321', 'Av. Principal 123', SYSDATE, 'ACTIVO', 0);
INSERT INTO usuarios
VALUES (seq_usuario.NEXTVAL, 'María', 'López Rodríguez',
        '87654321', 'maria.lopez@email.com', '912345678', 'Calle Secundaria 456', SYSDATE, 'ACTIVO', 0);

COMMIT;

-- 8. EJEMPLOS DE USO

-- Registrar un préstamo
BEGIN
    pkg_gestion_prestamos.registrar_prestamo
(
        p_id_usuario => 1,
        p_id_libro => 1,
        p_dias_prestamo => 15
    );
END;
/

-- Consultar préstamos de un usuario
DECLARE
v_cursor SYS_REFCURSOR;
    v_id_prestamo
NUMBER;
    v_titulo
VARCHAR2(200);
    v_fecha_prestamo
DATE;
BEGIN
    v_cursor
:= pkg_gestion_prestamos.obtener_prestamos_usuario(1);

    LOOP
FETCH v_cursor INTO v_id_prestamo, v_titulo, v_fecha_prestamo;
        EXIT
WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE
('Préstamo: ' || v_id_prestamo || ' - ' || v_titulo);
END LOOP;

CLOSE v_cursor;
END;
/

-- Ver estadísticas
BEGIN
    sp_reporte_estadisticas;
END;
/