# SchemanApp — API de Gestión de Horarios

API REST para la gestión de horarios de empleados en múltiples tiendas. Gestiona empleados, tiendas, turnos y asignaciones de turno con control de acceso basado en roles. Permite compartir empleados entre las distintas sucursales. Respetando tiempos de desanso y horas de contrato.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Referencia de la API](#referencia-de-la-api)
- [Tests](#tests)

---

## Características

- **Autenticación JWT** — con access y refresh token sin estado con rotación; los refresh tokens se almacenan en BD y se revocan al reutilizarse
- **Control de acceso por roles** — roles jerárquicos: `ADMIN` > `MANAGER` > `EMPLOYEE`, aplicados por endpoint con `@PreAuthorize`
- **Gestión de empleados** — CRUD con habilitación/deshabilitación de cuenta, tiendas preferidas y horas contratadas semanales
- **Gestión de turnos** — turnos por tienda con tipo (MAÑANA / TARDE / NOCHE), días disponibles y rango de fechas de vigencia
- **Asignaciones de turno** — asignar empleados a turnos con validación de solapamiento y vista de horario semanal
- **Rate limiting** — endpoint de login limitado a 5 intentos por 15 minutos por IP (Bucket4j + Caffeine)
- **Borrado lógico** — tiendas filtradas automáticamente mediante `@SQLRestriction`; borrados físicos protegidos por comprobaciones de integridad referencial
- **Migraciones de base de datos** — esquema versionado con Flyway
- **Documentación OpenAPI** — Swagger UI disponible en el perfil de desarrollo

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.5.11, Java 17 |
| Seguridad | Spring Security 6, JJWT 0.12.6 |
| Persistencia | Spring Data JPA, Hibernate 6, PostgreSQL 16 |
| Migraciones | Flyway 11.7.2 |
| Mapeo | MapStruct 1.6.3 + Lombok |
| Rate Limiting | Bucket4j 8.10.1 + Caffeine |
| Documentación | SpringDoc OpenAPI 2.8.16 |
| Tests | JUnit 5, Mockito, H2 |
| Calidad de código | JaCoCo, SonarCloud |
| Infraestructura | Docker, Docker Compose |

---

## Arquitectura

Organización por funcionalidades bajo `com.abanoj.scheman`:

```
auth/              Auth JWT — login, refresco, logout, registro de manager, cambio de contraseña
employee/          CRUD de empleados, vinculación con tiendas, habilitación/deshabilitación
store/             CRUD de tiendas con borrado lógico
shift/             Gestión de turnos por tienda
shiftassignment/   Asignación de turnos a empleados, vista de horario semanal
config/            Seguridad, CORS, rate limiting, auditoría JPA, OpenAPI
security/          Filtro JWT, servicio de tokens, entry points
exception/         GlobalExceptionHandler con DTO ErrorResponse
shared/            BaseEntity con campos de auditoría createdAt/updatedAt
```

Cada módulo sigue la misma estructura en capas:

```
controller/   Endpoints REST (/api/v1/...) con @PreAuthorize
service/      Interfaz + *Impl con @Transactional
repository/   Repositorios Spring Data JPA
entity/       Entidades JPA que extienden BaseEntity
dto/          Records separados para operaciones de Create, Update y Response
mapper/       Interfaces MapStruct — implementaciones generadas en tiempo de compilación
```

**Decisiones de diseño relevantes:**

- `Employee` comparte su clave primaria UUID con `User` mediante `@MapsId` — una sola identidad, dos responsabilidades
- La contraseña inicial del empleado se establece con su DNI y `mustChangePassword = true`
- Los refresh tokens se almacenan en BD; un nuevo login revoca todos los tokens existentes del usuario
- Los DTOs son records de Java con anotaciones de validación Jakarta
- `@Builder.Default` en campos de entidad con inicializadores para evitar conflictos con Lombok

---

## Instalación

### Requisitos previos

- Java 17+
- Docker y Docker Compose
- Maven (o usar el wrapper incluido)

### 1. Clonar el repositorio

```bash
git clone https://github.com/abanoj/scheman.git
cd scheman
```

### 2. Configurar las variables de entorno

Crear un archivo `.env` en la raíz del proyecto:

```env
# Base de datos
DB_NAME=scheman
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña

# JWT — clave HMAC-SHA256 codificada en Base64 (mínimo 256 bits)
JWT_SECRET=tu_clave_base64

# Cuenta de administrador inicial (se crea al arrancar)
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=tu_contraseña_admin
```

### 3. Iniciar la base de datos

```bash
docker-compose up -d db
```

### 4. Ejecutar la aplicación

```bash
backend/mvnw.cmd -f backend/pom.xml spring-boot:run
```

La API estará disponible en `http://localhost:8080`.
Swagger UI en `http://localhost:8080/docs`.

### Ejecución completa con Docker Compose

```bash
docker-compose up -d
```

Arranca PostgreSQL y la aplicación Spring Boot en perfil de producción (Swagger desactivado, Flyway activado).

---

## Configuración

| Variable | Descripción | Requerida |
|---|---|---|
| `DB_NAME` | Nombre de la base de datos PostgreSQL | Sí |
| `DB_USERNAME` | Usuario de la base de datos | Sí |
| `DB_PASSWORD` | Contraseña de la base de datos | Sí |
| `JWT_SECRET` | Clave de firma HMAC-SHA256 en Base64 | Sí |
| `ADMIN_EMAIL` | Email de la cuenta de administrador inicial | Sí |
| `ADMIN_PASSWORD` | Contraseña de la cuenta de administrador inicial | Sí |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS permitidos (perfil prod) | Solo prod |

**Perfiles:**

| Perfil | DDL | Flyway | Swagger | Logging |
|---|---|---|---|---|
| `dev` | `create-drop` | Desactivado | Activado en `/docs` | DEBUG |
| `test` | H2 en memoria | Desactivado | — | — |
| `prod` | `validate` | Activado | Desactivado | INFO/WARN |

---

## Referencia de la API

Todos los endpoints tienen el prefijo `/api/v1`. Documentación interactiva completa disponible en Swagger UI con el perfil de desarrollo.

### Autenticación

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/auth/login` | Público | Obtener par de tokens de acceso y refresco |
| `POST` | `/auth/refresh` | Público | Rotar refresh token y obtener nuevo par |
| `POST` | `/auth/logout` | Público | Revocar refresh token |
| `POST` | `/auth/signup/manager` | ADMIN | Crear una nueva cuenta de manager |
| `PATCH` | `/auth/{userId}/password` | Solo propietario | Cambiar contraseña propia |

### Empleados

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/employees` | ADMIN, MANAGER | Listar todos los empleados (paginado) |
| `POST` | `/employees` | ADMIN, MANAGER | Registrar nuevo empleado |
| `GET` | `/employees/me` | Autenticado | Obtener perfil propio |
| `GET` | `/employees/{id}` | ADMIN, MANAGER | Obtener empleado por ID |
| `PATCH` | `/employees/{id}` | ADMIN, MANAGER | Actualizar empleado |
| `PATCH` | `/employees/{id}/enable` | ADMIN, MANAGER | Habilitar cuenta |
| `PATCH` | `/employees/{id}/disable` | ADMIN, MANAGER | Deshabilitar cuenta |

### Tiendas

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/stores` | ADMIN, MANAGER | Listar todas las tiendas (paginado) |
| `POST` | `/stores` | ADMIN, MANAGER | Crear tienda |
| `GET` | `/stores/{id}` | ADMIN, MANAGER | Obtener tienda por ID |
| `PATCH` | `/stores/{id}` | ADMIN, MANAGER | Actualizar tienda |
| `DELETE` | `/stores/{id}` | ADMIN, MANAGER | Borrado lógico de tienda |

### Turnos

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/stores/{storeId}/shifts` | ADMIN, MANAGER | Listar turnos de una tienda (paginado) |
| `POST` | `/stores/{storeId}/shifts` | ADMIN, MANAGER | Crear turno |
| `GET` | `/stores/{storeId}/shifts/{id}` | ADMIN, MANAGER | Obtener turno por ID |
| `PUT` | `/stores/{storeId}/shifts/{id}` | ADMIN, MANAGER | Reemplazar turno |
| `DELETE` | `/stores/{storeId}/shifts/{id}` | ADMIN, MANAGER | Eliminar turno |
| `GET` | `/stores/{storeId}/shifts/unassigned` | ADMIN, MANAGER | Turnos sin asignar de una semana (`?date=`) |

### Asignaciones de Turno

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/shifts/{shiftId}/shift-assignments` | ADMIN, MANAGER | Listar asignaciones de un turno (paginado) |
| `POST` | `/shifts/{shiftId}/shift-assignments` | ADMIN, MANAGER | Crear asignación |
| `GET` | `/shifts/{shiftId}/shift-assignments/{id}` | ADMIN, MANAGER | Obtener asignación por ID |
| `PUT` | `/shifts/{shiftId}/shift-assignments/{id}` | ADMIN, MANAGER | Actualizar asignación |
| `DELETE` | `/shifts/{shiftId}/shift-assignments/{id}` | ADMIN, MANAGER | Eliminar asignación |
| `GET` | `/shifts/{shiftId}/shift-assignments/employee/{employeeId}` | ADMIN, MANAGER, Propietario | Asignaciones por empleado |
| `GET` | `/shift-assignments/employees/{employeeId}/weekly` | ADMIN, MANAGER, Propietario | Horario semanal (`?date=`) |

---

## Tests

El proyecto cuenta con 24 clases de test organizadas en tres capas:

```bash
# Ejecutar todos los tests (usa H2, no requiere Docker)
backend/mvnw.cmd -f backend/pom.xml test

# Ejecutar una clase concreta
backend/mvnw.cmd -f backend/pom.xml test -Dtest=EmployeeControllerTest

# Ejecutar con informe de cobertura
backend/mvnw.cmd -f backend/pom.xml verify
```

| Capa | Anotación | Qué verifica |
|---|---|---|
| Controlador | `@WebMvcTest` | Capa HTTP, validación de requests, forma de la respuesta |
| Seguridad | `@WebMvcTest` (clase `*SecurityTest` separada) | 403 por roles en cada endpoint |
| Servicio | `@ExtendWith(MockitoExtension.class)` | Lógica de negocio y validaciones |
| Repositorio | `@DataJpaTest` | Consultas, restricciones, borrado lógico |

El informe de cobertura se genera en `backend/target/site/jacoco/index.html` tras ejecutar `mvn verify`.

## Próximas funcionalidades :rocket:

### Corto plazo
- [ ] Pedido de cambio de turno por parte de `Employee`.
- [ ] Solicitud de día libre por parte de `Employee`.
- [ ] Asignación de vacaciones por parte de un `MANAGER`.

### Medio plazo
- [ ] Integración con OAuth2 (Google/GitHub)

### Largo plazo
- [ ] Creación del rol de encargado de sucursal y sus funcionalidades.
- [ ] Solicitud de pedidos y devoluciones por sucursal.
- [ ] Otras ideas futuras...