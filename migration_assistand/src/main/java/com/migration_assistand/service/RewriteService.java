package com.migration_assistand.service;

import com.migration_assistand.dtos.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class RewriteService {

    /**
     * Ejecuta mvn rewrite:dryRun en el proyecto dado.
     * Devuelve un AnalyzeResponse con contenido del rewrite.patch si existe.
     */
    public AnalyzeResponse runRewriteDryRun(Path projectDir) throws IOException, InterruptedException {
        AnalyzeResponse response = new AnalyzeResponse();
        response.setProjectPath(projectDir.toAbsolutePath().toString());

        // 👇 Garantizamos que el pom.xml tenga todo lo necesario
        ensureRewritePlugin(projectDir);

        String[] cmd = new String[] {
                "mvn.cmd",
                "-B",
                "rewrite:dryRun"
        };

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(projectDir.toFile());
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        int exit = proc.waitFor();
        response.setRewriteExitCode(exit);
        response.setRewriteLog(out.toString());

        Path patch = projectDir.resolve("target/rewrite/rewrite.patch");
        if (Files.exists(patch)) {
            String patchContent = Files.readString(patch, StandardCharsets.UTF_8);
            response.setHasPatch(true);
            response.setPatchContent(patchContent);
            response.setPatchPath(patch.toAbsolutePath().toString());
        } else {
            response.setHasPatch(false);
        }

        return response;
    }

    private void ensureRewritePlugin(Path projectDir) throws IOException {
        Path pom = projectDir.resolve("pom.xml");
        if (!Files.exists(pom)) {
            throw new IOException("No se encontró pom.xml en " + projectDir);
        }

        String content = Files.readString(pom, StandardCharsets.UTF_8);

        // Si ya contiene el plugin de rewrite, asumimos que está configurado
        if (content.contains("rewrite-maven-plugin")) {
            return;
        }

        // --- Plugin con recetas y dependencias ---
        String pluginXml = """
            <plugin>
                <groupId>org.openrewrite.maven</groupId>
                <artifactId>rewrite-maven-plugin</artifactId>
                <version>5.37.0</version>
                <configuration>
                    <activeRecipes>
                        <!-- Java 8 → 11 primero -->
                        <recipe>org.openrewrite.java.migrate.Java8toJava11</recipe>
                        <!-- Si es Spring Boot -->
                        <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2</recipe>
                    </activeRecipes>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.openrewrite.recipe</groupId>
                        <artifactId>rewrite-migrate-java</artifactId>
                        <version>2.28.0</version>
                    </dependency>
                    <dependency>
                        <groupId>org.openrewrite.recipe</groupId>
                        <artifactId>rewrite-spring</artifactId>
                        <version>5.21.0</version>
                    </dependency>
                </dependencies>
            </plugin>
            """;

        // --- Inyección de plugin ---
        if (content.contains("<build>") && content.contains("<plugins>")) {
            content = content.replaceFirst("(?s)(<plugins>)", "$1\n" + pluginXml + "\n");
        } else if (content.contains("<build>")) {
            content = content.replaceFirst("(?s)(<build>)", "$1\n<plugins>\n" + pluginXml + "\n</plugins>\n");
        } else {
            content = content.replaceFirst("(?s)(</project>)",
                    "<build>\n<plugins>\n" + pluginXml + "\n</plugins>\n</build>\n$1");
        }

        Files.writeString(pom, content, StandardCharsets.UTF_8);
    }

    public Path runRewriteApply(Path projectDir) throws IOException, InterruptedException {
        ensureRewritePlugin(projectDir);

        String[] cmd = new String[] { "mvn.cmd", "-B", "rewrite:run" };
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(projectDir.toFile());
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        int exit = proc.waitFor();
        if (exit != 0) {
            throw new IOException("Error al aplicar migración: " + out);
        }

        return projectDir; // aquí ya estará el proyecto migrado
    }

}
