package com.kompu.api.entity.shared.gateway;

/**
 * Gateway interface for file storage operations.
 */
public interface FileStorageGateway {

    /**
     * Save a base64 encoded file.
     *
     * @param originalFilename the desired filename (without path)
     * @param base64Content    the base64 encoded content (may include data URI
     *                         scheme prefix)
     * @param subDirectory     optional subdirectory to organize files (e.g. tenant
     *                         ID)
     * @return the relative path or URL where the file is stored
     */
    String saveBase64File(String originalFilename, String base64Content, String subDirectory);
}
