package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class RecurrenceException extends RuntimeException {

    private final ErrorCode errorCode;

    public RecurrenceException(ErrorCode errorCode, String message){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public RecurrenceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
