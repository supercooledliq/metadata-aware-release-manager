package com.metarelease;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class VersionService {

    private final ScannerService scannerService;

    public VersionService(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    public String createSnapshot(Path metadataDirectory, Path snapshotsDirectory) throws IOException {
        List<MetadataFileInfo> scannedFiles = scannerService.scan(metadataDirectory);
        String versionName = nextVersionName(snapshotsDirectory);

        Path snapshotDirectory = snapshotsDirectory.resolve(versionName);
        Path snapshotMetadataDirectory = snapshotDirectory.resolve("metadata");

        Files.createDirectories(snapshotMetadataDirectory);
        copyMetadataFiles(metadataDirectory, snapshotMetadataDirectory, scannedFiles);
        writeManifest(snapshotDirectory, versionName, scannedFiles);

        return versionName;
    }

    public List<String> listSnapshots(Path snapshotsDirectory) throws IOException {
        if (!Files.exists(snapshotsDirectory)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(snapshotsDirectory)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.matches("v\\d+"))
                    .sorted(Comparator.comparingInt(this::versionNumber))
                    .toList();
        }
    }

    private String nextVersionName(Path snapshotsDirectory) throws IOException {
        List<String> existingVersions = listSnapshots(snapshotsDirectory);

        int highestVersion = 0;
        for (String version : existingVersions) {
            highestVersion = Math.max(highestVersion, versionNumber(version));
        }

        return "v" + (highestVersion + 1);
    }

    private int versionNumber(String versionName) {
        return Integer.parseInt(versionName.substring(1));
    }

    private void copyMetadataFiles(
            Path metadataDirectory,
            Path snapshotMetadataDirectory,
            List<MetadataFileInfo> scannedFiles
    ) throws IOException {
        for (MetadataFileInfo file : scannedFiles) {
            Path source = metadataDirectory.resolve(file.relativePath());
            Path target = snapshotMetadataDirectory.resolve(file.relativePath());

            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void writeManifest(
            Path snapshotDirectory,
            String versionName,
            List<MetadataFileInfo> scannedFiles
    ) throws IOException {
        Path manifestFile = snapshotDirectory.resolve("manifest.json");
        String manifest = buildManifestJson(versionName, scannedFiles);

        Files.writeString(manifestFile, manifest);
    }

    private String buildManifestJson(String versionName, List<MetadataFileInfo> scannedFiles) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"version\": \"").append(escapeJson(versionName)).append("\",\n");
        json.append("  \"timestamp\": \"").append(Instant.now()).append("\",\n");
        json.append("  \"files\": [\n");

        for (int i = 0; i < scannedFiles.size(); i++) {
            MetadataFileInfo file = scannedFiles.get(i);

            json.append("    {\n");
            json.append("      \"path\": \"").append(escapeJson(file.relativePath())).append("\",\n");
            json.append("      \"checksum\": \"").append(escapeJson(file.checksum())).append("\",\n");
            json.append("      \"sizeInBytes\": ").append(file.sizeInBytes()).append("\n");
            json.append("    }");

            if (i < scannedFiles.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        return json.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
