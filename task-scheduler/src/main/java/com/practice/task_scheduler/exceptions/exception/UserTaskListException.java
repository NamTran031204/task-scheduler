package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;

public class UserTaskListException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserTaskListException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public UserTaskListException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
