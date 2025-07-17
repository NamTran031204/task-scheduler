package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class TaskListException extends RuntimeException {
    private final ErrorCode errorCode;

    public TaskListException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TaskListException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
