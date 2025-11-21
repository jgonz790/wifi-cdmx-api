# WiFi CDMX API

API REST para consultar puntos de acceso WiFi gratuito en la Ciudad de México. Proporciona endpoints para buscar, filtrar y localizar puntos WiFi cercanos usando coordenadas geográficas.

## Tecnologías Utilizadas

- **Java 17** - Lenguaje de programación
- **Spring Boot 4.0.0** - Framework principal
  - Spring Data JPA - Persistencia de datos
  - Spring Web MVC - API REST
  - Spring Validation - Validación de datos
- **PostgreSQL** - Base de datos relacional
- **Apache POI 5.2.5** - Lectura de archivos Excel
- **Lombok** - Reducción de código boilerplate
- **SpringDoc OpenAPI 2.3.0** - Documentación Swagger
- **Docker & Docker Compose** - Contenedorización
- **Maven** - Gestión de dependencias
- **JUnit 5 & Mockito** - Testing

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                        Cliente HTTP                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│              (WifiPointController.java)                      │
│                  REST API Endpoints                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│   (WifiPointService.java, DataLoaderService.java)           │
│                  Business Logic                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository Layer                           │
│              (WifiPointRepository.java)                      │
│                 Data Access (JPA)                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                         │
│                  (wifi_points table)                         │
└─────────────────────────────────────────────────────────────┘
```

## Estructura del Proyecto

```
wifi-api/
├── src/
│   ├── main/
│   │   ├── java/com/wificdmx/wifiapi/
│   │   │   ├── config/              # Configuraciones
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/          # Controladores REST
│   │   │   │   └── WifiPointController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── WifiPointDTO.java
│   │   │   │   └── WifiPointResponseDTO.java
│   │   │   ├── exception/           # Manejo de excepciones
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/               # Entidades JPA
│   │   │   │   └── WifiPoint.java
│   │   │   ├── repository/          # Repositorios
│   │   │   │   └── WifiPointRepository.java
│   │   │   ├── service/             # Lógica de negocio
│   │   │   │   ├── DataLoaderService.java
│   │   │   │   └── WifiPointService.java
│   │   │   └── WifiApiApplication.java
│   │   └── resources/
│   │       ├── data/
│   │       │   └── 00-2025-wifi_gratuito_en_cdmx.xlsx
│   │       └── application.yml
│   └── test/
│       └── java/com/wificdmx/wifiapi/
│           └── service/
│               └── WifiPointServiceTest.java
├── docker-compose.yml
├── Dockerfile
├── Makefile
├── pom.xml
└── README.md
```

## Requisitos Previos

- Java 17 o superior
- Docker y Docker Compose
- Maven 3.8+ (opcional si usas Maven Wrapper)
- Git

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/wifi-cdmx-api.git
cd wifi-cdmx-api
```

### 2. Configurar variables de entorno (opcional)

Crea un archivo `.env` si deseas personalizar las credenciales:

```env
POSTGRES_DB=wifi_cdmx
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123
POSTGRES_PORT=5432
APP_PORT=8080
```

### 3. Iniciar PostgreSQL con Docker Compose

```bash
docker-compose up -d postgres
```

Verifica que PostgreSQL esté corriendo:

```bash
docker-compose ps
```

### 4. Compilar el proyecto

```bash
# Con Maven instalado
mvn clean package -DskipTests

# O con Maven Wrapper (incluido en el proyecto)
./mvnw clean package -DskipTests
```

### 5. Ejecutar la aplicación

```bash
# Opción 1: Con Maven
mvn spring-boot:run

# Opción 2: Con el JAR compilado
java -jar target/wifi-api-0.0.1-SNAPSHOT.jar

# Opción 3: Con Docker Compose (todo el stack)
docker-compose up -d
```

### 6. Verificar que la aplicación está corriendo

```bash
curl http://localhost:8080/api/v1/wifi-points/health
```

Respuesta esperada:
```
WiFi CDMX API is running
```

## Endpoints de la API

### Base URL
```
http://localhost:8080/api/v1
```

### 1. Listar todos los puntos WiFi (paginado)

**GET** `/wifi-points`

**Parámetros de consulta:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo para ordenar (default: puntoId)

**Ejemplo de request:**
```bash
curl -X GET "http://localhost:8080/api/v1/wifi-points?page=0&size=10&sort=alcaldia"
```

