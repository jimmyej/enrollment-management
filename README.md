# Enrollment Management API

API reactiva para la gestión de matrícula académica: estudiantes, cursos e inscripciones (enrollments), construida con **Spring WebFlux** y **MongoDB Reactivo**.

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen)
![MongoDB](https://img.shields.io/badge/MongoDB-Reactive-green)
![Build](https://github.com/jimmyej/enrollment-management/actions/workflows/ci_cd.yml/badge.svg)
![License](https://img.shields.io/badge/license-Unlicensed-lightgrey)

## Descripción

`enrollment-management` es un backend reactivo que expone una API REST para administrar el proceso de matrícula de una institución educativa. Permite:

- Gestionar **estudiantes** (alta, baja, edición, consulta y foto de perfil).
- Gestionar **cursos** (alta, baja, edición y consulta).
- Gestionar **inscripciones (enrollments)** que vinculan un estudiante con uno o varios cursos.

El proyecto sigue un enfoque **funcional/reactivo** (Router + Handler, en lugar de `@RestController` tradicionales) sobre **Spring WebFlux**, con persistencia en **MongoDB** mediante `spring-boot-starter-data-mongodb-reactive`.

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos previos](#requisitos-previos)
- [Configuración](#configuración)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Documentación de la API (Swagger)](#documentación-de-la-api-swagger)
- [Endpoints principales](#endpoints-principales)
- [Modelo de datos](#modelo-de-datos)
- [Pruebas](#pruebas)
- [Integración continua (CI)](#integración-continua-ci)
- [Roadmap / Mejoras sugeridas](#roadmap--mejoras-sugeridas)
- [Contribuir](#contribuir)
- [Autor](#autor)

## Arquitectura

El proyecto implementa el patrón **Router Function + Handler** propio de Spring WebFlux funcional, en lugar de controladores anotados clásicos:

```
Cliente HTTP
    │
    ▼
RouterConfig (define rutas /api/v1/**)
    │
    ▼
Handler (StudentHandler / CourseHandler / EnrollmentHandler)
    │
    ▼
Service (interfaz) → ServiceImpl (lógica de negocio)
    │
    ▼
Repository (Spring Data MongoDB Reactive)
    │
    ▼
MongoDB (documentos: students, courses, enrollments, users, roles)
```

Componentes transversales:

- **`WebExceptionHandler`**: manejo centralizado de errores.
- **`SecurityFilter`**: `WebFilter` reactivo que intercepta cada request.
- **`RequestValidator`**: validación de payloads de entrada.
- **`MediaConfig` / `MediaService`**: integración con **Cloudinary** para la carga de fotos de estudiantes. Nota: las propiedades `CLOUD_NAME`/`API_KEY`/`API_SECRET` ahora aceptan valores por defecto vacíos en el código para evitar errores de creación de beans durante la ejecución de tests; en entornos de integración o producción se recomienda configurar las variables de entorno o los *secrets* del CI para usar credenciales reales de Cloudinary.
- **`SwaggerConfig`**: agrupación y documentación de la API vía OpenAPI/Swagger.

## Tecnologías

| Categoría              | Tecnología                                              |
|------------------------|----------------------------------------------------------|
| Lenguaje               | Java 25                                                  |
| Framework              | Spring Boot 3.5.0 (WebFlux, funcional/reactivo)          |
| Base de datos          | MongoDB (driver reactivo)                                |
| Documentación API      | springdoc-openapi (Swagger UI)                           |
| Reportes               | JasperReports 6.17.0                                     |
| Almacenamiento de imágenes | Cloudinary                                          |
| Utilidades             | Lombok                                                    |
| Testing                | Spring Boot Test, Reactor Test (`StepVerifier`)           |
| Cobertura de código    | JaCoCo                                                    |
| Build                  | Maven (con Maven Wrapper incluido)                        |
| CI                     | GitHub Actions                                            |

## Estructura del proyecto

```
src/main/java/com/mitocode
├── EnrollmentManagementApplication.java   # Clase principal (Spring Boot)
├── configs/                               # Configuración (Mongo, Media/Cloudinary, Swagger, Router)
│   ├── MediaConfig.java
│   ├── MongoConfig.java
│   ├── ResourceWebPropertiesConfig.java
│   ├── RouterConfig.java
│   └── SwaggerConfig.java
├── controllers/                           # Controlador auxiliar
│   └── StudentController.java
├── documents/                             # Entidades / documentos de MongoDB
│   ├── Student.java
│   ├── Course.java
│   ├── Enrollment.java
│   ├── User.java
│   ├── Role.java
│   └── UserRole.java
├── exceptions/
│   └── WebExceptionHandler.java           # Manejo global de excepciones
├── filters/
│   └── SecurityFilter.java                # Filtro reactivo (headers/seguridad)
├── handlers/                              # Lógica de manejo de requests (equivalente a controllers)
│   ├── StudentHandler.java
│   ├── CourseHandler.java
│   └── EnrollmentHandler.java
├── repositories/                          # Repositorios reactivos (Spring Data MongoDB)
│   ├── CrudRepository.java
│   ├── StudentRepository.java
│   ├── CourseRepository.java
│   ├── EnrollmentRepository.java
│   ├── UserRepository.java
│   └── RoleRepository.java
├── services/                              # Interfaces de negocio + implementaciones
│   ├── CrudService.java
│   ├── StudentService.java
│   ├── CourseService.java
│   ├── EnrollmentService.java
│   ├── MediaService.java
│   ├── helpers/PageHelper.java
│   └── impls/                             # Implementaciones
└── validators/
    └── RequestValidator.java

src/main/resources
└── application.properties                 # Configuración de Mongo, Swagger, multipart

src/test/java/com/mitocode
├── EnrollmentManagementApplicationTests.java
└── configs/
    ├── StudentFunctionalTest.java
    ├── CourseFunctionalTest.java
    └── EnrollmentFunctionalTest.java
```

## Requisitos previos

- **Java JDK 25**
- **Maven 3.8+** (o usar el wrapper `mvnw` / `mvnw.cmd` incluido)
- **MongoDB** en ejecución local (o accesible remotamente)
- Cuenta de **Cloudinary** (para la funcionalidad de carga de fotos de estudiantes)

## Configuración

La configuración base se encuentra en `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/management-enrollment-db
spring.data.mongodb.database=management-enrollment-db

springdoc.api-docs.groups.enabled=true
springdoc.swagger-ui.path=/swagger-doc/swagger-ui.html
springdoc.api-docs.path=/swagger-doc/v3/api-docs

## MULTIPART (MultipartProperties)
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2KB
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=25MB
```

Adicionalmente, la integración con Cloudinary (`MediaConfig`) requiere las siguientes **variables de entorno**:

| Variable       | Descripción                          |
|----------------|---------------------------------------|
| `CLOUD_NAME`   | Nombre de la cuenta/cloud en Cloudinary |
| `API_KEY`      | API Key de Cloudinary                 |
| `API_SECRET`   | API Secret de Cloudinary              |

Ejemplo (Linux/macOS):

```bash
export CLOUD_NAME=tu_cloud_name
export API_KEY=tu_api_key
export API_SECRET=tu_api_secret
```

> Estas mismas variables se usan como *secrets* en el pipeline de GitHub Actions (`CLOUD_NAME`, `API_KEY`, `API_SECRET`).

## Instalación y ejecución

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/jimmyej/enrollment-management.git
   cd enrollment-management
   ```

2. **Levantar MongoDB** (si no está corriendo ya), por ejemplo con Docker:

   ```bash
   docker run -d --name mongo-enrollment -p 27017:27017 mongo:5
   ```

3. **Configurar las variables de entorno** de Cloudinary (ver sección anterior).

4. **Compilar y ejecutar** usando el Maven Wrapper:

   ```bash
   # Linux/macOS
   ./mvnw spring-boot:run

   # Windows
   mvnw.cmd spring-boot:run
   ```

   O generar el `.jar` y ejecutarlo:

   ```bash
   ./mvnw clean package
   java -jar target/enrollment-management-0.0.1-SNAPSHOT.jar
   ```

5. La aplicación quedará disponible en `http://localhost:8080`.

## Documentación de la API (Swagger)

El proyecto expone documentación interactiva vía **springdoc-openapi**, agrupada en tres módulos: *Students*, *Courses* y *Enrollments*.

- Swagger UI: `http://localhost:8080/swagger-doc/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/swagger-doc/v3/api-docs`

> Todos los endpoints documentados requieren los headers `Authorization` y `userId` (ver `SwaggerConfig`).

## Endpoints principales

Base path común: `/api/v1`

### Estudiantes — `/api/v1/students`

| Método | Ruta                              | Descripción                              |
|--------|-------------------------------------|-------------------------------------------|
| GET    | `/api/v1/students`                 | Lista todos los estudiantes               |
| GET    | `/api/v1/students/pages`           | Lista estudiantes de forma paginada       |
| GET    | `/api/v1/students/{id}`            | Obtiene un estudiante por ID              |
| POST   | `/api/v1/students`                 | Crea un nuevo estudiante                  |
| PUT    | `/api/v1/students/{id}`            | Actualiza un estudiante existente         |
| PUT    | `/api/v1/students/{id}/upload`     | Sube/actualiza la foto de perfil (multipart, vía Cloudinary) |
| DELETE | `/api/v1/students/{id}`            | Elimina un estudiante                     |

### Cursos — `/api/v1/courses`

| Método | Ruta                          | Descripción                          |
|--------|--------------------------------|----------------------------------------|
| GET    | `/api/v1/courses`             | Lista todos los cursos                |
| GET    | `/api/v1/courses/pages`       | Lista cursos de forma paginada        |
| GET    | `/api/v1/courses/{id}`        | Obtiene un curso por ID               |
| POST   | `/api/v1/courses`             | Crea un nuevo curso                   |
| PUT    | `/api/v1/courses/{id}`        | Actualiza un curso existente          |
| DELETE | `/api/v1/courses/{id}`        | Elimina un curso                      |

### Inscripciones — `/api/v1/enrollments`

| Método | Ruta                              | Descripción                                  |
|--------|-------------------------------------|-------------------------------------------------|
| GET    | `/api/v1/enrollments`              | Lista todas las inscripciones                   |
| GET    | `/api/v1/enrollments/pages`        | Lista inscripciones de forma paginada           |
| GET    | `/api/v1/enrollments/{id}`         | Obtiene una inscripción por ID                  |
| POST   | `/api/v1/enrollments`              | Crea una nueva inscripción (estudiante + cursos) |
| PUT    | `/api/v1/enrollments/{id}`         | Actualiza una inscripción existente             |
| DELETE | `/api/v1/enrollments/{id}`         | Elimina una inscripción                         |

Todos los endpoints devuelven `404 Not Found` cuando el recurso solicitado por `id` no existe.

## Modelo de datos

Documentos MongoDB principales (paquete `com.mitocode.documents`):

**Student**
```json
{
  "id": "string",
  "firstName": "string",
  "lastName": "string",
  "docNumber": "string",
  "age": 0,
  "urlPhoto": "string",
  "publicId": "string"
}
```

**Course**
```json
{
  "id": "string",
  "name": "string",
  "acronym": "string",
  "status": true
}
```

**Enrollment**
```json
{
  "id": "string",
  "enrollmentDate": "2026-08-12T00:00:00",
  "student": { "...": "Student" },
  "courses": [ { "...": "Course" } ],
  "status": true
}
```

**User / Role** — soporte para autenticación/autorización basada en roles (`users`, `roles`).

## Pruebas

El proyecto incluye pruebas funcionales reactivas (con `WebTestClient` / `StepVerifier`) para cada módulo:

- `StudentFunctionalTest`
- `CourseFunctionalTest`
- `EnrollmentFunctionalTest`

Ejecutar la suite de pruebas:

```bash
./mvnw test
```

La cobertura de código se calcula con **JaCoCo** (excluyendo la clase principal y los documentos/entidades) y se genera automáticamente en la fase `prepare-package`:

```bash
./mvnw clean verify
# Reporte en: target/site/jacoco/index.html
```

## Integración continua (CI)

El workflow de GitHub Actions (`.github/workflows/ci_cd.yml`) se ejecuta en cada `push` o `pull request` hacia `master`.

Pasos principales:
1. Checkout del código.
2. Configuración de JDK 25 (Temurin) con caché de Maven.
3. Ejecutar `mvn -B verify` (incluye tests y JaCoCo) y generar el reporte HTML de cobertura en `target/site/jacoco`.
4. Subir `target/site/jacoco` como artifact llamado `jacoco-report`.

Variables/Secrets usadas en CI: `CLOUD_NAME`, `API_KEY`, `API_SECRET`.

## Roadmap / Mejoras sugeridas

- [ ] Reemplazar el `SecurityFilter` de demostración por autenticación real (JWT / OAuth2).
- [ ] Añadir `docker-compose.yml` para levantar la app + MongoDB con un solo comando.
- [ ] Documentar variables de entorno mediante un archivo `.env.example`.
- [ ] Exponer generación de reportes con JasperReports (dependencia ya incluida).
- [ ] Agregar pruebas unitarias a nivel de `service` además de las funcionales existentes.

## Contribuir

Las contribuciones son bienvenidas:

1. Haz un fork del repositorio.
2. Crea una rama para tu feature o fix: `git checkout -b feature/nueva-funcionalidad`.
3. Realiza tus cambios y agrega pruebas si aplica.
4. Haz commit y push: `git commit -m "feat: descripción del cambio"`.
5. Abre un Pull Request hacia `master`.

## Autor

**Jimmy Sanchez** — [@jimmyej](https://github.com/jimmyej)