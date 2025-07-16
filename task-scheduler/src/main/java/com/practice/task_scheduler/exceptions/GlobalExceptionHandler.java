package com.practice.task_scheduler.exceptions;

import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler({UserRequestException.class})
    public ResponseEntity<ErrorResponse> handleException(UserRequestException exception, WebRequest request){
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(ErrorResponse.builder()
                        .status(errorCode.getCode())
                        .message(exception.getMessage())
                        .timestamp(new Date())
                        .path(request.getDescription(false))
                        .error(errorCode.name())
                .build());
    }
}

