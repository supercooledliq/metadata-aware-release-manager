package com.metarelease;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    //Path.of("metadata") creates a Java Path object pointing to the metadata folder.
    private static final Path METADATA_DIRECTORY = Path.of("metadata");
    private static final Path SNAPSHOTS_DIRECTORY = Path.of(".metarelease", "snapshots");

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0];

        switch (command) {
            case "scan" -> handleScan();
            case "snapshot" -> handleSnapshot();
            case "list" -> handleList();
            case "diff" -> handleDiff(args);
            case "restore" -> handleRestore(args);
            default -> {
                System.out.println("Unknown command: " + command);
                printHelp();
            }
        }
    }

    private static void handleScan() {
        //java.util.Scanner is used to take input from the user but here the program already gets input from String[] args
        // so ScannerService scans folders and files
        ScannerService scannerService = new ScannerService();

        try {
            List<MetadataFileInfo> files = scannerService.scan(METADATA_DIRECTORY);

            if (files.isEmpty()) {
                System.out.println("No metadata files found.");
                return;
            }

            System.out.println("Metadata files found:");
            for (MetadataFileInfo file : files) {
                System.out.println("- " + file.relativePath());
                System.out.println("  size: " + file.sizeInBytes() + " bytes");
                System.out.println("  checksum: " + file.checksum());
            }
        } catch (IOException e) {
            System.out.println("Could not scan metadata folder: " + e.getMessage());
        }
    }

    private static void handleSnapshot() {
        VersionService versionService = new VersionService(new ScannerService());

        try {
            String versionName = versionService.createSnapshot(METADATA_DIRECTORY, SNAPSHOTS_DIRECTORY);
            System.out.println("Snapshot created: " + versionName);
        } catch (IOException e) {
            System.out.println("Could not create snapshot: " + e.getMessage());
        }
    }

    private static void handleList() {
        VersionService versionService = new VersionService(new ScannerService());

        try {
            List<String> versions = versionService.listSnapshots(SNAPSHOTS_DIRECTORY);

            if (versions.isEmpty()) {
                System.out.println("No snapshots found.");
                return;
            }

            System.out.println("Saved snapshots:");
            for (String version : versions) {
                System.out.println("- " + version);
            }
        } catch (IOException e) {
            System.out.println("Could not list snapshots: " + e.getMessage());
        }
    }

    private static void handleDiff(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: mvn exec:java -Dexec.args=\"diff v1 v2\"");
            return;
        }

        String firstVersion = args[1];
        String secondVersion = args[2];

        System.out.println("Diff command selected.");
        System.out.println("Phase 4 will compare " + firstVersion + " and " + secondVersion + ".");
    }

    private static void handleRestore(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: mvn exec:java -Dexec.args=\"restore v1\"");
            return;
        }

        String version = args[1];

        System.out.println("Restore command selected.");
        System.out.println("Phase 5 will restore metadata from " + version + ".");
    }

    private static void printHelp() {
        System.out.println("Metadata-Aware Release Manager");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  mvn exec:java -Dexec.args=\"scan\"");
        System.out.println("  mvn exec:java -Dexec.args=\"snapshot\"");
        System.out.println("  mvn exec:java -Dexec.args=\"list\"");
        System.out.println("  mvn exec:java -Dexec.args=\"diff v1 v2\"");
        System.out.println("  mvn exec:java -Dexec.args=\"restore v1\"");
    }
}