**Ejemplo de response:**
```json
{
  "content": [
    {
      "puntoId": "PILARES-001",
      "programa": "Pilares",
      "latitud": 19.4326,
      "longitud": -99.1332,
      "alcaldia": "Iztapalapa"
    }
  ],
  "totalElements": 35350,
  "totalPages": 3535,
  "currentPage": 0,
  "pageSize": 10,
  "first": true,
  "last": false
}
```

### 2. Obtener punto WiFi por ID

**GET** `/wifi-points/{id}`

**Ejemplo de request:**
```bash
curl -X GET "http://localhost:8080/api/v1/wifi-points/PILARES-001"
```

**Ejemplo de response:**
```json
{
  "puntoId": "PILARES-001",
  "programa": "Pilares",
  "latitud": 19.4326,
  "longitud": -99.1332,
  "alcaldia": "Iztapalapa"
}
```

**Error 404:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "WiFi point not found with ID: INVALID-ID",
  "path": "/api/v1/wifi-points/INVALID-ID",
  "timestamp": "2025-01-21T10:30:00"
}
```

### 3. Buscar por alcaldía

**GET** `/wifi-points/alcaldia/{alcaldia}`

**Parámetros de consulta:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)

**Ejemplo de request:**
```bash
curl -X GET "http://localhost:8080/api/v1/wifi-points/alcaldia/Iztapalapa?page=0&size=20"
```

**Ejemplo de response:**
```json
{
  "content": [
    {
      "puntoId": "PILARES-001",
      "programa": "Pilares",
      "latitud": 19.4326,
      "longitud": -99.1332,
      "alcaldia": "Iztapalapa"
    }
  ],
  "totalElements": 5234,
  "totalPages": 262,
  "currentPage": 0,
  "pageSize": 20,
  "first": true,
  "last": false
}
```

### 4. Buscar puntos cercanos (proximity search)

**GET** `/wifi-points/nearby`

**Parámetros de consulta (requeridos):**
- `lat`: Latitud (rango: -90 a 90)
- `lon`: Longitud (rango: -180 a 180)
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)

**Ejemplo de request:**
```bash
# Buscar puntos WiFi cerca del Zócalo de la CDMX
curl -X GET "http://localhost:8080/api/v1/wifi-points/nearby?lat=19.4326&lon=-99.1332&size=5"
```

**Ejemplo de response:**
```json
{
  "content": [
    {
      "puntoId": "CENTRO-001",
      "programa": "WiFi Gratuito CDMX",
      "latitud": 19.4328,
      "longitud": -99.1330,
      "alcaldia": "Cuauhtemoc",
      "distancia": 0.023
    },
    {
      "puntoId": "CENTRO-002",
      "programa": "Pilares",
      "latitud": 19.4350,
      "longitud": -99.1350,
      "alcaldia": "Cuauhtemoc",
      "distancia": 0.312
    }
  ],
  "totalElements": 35350,
  "totalPages": 7070,
  "currentPage": 0,
  "pageSize": 5,
  "first": true,
  "last": false
}
```

**Nota:** El campo `distancia` representa la distancia en kilómetros desde el punto de referencia.

**Error 400 (coordenadas inválidas):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Latitude must be between -90 and 90",
  "path": "/api/v1/wifi-points/nearby",
  "timestamp": "2025-01-21T10:30:00"
}
```

### 5. Health Check

**GET** `/wifi-points/health`

```bash
curl -X GET "http://localhost:8080/api/v1/wifi-points/health"
```

**Response:**
```
WiFi CDMX API is running
```

## Documentación Swagger

La API cuenta con documentación interactiva generada automáticamente con Swagger UI.

**URL:** http://localhost:8080/swagger-ui.html

Desde Swagger UI puedes:
- Ver todos los endpoints disponibles
- Probar las APIs directamente desde el navegador
- Ver ejemplos de requests y responses
- Consultar esquemas de datos

**OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Variables de Entorno

### Application (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wifi_cdmx
    username: admin
    password: admin123
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080

logging:
  level:
    com.wificdmx.wifiapi: DEBUG
```

### Docker Compose

```yaml
POSTGRES_DB: wifi_cdmx
POSTGRES_USER: admin
POSTGRES_PASSWORD: admin123
```

## Comandos útiles

### Con Makefile (si está disponible)

```bash
# Compilar el proyecto
make build

# Ejecutar tests
make test

# Iniciar servicios
make run

