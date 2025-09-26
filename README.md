# AI-Assisted Legacy Modernization

## **Proyecto:** Reducing Migration Risk with AI-Assisted Legacy Modernization  
**Hackathon TCS – IA**  

Una solución de modernización asistida por IA diseñada para industrias como la banca, donde los sistemas legacy soportan operaciones críticas y los riesgos de migración son altos. La plataforma analiza aplicaciones existentes, identifica componentes obsoletos y dependencias de alto impacto, y recomienda estrategias de migración seguras y compatibles. Combinando análisis de código avanzado con IA generativa, proporciona actualizaciones accionables, recomendaciones de arquitectura optimizada y documentación completa, ayudando a acelerar procesos de migración, controlar costos, minimizar riesgos y garantizar código de alta calidad cumpliendo estándares TCS.

---

## **API Endpoints**

Todos los endpoints están bajo la ruta base `/api`.

| Método | Endpoint | Descripción | Body / Parámetros | Respuesta |
|--------|----------|------------|-----------------|-----------|
| `POST` | `/api/analyze` | Analiza un proyecto ZIP y genera patch | Multipart form-data: `file` | `AnalyzeResponse` JSON |
| `POST` | `/api/analyze-repo` | Analiza repositorio Git | JSON: `{ "repoUrl": "..." }` | `AnalyzeResponse` JSON |
| `GET` | `/api/download-patch` | Descarga patch generado | Query param: `projectPath` | Archivo `rewrite.patch` |
| `POST` | `/api/apply-migration` | Aplica migración al proyecto | Query param: `projectPath` | Mensaje con ruta del proyecto migrado |
| `GET` | `/api/download-migrated` | Descarga proyecto migrado en ZIP | Query param: `projectPath` | Archivo `migrated.zip` |
| `POST` | `/api/llm/ask` | Consulta al modelo de lenguaje sobre migración | JSON: `{ "projectPath": "...", "question": "..." }` | JSON: `{ "answer": "..." }` |

---

### **Ejemplos de uso en Postman**

1. **Analizar ZIP**
   - URL: `POST http://localhost:8081/api/analyze`
   - Body: `form-data` → `file`: `demo.zip`
   - Respuesta: JSON con `projectPath`, `patchPath`, `rewriteLog`.

2. **Analizar repositorio Git**
   - URL: `POST http://localhost:8081/api/analyze-repo`
   - Body:
     ```json
     { "repoUrl": "https://github.com/usuario/demo.git" }
     ```

3. **Descargar patch**
   - URL: `GET http://localhost:8081/api/download-patch?projectPath=C:/Temp/migrator/demo`
   - Respuesta: `rewrite.patch` descargable.

4. **Aplicar migración**
   - URL: `POST http://localhost:8081/api/apply-migration?projectPath=C:/Temp/migrator/demo`
   - Respuesta: `"Migración aplicada correctamente. Ruta del proyecto migrado: C:\\Temp\\migrator\\demo"`

5. **Descargar proyecto migrado**
   - URL: `GET http://localhost:8081/api/download-migrated?projectPath=C:/Temp/migrator/demo`
   - Respuesta: `migrated.zip` descargable.

6. **Consultar LLM sobre migración**
   - URL: `POST http://localhost:8081/api/llm/ask`
   - Body:
     ```json
     {
       "projectPath": "C:/Temp/migrator/demo",
       "question": "¿Hay algún riesgo pendiente en esta migración?"
     }
     ```
   - Respuesta:
     ```json
     {
       "answer": "El patch incluye actualización de dependencias a Spring Boot 3.2 y Java 17. No se detectan conflictos críticos."
     }
     ```

---

## **Evidencias**
- Prototipo funcional de API de modernización asistida por IA.
- Proyecto migrado descargable en ZIP.
- Integración con LLM Gemini para recomendaciones de migración.

---

## **Notas finales**
- Solución lista para pruebas locales y despliegue.
- Escalable a múltiples clientes e industrias.
- Enfocada en reducción de riesgos, eficiencia y cumplimiento regulatorio.
