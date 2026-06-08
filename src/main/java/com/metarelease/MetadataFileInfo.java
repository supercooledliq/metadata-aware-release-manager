package com.metarelease;
//A Java record is a simple class made for storing data. it is a small, immutable data holder
public record MetadataFileInfo(String relativePath, long sizeInBytes, String checksum) {
}