# Detener servicios
make clean
```

### Con Docker Compose

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v

# Reconstruir imágenes
docker-compose up -d --build
```

### Con Maven

```bash
# Compilar
mvn clean package

# Ejecutar tests
mvn test

# Ejecutar aplicación
mvn spring-boot:run

# Limpiar target
mvn clean
```

## Carga de Datos

La aplicación carga automáticamente los datos del archivo Excel al iniciar, **solo si la base de datos está vacía**.

**Archivo de datos:**
- Ubicación: `src/main/resources/data/00-2025-wifi_gratuito_en_cdmx.xlsx`
- Registros: 35,350 puntos WiFi
- Columnas: id, programa, latitud, longitud, alcaldia

**Proceso de carga:**
1. Al iniciar la aplicación, `DataLoaderService` verifica si existen registros
2. Si la tabla está vacía, lee el archivo Excel
3. Normaliza los nombres de alcaldías (convierte a Title Case)
4. Convierte coordenadas de String a Double
5. Guarda todos los registros con `saveAll()` para eficiencia
6. Registra el progreso cada 5,000 registros

## Decisiones Técnicas

### 1. Búsqueda de proximidad con Haversine

Se utiliza la fórmula de Haversine para calcular distancias entre coordenadas geográficas con precisión. Esta fórmula tiene en cuenta la curvatura de la Tierra.

```sql
SELECT *,
  (6371 * acos(
    cos(radians(:lat)) * cos(radians(latitud)) *
    cos(radians(longitud) - radians(:lon)) +
    sin(radians(:lat)) * sin(radians(latitud))
  )) AS distancia
FROM wifi_points
ORDER BY distancia
```

**Ventajas:**
- Precisión en cálculos geográficos
- Rendimiento aceptable para datasets medianos
- Resultados en kilómetros directamente

### 2. Paginación por defecto

Todos los endpoints que retornan listas usan paginación para:
- Reducir carga en la base de datos
- Mejorar tiempos de respuesta
- Evitar transferir grandes volúmenes de datos

**Configuración:**
- Tamaño por defecto: 20 elementos
- Ordenamiento por defecto: `puntoId`
- Personalizable vía query params

### 3. Normalización de alcaldías

Los nombres de alcaldías se normalizan a Title Case para consistencia:
- `"IZTAPALAPA"` → `"Iztapalapa"`
- `"MIGUEL HIDALGO"` → `"Miguel Hidalgo"`

### 4. Carga de datos condicional

La carga de datos solo ocurre si `wifiPointRepository.count() == 0`, evitando:
- Duplicación de registros
- Tiempo de inicio innecesario en reinicios
- Conflictos de claves primarias

### 5. DTOs separados de entidades

Se usan DTOs para:
- Desacoplar la capa de presentación de la persistencia
- Controlar qué campos se exponen en la API
- Agregar campos calculados (como `distancia`) sin modificar entidades
- Excluir campos internos (timestamps) de las respuestas

### 6. Manejo global de excepciones

`@RestControllerAdvice` centraliza el manejo de errores:
- Respuestas consistentes
- Logging automático
- Códigos HTTP apropiados
- Información útil para debugging

### 7. Spring Boot 4.0.0

Se utiliza la última versión estable para:
- Mejoras de rendimiento
- Nuevas características de Java 17
- Seguridad actualizada
- Compatibilidad con dependencias modernas

## Testing

El proyecto incluye tests unitarios con JUnit 5 y Mockito.

### Ejecutar tests

```bash
# Todos los tests
mvn test

# Test específico
mvn test -Dtest=WifiPointServiceTest

# Con coverage report
mvn test jacoco:report
```

### Cobertura de tests

- `WifiPointServiceTest`: 10 casos de prueba
  - ✅ findAll con paginación
  - ✅ findById exitoso
  - ✅ findById no encontrado (404)
  - ✅ findByAlcaldia
  - ✅ Validación de latitud
  - ✅ Validación de longitud
  - ✅ Validación de coordenadas nulas
  - ✅ Resultados vacíos
  - ✅ Múltiples páginas

## Autor

**Tu Nombre**
- Email: tu.email@example.com
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- LinkedIn: [Tu Perfil](https://linkedin.com/in/tu-perfil)

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

**Desarrollado como prueba técnica para Backend Developer**

🤖 Generado con [Claude Code](https://claude.com/claude-code)
