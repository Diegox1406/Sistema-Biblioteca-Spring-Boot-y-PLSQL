# Sistema de Administración de Biblioteca - Spring Boot + Oracle PL/SQL

## Descripción

Sistema de administración de biblioteca desarrollado con Spring Boot y Oracle Database con procedimientos almacenados en PL/SQL.

## Tecnologías

- **Backend**: Spring Boot 3, Java 17
- **Base de Datos**: Oracle Database 
- **ORM**: Spring Data JPA, Hibernate
- **Build Tool**: Maven
- **Validación**: Bean Validation (Jakarta)

## Características Principales

### Gestión de Libros
- Registro de libros con validaciones
- Búsqueda por título, autor, categoría
- Control de disponibilidad en tiempo real
- Integración con procedimientos PL/SQL

### Gestión de Préstamos
- Registro de préstamos con validaciones de negocio
- Control de límites (máx. 3 préstamos por usuario)
- Cálculo automático de multas por retraso
- Devolución de libros con actualización de inventario

### Reportes y Estadísticas
- Dashboard con estadísticas generales
- Reporte de préstamos en mora
- Cálculo de multas pendientes

## Endpoints API REST

### Libros
```
GET    /api/libros              - Listar libros disponibles
GET    /api/libros/{id}         - Obtener libro por ID
POST   /api/libros              - Registrar nuevo libro
GET    /api/libros/buscar?titulo={titulo} - Buscar por título
```

### Préstamos
```
POST   /api/prestamos           - Registrar préstamo
PUT    /api/prestamos/{id}/devolver - Registrar devolución
GET    /api/prestamos/usuario/{id} - Préstamos de un usuario
GET    /api/prestamos/mora      - Préstamos en mora
POST   /api/prestamos/calcular-multas - Calcular multas
```

### Reportes
```
GET    /api/reportes/estadisticas - Estadísticas del sistema