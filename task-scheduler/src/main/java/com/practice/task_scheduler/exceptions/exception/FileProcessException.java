package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class FileProcessException extends RuntimeException {
    private final ErrorCode errorCode;
    public FileProcessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public FileProcessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FileProcessException(ErrorCode errorCode, String filename, String message) {
        super(errorCode.getMessage() + " " + filename + ": " + message);
        this.errorCode = errorCode;
    }
}
