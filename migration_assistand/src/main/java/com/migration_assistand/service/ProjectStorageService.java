package com.migration_assistand.service;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.UUID;

@Service
public class ProjectStorageService {

    private final Path root = Paths.get(System.getProperty("java.io.tmpdir"), "migrator");

    public ProjectStorageService() throws IOException {
        Files.createDirectories(root);
    }

    public Path storeAndExtract(MultipartFile multipartFile) throws IOException {
        String id = UUID.randomUUID().toString();
        Path dir = root.resolve(id);
        Files.createDirectories(dir);

        // Save zip
        Path zipPath = dir.resolve("project.zip");
        try (InputStream is = multipartFile.getInputStream()) {
            Files.copy(is, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Extract zip using commons-compress
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                Path outPath = dir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        // Some zips contain a root folder; try to find pom.xml root
        Path projectRoot = findProjectRoot(dir);
        return projectRoot;
    }

    public Path cloneRepo(String repoUrl) throws Exception {
        String id = UUID.randomUUID().toString();
        Path dir = root.resolve(id);
        Files.createDirectories(dir);
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(dir.toFile())
                .call();
        Path projectRoot = findProjectRoot(dir);
        return projectRoot;
    }

    private Path findProjectRoot(Path dir) throws IOException {
        // find first path that contains pom.xml
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                if (Files.isDirectory(p)) {
                    Path pom = p.resolve("pom.xml");
                    if (Files.exists(pom)) return p;
                }
            }
        }
        // fallback to dir if it itself contains pom
        if (Files.exists(dir.resolve("pom.xml"))) return dir;
        return dir;
    }
}