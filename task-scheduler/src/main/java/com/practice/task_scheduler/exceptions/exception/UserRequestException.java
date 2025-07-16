package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class UserRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }




}
