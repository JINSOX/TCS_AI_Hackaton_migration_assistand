package com.migration_assistand.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LLMService {

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * Envía una pregunta sobre migración al modelo Gemini y devuelve la respuesta cruda.
     */
    public String ask(String projectPath, String question) throws Exception {
        // Leer patch o proyecto migrado
        Path patch = Path.of(projectPath, "target/rewrite/rewrite.patch");
        String patchContent = Files.exists(patch) ? Files.readString(patch) : "";

        // Inicializar el cliente con tu API key
        Client geminiClient = Client.builder()
                .apiKey(apiKey)
                .build();

        // Concatenar pregunta con patch
        String prompt = "You are a helpful assistant for Java migration.\nQuestion: " 
                + question + "\n\nPatch:\n" + patchContent;

        // Generar contenido usando la forma más simple según la documentación
        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-2.5-flash",
                prompt,
                null // no necesitamos configuración adicional por ahora
        );

        // Devolver la respuesta de texto
        return response.text();
    }
}
