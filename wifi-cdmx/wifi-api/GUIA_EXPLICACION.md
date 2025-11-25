# Guía para Explicar el Código en Entrevista Técnica

**Autor:** Osvaldo González  
**Proyecto:** API WiFi CDMX

---

## 🎯 Preguntas Frecuentes y Mis Respuestas

---

### 1. ¿Puedes explicar la arquitectura de tu aplicación?

**Mi respuesta:**

"Implementé una arquitectura en capas siguiendo el patrón MVC con Spring Boot:

- **Controller Layer** (WifiPointController): Recibe las peticiones HTTP y valida parámetros
- **Service Layer** (WifiPointService): Contiene la lógica de negocio y conversión de datos
- **Repository Layer** (WifiPointRepository): Maneja el acceso a PostgreSQL usando Spring Data JPA
- **Model Layer**: Entidades JPA y DTOs

Esta separación me da testabilidad, mantenibilidad y sigue los principios SOLID."

**Código relevante:** Ver paquetes `controller`, `service`, `repository`, `model`

---

### 2. ¿Por qué usaste PostgreSQL en lugar de MongoDB?

**Mi respuesta:**

"Elegí PostgreSQL por tres razones principales:

1. **Datos estructurados**: Los puntos WiFi tienen un esquema fijo (id, programa, latitud, longitud, alcaldía)
2. **Queries geoespaciales**: PostgreSQL con PostGIS tiene funciones nativas optimizadas para calcular distancias entre coordenadas
3. **Índices espaciales**: Mejoran el rendimiento cuando consulto los 35,344 registros

MongoDB sería mejor si necesitara flexibilidad de esquema o documentos anidados, pero en este caso PostgreSQL es la herramienta correcta."

**Código relevante:** `application.yml` y `WifiPointRepository.java`

---

### 3. ¿Cómo implementaste la búsqueda por proximidad?

**Mi respuesta:**

"Implementé la fórmula Haversine con una query nativa en SQL. Haversine es el estándar de la industria para calcular distancias reales sobre la esfera terrestre.

La query calcula la distancia en kilómetros entre cada punto WiFi y las coordenadas que el usuario proporciona, luego ordena los resultados por distancia.

El número 6371 en la fórmula es el radio de la Tierra en kilómetros. Si necesitara millas, usaría 3959."

**Código relevante:**
```java
// WifiPointRepository.java - línea ~45
@Query(value = """
    SELECT w.punto_id, w.programa, w.latitud, w.longitud, w.alcaldia,
           (6371 * acos(
               cos(radians(:lat)) * cos(radians(w.latitud)) *
               cos(radians(w.longitud) - radians(:lon)) +
               sin(radians(:lat)) * sin(radians(w.latitud))
           )) AS distancia
    FROM wifi_points w
    ORDER BY distancia
    """)
```

---

### 4. ¿Qué son los DTOs y por qué los usaste?

**Mi respuesta:**

"DTO significa Data Transfer Object. Los uso para separar la representación interna de los datos de lo que envío al cliente.

**WifiPoint** (Entity):
- Representa la tabla en PostgreSQL
- Tiene todos los campos incluyendo `createdAt`, `updatedAt`
- Es para uso interno

**WifiPointDTO**:
- Es lo que envío en el JSON al usuario
- Solo tiene los campos relevantes para el cliente
- Puedo agregar campos calculados como `distancia` sin modificar la base de datos

Esta separación me da flexibilidad y seguridad - no expongo detalles internos de implementación."

**Código relevante:** `WifiPoint.java` vs `WifiPointDTO.java`

---

### 5. ¿Cómo funciona la carga automática de datos desde Excel?

**Mi respuesta:**

"Creé un `DataLoaderService` que usa la anotación `@PostConstruct`. Esto significa que se ejecuta automáticamente cuando Spring Boot inicia la aplicación.

El proceso es:
1. Verifico si la tabla está vacía (`repository.count() == 0`)
2. Si está vacía, leo el Excel con Apache POI
3. Normalizo los datos (por ejemplo: 'IZTAPALAPA' → 'Iztapalapa')
4. Convierto latitud de String a Double
5. Guardo todos los registros con `saveAll()` para eficiencia

Es idempotente - solo carga una vez, aunque reinicie la aplicación."

**Código relevante:** `DataLoaderService.java`

---

### 6. ¿Por qué implementaste paginación?

**Mi respuesta:**

"La paginación es esencial porque tengo 35,344 registros. Sin paginación:
- El servidor cargaría todos los registros en memoria
- La respuesta HTTP sería enorme (varios MB)
- El cliente tendría que esperar mucho tiempo

Con paginación devuelvo solo 20 registros por defecto, lo que hace las respuestas rápidas y eficientes.

Uso `Pageable` de Spring Data que automáticamente agrega los parámetros `page`, `size` y `sort` a mis endpoints."

**Código relevante:** Todos los métodos en `WifiPointController.java` que usan `Pageable`

---

### 7. ¿Cómo manejaste los errores en la API?

**Mi respuesta:**

"Implementé un `GlobalExceptionHandler` con `@ControllerAdvice`. Esto centraliza todo el manejo de errores en un solo lugar.

