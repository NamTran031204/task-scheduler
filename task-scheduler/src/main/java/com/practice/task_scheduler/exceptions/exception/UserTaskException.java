package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;

public class UserTaskException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserTaskException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public UserTaskException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
