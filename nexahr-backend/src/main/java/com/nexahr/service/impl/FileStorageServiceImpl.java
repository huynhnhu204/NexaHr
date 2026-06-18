package com.nexahr.service.impl;

import com.nexahr.exception.BadRequestException;
import com.nexahr.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_ATTENDANCE_PHOTO_BYTES = 5L * 1024 * 1024;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final Path rootLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new BadRequestException("Không thể khởi tạo thư mục lưu trữ tệp");
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Tệp tải lên không được để trống");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path targetDir = subDirectory != null ? rootLocation.resolve(subDirectory) : rootLocation;
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return subDirectory != null ? subDirectory + "/" + storedFileName : storedFileName;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Không thể lưu tệp: " + originalFilename);
        }
    }

    @Override
    public String storeWithFileName(MultipartFile file, String subDirectory, String fileName) {
        validateAttendancePhoto(file);
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("Tên tệp không hợp lệ");
        }
        String safeName = StringUtils.cleanPath(fileName);
        try {
            Path targetDir = subDirectory != null ? rootLocation.resolve(subDirectory) : rootLocation;
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(safeName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return subDirectory != null ? subDirectory + "/" + safeName : safeName;
        } catch (IOException e) {
            log.error("Failed to store file {}", safeName, e);
            throw new BadRequestException("Không thể lưu ảnh chấm công");
        }
    }

    private void validateAttendancePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng tải lên ảnh minh chứng");
        }
        if (file.getSize() > MAX_ATTENDANCE_PHOTO_BYTES) {
            throw new BadRequestException("Ảnh quá lớn. Kích thước tối đa 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("File không đúng định dạng. Chỉ chấp nhận JPEG, PNG, WebP");
        }
    }

    @Override
    public Path resolve(String storedFileName) {
        return rootLocation.resolve(storedFileName).normalize();
    }

    @Override
    public void delete(String storedFileName) {
        try {
            Files.deleteIfExists(resolve(storedFileName));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", storedFileName, e);
        }
    }
}