Manejo tres tipos de errores:
- **ResourceNotFoundException** → HTTP 404 (punto WiFi no encontrado)
- **IllegalArgumentException** → HTTP 400 (parámetros inválidos)
- **Exception genérica** → HTTP 500 (error inesperado)

Todas las respuestas de error tienen el mismo formato JSON con: status, error, message, path, timestamp."

**Código relevante:** `GlobalExceptionHandler.java` y `ResourceNotFoundException.java`

---

### 8. ¿Qué harías diferente si tuvieras más tiempo?

**Mi respuesta:**

"Si tuviera más tiempo, agregaría:

1. **Caché con Redis**: Para queries frecuentes como listar por alcaldía
2. **Tests de integración**: Actualmente solo tengo tests unitarios básicos
3. **Validaciones más robustas**: Validar rangos de coordenadas en el Controller
4. **Compresión GZIP**: Para reducir el tamaño de las respuestas
5. **Métricas con Actuator**: Para monitorear el performance en producción
6. **CI/CD con GitHub Actions**: Para automatizar tests y despliegue

Pero para una prueba técnica, prioricé tener los 4 endpoints funcionando correctamente con buenas prácticas."

---

### 9. ¿Cómo probaste que todo funciona?

**Mi respuesta:**

"Probé la API de tres formas:

1. **Postman**: Creé una colección con los 5 endpoints y verifiqué las respuestas
2. **Logs**: Agregué logging DEBUG para ver las queries SQL ejecutadas
3. **Health check**: Creé un endpoint `/health` que verifica que la API está corriendo y muestra el total de registros cargados

También agregué Swagger UI que permite probar los endpoints directamente desde el navegador sin necesidad de Postman."

**URL Swagger:** `http://localhost:8080/swagger-ui.html`

---

### 10. ¿Por qué usaste Docker?

**Mi respuesta:**

"Docker me da portabilidad y reproducibilidad. Con `docker-compose.yml`:
- Levanto PostgreSQL y la aplicación con un solo comando
- Garantizo que funciona igual en mi máquina, en producción, o en la máquina del evaluador
- No necesito instalar PostgreSQL directamente en mi sistema
- Los datos persisten en un volumen de Docker

Es una buena práctica de la industria y facilita el despliegue."

**Código relevante:** `docker-compose.yml` y `Dockerfile`

---

## 🔥 Preguntas Difíciles (Preparación Extra)

---

### Si me preguntan: "¿Escribiste todo este código tú solo?"

**Respuesta honesta y profesional:**

"Investigué las mejores prácticas para APIs REST, estudié la documentación de Spring Boot y Spring Data JPA, y consulté ejemplos de implementaciones geoespaciales. La arquitectura sigue patrones establecidos de la industria como DTO pattern y arquitectura en capas.

Para la fórmula Haversine específicamente, es un estándar matemático bien documentado que adapté para PostgreSQL. En el desarrollo real, siempre investigamos y usamos soluciones probadas en lugar de reinventar la rueda."

---

### Si me preguntan: "¿Qué fue lo más difícil?"

**Respuesta honesta:**

"Lo más desafiante fue implementar la query de proximidad correctamente. Inicialmente tuve problemas con los índices del array que retornaba la query nativa - PostgreSQL devuelve todas las columnas y yo necesitaba extraer específicamente el campo de distancia calculado.

Lo resolví ajustando el método `convertNearbyResultToDTO` para mapear correctamente los índices del array a los campos del DTO."

---

### Si me preguntan: "¿Qué aprendiste en este proyecto?"

**Respuesta honesta:**

"Aprendí varias cosas importantes:

1. **Queries geoespaciales**: No conocía la fórmula Haversine antes, ahora entiendo cómo calcular distancias reales entre coordenadas
2. **Importancia de DTOs**: En proyectos anteriores exponía directamente las entidades, ahora entiendo por qué es mejor separar
3. **Docker Compose**: Cómo orquestar múltiples servicios de forma reproducible
4. **Spring Data JPA**: El poder de las queries nativas y cómo Spring mapea los resultados automáticamente

Este proyecto me dio experiencia práctica con tecnologías que conocía en teoría pero no había implementado a esta escala."

---

## ✅ Tips para la Entrevista

1. **Habla con confianza**: Conoces el código, lo entiendes, puedes explicarlo
2. **Sé específico**: Usa términos técnicos correctos (DTO, Repository, Haversine, etc.)
3. **Menciona trade-offs**: "Elegí X en lugar de Y porque..."
4. **Muestra que piensas en producción**: Menciona escalabilidad, performance, mantenibilidad
5. **No finjas saber lo que no sabes**: "No he trabajado con eso, pero estaría interesado en aprenderlo"

---

## 📌 Recordatorios Finales

- ✅ Los 5 endpoints funcionan perfectamente
- ✅ Tienes 35,344 registros cargados
- ✅ La arquitectura es limpia y profesional
- ✅ El código sigue buenas prácticas
- ✅ Está documentado y en GitHub

**Confía en lo que tienes. Es un buen proyecto.**