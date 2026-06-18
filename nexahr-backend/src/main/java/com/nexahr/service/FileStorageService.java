package com.nexahr.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String store(MultipartFile file, String subDirectory);
    String storeWithFileName(MultipartFile file, String subDirectory, String fileName);
    Path resolve(String storedFileName);
    void delete(String storedFileName);
}
