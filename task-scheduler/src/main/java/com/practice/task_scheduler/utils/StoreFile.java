package com.practice.task_scheduler.utils;

import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.FileProcessException;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StoreFile {

    public static final String storePath = "uploads/";

    @Async("fileAsync")
    public static CompletableFuture<String> storeFile(MultipartFile file) throws FileProcessException {
        try {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            String uniqueName = UUID.randomUUID().toString() + fileName;

            Path path = Paths.get(storePath.substring(0, storePath.length()-1));

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            Path destination = Paths.get(path.toString(), uniqueName);

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return CompletableFuture.completedFuture(uniqueName);
        }catch (IOException e){
            throw new FileProcessException(ErrorCode.FILE_PROCESS);
        } catch (Exception e) {
            throw new FileProcessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public static void checkImage(MultipartFile file) throws FileProcessException{
        checkFile(file);

        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("image/")) {
            throw new FileProcessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    public static void checkFile(MultipartFile file) throws FileProcessException{
        if (file == null || file.isEmpty()) {
            throw new FileProcessException(ErrorCode.FILE_NOT_EXIST);
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new FileProcessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    public static void deleteImage(String url){
        Path path = Paths.get(storePath + url);
        try {
            if(!Files.exists(path)){
                throw new FileProcessException(ErrorCode.FILE_NOT_EXIST);
            }
            Files.delete(path);
        } catch (IOException e) {
            throw new FileProcessException(ErrorCode.FILE_PROCESS);
        }
    }
}
