package com.practice.task_scheduler.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER(1000, "user error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001, "user existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "user is not found", HttpStatus.NOT_FOUND),
    USER_PASSWORD_INCORRECT(1003, "password is incorrect", HttpStatus.UNAUTHORIZED),
    USER_PASSWORD_FOUND(1013, "password used by another account", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
