package com.metarelease;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ScannerService {

    public List<MetadataFileInfo> scan(Path metadataDirectory) throws IOException {
        if (!Files.exists(metadataDirectory)) {
            throw new IOException("metadata folder does not exist: " + metadataDirectory);
        }

        if (!Files.isDirectory(metadataDirectory)) {
            throw new IOException("metadata path is not a folder: " + metadataDirectory);
        }

        List<Path> metadataFiles;
        //files.walk walks through a folder and its subfolders
        try (Stream<Path> paths = Files.walk(metadataDirectory)) {
            metadataFiles = paths
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> metadataDirectory.relativize(path).toString()))
                    .toList();
        }

        List<MetadataFileInfo> scannedFiles = new ArrayList<>();
        for (Path file : metadataFiles) {
            Path relativePath = metadataDirectory.relativize(file);
            long sizeInBytes = Files.size(file);
            String checksum = ChecksumUtil.sha256(file);

            scannedFiles.add(new MetadataFileInfo(
                    relativePath.toString(),
                    sizeInBytes,
                    checksum
            ));
        }

        return scannedFiles;
    }
}
