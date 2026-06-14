# Scheman — API de Gestión de Horarios

API REST para la gestión de horarios de empleados en múltiples tiendas. Gestiona empleados, tiendas, turnos y asignaciones con control de acceso basado en roles, validaciones de negocio (descanso mínimo entre turnos, horas contratadas semanales) y una vista de cobertura semanal por tienda.

## Tabla de Contenidos

- [Demo](#demo)
- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Referencia de la API](#referencia-de-la-api)
- [Tests](#tests)
- [Próximas funcionalidades](#próximas-funcionalidades-rocket)

---

## Demo

🔗 **[https://scheman-ui.onrender.com](https://scheman-ui.onrender.com)**

> Los datos de demo se reinician automáticamente cada 6 horas.
> El servicio está en Render free tier — si lleva un rato inactivo puede tardar ~30 segundos en despertar.

| Rol | Email | Contraseña |
|---|---|---|
| Admin | `admin@demo.com` | `Demo@2026` |
| Manager | `manager@demo.com` | `Demo@2026` |
| Empleado | `empleado@demo.com` | `Demo@2026` |

---

## Características

- **Autenticación JWT** — access token en memoria (15 min) y refresh token rotativo almacenado en BD (7 días); revocación automática en cada nuevo login
- **Control de acceso por roles** — jerarquía `ADMIN` > `MANAGER` > `EMPLOYEE` aplicada por endpoint con `@PreAuthorize`
- **Gestión de managers** — CRUD exclusivo para ADMIN con habilitación/deshabilitación de cuenta
- **Gestión de empleados** — CRUD con vinculación a tiendas, turno preferente y horas contratadas semanales
- **Gestión de turnos** — por tienda con tipo (MORNING/AFTERNOON/NIGHT), días disponibles y rango de vigencia; soporte de turnos nocturnos que cruzan la medianoche
- **Asignaciones de turno** — validación de turno único por día y descanso mínimo de 12 horas entre turnos consecutivos
- **Aviso de horas semanales** — si una asignación supera las horas contratadas, la respuesta incluye un campo `warning` no bloqueante
- **Cobertura semanal** — endpoint que devuelve el estado de cobertura (asignado/sin asignar) de todos los turnos activos de una tienda para una semana
- **Rate limiting** — login limitado a 10 intentos por 5 minutos por IP (Bucket4j + Caffeine)
- **Borrado lógico** — tiendas filtradas automáticamente con `@SQLRestriction`
- **Demo mode** — seed automático al arrancar y reset periódico cada 6 horas vía `@Scheduled`
- **Documentación OpenAPI** — Swagger UI disponible en el perfil de desarrollo (`/docs`)

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
| Infraestructura | Docker, Docker Compose |

---

## Arquitectura

Organización por funcionalidades bajo `com.abanoj.scheman`:

```
auth/              JWT — login, refresco, logout, registro de manager, cambio de contraseña
employee/          CRUD de empleados, vinculación con tiendas, habilitación/deshabilitación
store/             CRUD de tiendas con borrado lógico
shift/             Turnos por tienda con cobertura semanal
shiftassignment/   Asignaciones con validación de solapamiento y aviso de horas
demo/              Seed y reset automático de datos de demostración
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
dto/          Records para Create, Update y Response
mapper/       Interfaces MapStruct generadas en compilación
```

**Decisiones de diseño relevantes:**

- `Employee` comparte su PK UUID con `User` mediante `@MapsId` — una sola identidad, dos responsabilidades
- La contraseña inicial del empleado es su DNI con `mustChangePassword = true`
- Los refresh tokens se almacenan en BD; un nuevo login revoca todos los tokens existentes del usuario
- El campo `warning` en `ShiftAssignmentResponseDto` transporta avisos no bloqueantes (horas superadas) sin interrumpir el flujo

---

## Instalación

### Requisitos previos

- Java 17+
- Docker y Docker Compose

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

> **Producción:** `https://scheman.onrender.com`

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

| Perfil | DDL | Flyway | Swagger | Demo |
|---|---|---|---|---|
| `dev` | `update` | Desactivado | `/docs` | Activado |
| `test` | H2 en memoria | Desactivado | — | Desactivado |
| `prod` | `validate` | Activado | Desactivado | Desactivado |

---

## Referencia de la API

Todos los endpoints tienen el prefijo `/api/v1`. Documentación interactiva completa en Swagger UI con el perfil de desarrollo.

### Autenticación

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/auth/login` | Público | Obtener par de tokens |
| `POST` | `/auth/refresh` | Público | Rotar refresh token |
| `POST` | `/auth/logout` | Público | Revocar refresh token |
| `POST` | `/auth/signup/manager` | ADMIN | Crear cuenta de manager |
| `PATCH` | `/auth/{userId}/password` | Solo propietario | Cambiar contraseña propia |

### Managers

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/managers` | ADMIN | Listar managers (paginado) |
| `GET` | `/managers/{id}` | ADMIN | Obtener por ID |
| `PATCH` | `/managers/{id}` | ADMIN | Actualizar nombre |
| `PATCH` | `/managers/{id}/enable` | ADMIN | Habilitar cuenta |
| `PATCH` | `/managers/{id}/disable` | ADMIN | Deshabilitar cuenta |

### Empleados

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/employees` | ADMIN, MANAGER | Listar empleados (paginado) |
| `POST` | `/employees` | ADMIN, MANAGER | Registrar empleado |
| `GET` | `/employees/me` | Autenticado | Perfil propio |
| `GET` | `/employees/{id}` | ADMIN, MANAGER | Obtener por ID |
| `PATCH` | `/employees/{id}` | ADMIN, MANAGER | Actualizar empleado |
| `PATCH` | `/employees/{id}/enable` | ADMIN, MANAGER | Habilitar cuenta |
| `PATCH` | `/employees/{id}/disable` | ADMIN, MANAGER | Deshabilitar cuenta |

### Tiendas

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/stores` | ADMIN, MANAGER | Listar tiendas (paginado) |
| `POST` | `/stores` | ADMIN, MANAGER | Crear tienda |
| `GET` | `/stores/{id}` | ADMIN, MANAGER | Obtener por ID |
| `PATCH` | `/stores/{id}` | ADMIN, MANAGER | Actualizar tienda |
| `DELETE` | `/stores/{id}` | ADMIN, MANAGER | Borrado lógico |

### Turnos

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/stores/{storeId}/shifts` | ADMIN, MANAGER | Listar turnos de una tienda |
| `POST` | `/stores/{storeId}/shifts` | ADMIN, MANAGER | Crear turno |
| `GET` | `/stores/{storeId}/shifts/{id}` | ADMIN, MANAGER | Obtener por ID |
| `PUT` | `/stores/{storeId}/shifts/{id}` | ADMIN, MANAGER | Reemplazar turno |
| `DELETE` | `/stores/{storeId}/shifts/{id}` | ADMIN, MANAGER | Eliminar turno |
| `GET` | `/stores/{storeId}/shifts/unassigned` | ADMIN, MANAGER | Turnos sin cubrir (`?date=`) |
| `GET` | `/stores/{storeId}/shifts/coverage` | ADMIN, MANAGER | Cobertura semanal (`?date=`) |

### Asignaciones de Turno

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/shifts/{shiftId}/shift-assignments` | ADMIN, MANAGER | Listar asignaciones (paginado) |
| `POST` | `/shifts/{shiftId}/shift-assignments` | ADMIN, MANAGER | Crear asignación |
| `GET` | `/shifts/{shiftId}/shift-assignments/{id}` | ADMIN, MANAGER | Obtener por ID |
| `PUT` | `/shifts/{shiftId}/shift-assignments/{id}` | ADMIN, MANAGER | Actualizar asignación |
| `DELETE` | `/shifts/{shiftId}/shift-assignments/{id}` | ADMIN, MANAGER | Eliminar asignación |
| `GET` | `/shifts/{shiftId}/shift-assignments/employee/{employeeId}` | ADMIN, MANAGER, Propietario | Por empleado |
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
| Seguridad | `@WebMvcTest` (clase `*SecurityTest`) | 403 por rol en cada endpoint |
| Servicio | `@ExtendWith(MockitoExtension.class)` | Lógica de negocio y validaciones |
| Repositorio | `@DataJpaTest` | Consultas, restricciones, borrado lógico |

El informe de cobertura se genera en `backend/target/site/jacoco/index.html` tras ejecutar `mvn verify`.

---

## Próximas funcionalidades :rocket:

### Corto plazo
- [ ] Pedido de cambio de turno por parte de `EMPLOYEE`
- [ ] Solicitud de día libre por parte de `EMPLOYEE`
- [ ] Asignación de vacaciones por parte de un `MANAGER`

### Medio plazo
- [ ] Integración con OAuth2 (Google / GitHub)
- [ ] Notificaciones por email al asignar un turno

### Largo plazo
- [ ] Rol de encargado de sucursal con funcionalidades propias
- [ ] Solicitud de pedidos y devoluciones por sucursal
