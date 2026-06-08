// flow:
// ChecksumUtil.sha256(file)
// -> create SHA-256 digest object
// -> open the file as an InputStream
// -> read the file in small byte chunks
// -> feed each chunk into MessageDigest
// -> get final hash bytes
// -> convert bytes into readable hex text
// -> return checksum string


package com.metarelease;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {

    private ChecksumUtil() {
    }

    public static String sha256(Path file) throws IOException { //This method receives a file path and returns the SHA-256 checksum.
        MessageDigest digest = createSha256Digest();

        try (InputStream inputStream = Files.newInputStream(file)) {
            //A small temporary byte array used while reading the file.8192 bytes at a time , even if the 
            //files get larger over time, reading in chunks is easier and memory-friendly
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {//This reads the file piece by piece instead of loading the whole file at once.
                digest.update(buffer, 0, bytesRead);
            }
        }

        return toHex(digest.digest());
    }

    private static MessageDigest createSha256Digest() {
        try {
            //MessageDigest is Java’s built-in class for creating checksums/hashes.
            return MessageDigest.getInstance("SHA-256");// this tell java I want to use the SHA-256 algorithm.
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();

        for (byte value : bytes) {
            hex.append(String.format("%02x", value));
        }

        return hex.toString();
    }
}
