package com.migration_assistand.controller;

import com.migration_assistand.dtos.AnalyzeResponse;
import com.migration_assistand.service.ProjectStorageService;
import com.migration_assistand.service.RewriteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
@Validated
public class AnalyzerController {

    private final ProjectStorageService storageService;
    private final RewriteService rewriteService;

    @Autowired
    public AnalyzerController(ProjectStorageService storageService, RewriteService rewriteService) {
        this.storageService = storageService;
        this.rewriteService = rewriteService;
    }

    /**
     * Subir zip del proyecto
     */
    @PostMapping(path = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResponse> analyzeZip(@RequestPart("file") MultipartFile file) throws Exception {
        Path projectDir = storageService.storeAndExtract(file);
        AnalyzeResponse resp = rewriteService.runRewriteDryRun(projectDir);

        // No enviamos patchContent enorme en el JSON
        if (resp.isHasPatch()) {
            resp.setPatchContent(null);
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * Alternativa: pasar URL de repo (git) en JSON { "repoUrl": "..." }
     */
    @PostMapping(path = "/analyze-repo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalyzeResponse> analyzeRepo(@RequestBody RepoRequest repo) throws Exception {
        Path projectDir = storageService.cloneRepo(repo.getRepoUrl());
        AnalyzeResponse resp = rewriteService.runRewriteDryRun(projectDir);

        if (resp.isHasPatch()) {
            resp.setPatchContent(null);
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * Descargar patch generado
     */
    @GetMapping("/download-patch")
    public ResponseEntity<?> downloadPatch(@RequestParam String projectPath) throws Exception {
        Path patch = Path.of(projectPath, "target/rewrite/rewrite.patch");
        if (!Files.exists(patch)) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = Files.readAllBytes(patch);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"rewrite.patch\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @PostMapping("/apply-migration")
    public ResponseEntity<?> applyMigration(@RequestParam String projectPath) throws Exception {
        Path projectDir = Path.of(projectPath);
        Path migratedDir = rewriteService.runRewriteApply(projectDir);

        return ResponseEntity.ok().body(
            "Migración aplicada correctamente. Ruta del proyecto migrado: " + migratedDir.toAbsolutePath()
        );
    }

    @GetMapping("/download-migrated")
    public ResponseEntity<?> downloadMigrated(@RequestParam String projectPath) throws IOException {
        Path projectDir = Path.of(projectPath);
        Path zipPath = Files.createTempFile("migrated-", ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walk(projectDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    ZipEntry entry = new ZipEntry(projectDir.relativize(path).toString());
                    try {
                        zos.putNextEntry(entry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        }

        byte[] content = Files.readAllBytes(zipPath);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"migrated.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    public static class RepoRequest {
        private String repoUrl;
        public String getRepoUrl() { return repoUrl; }
        public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    }

}
