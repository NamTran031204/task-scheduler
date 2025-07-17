package com.practice.task_scheduler.exceptions;

import com.practice.task_scheduler.exceptions.exception.FileProcessException;
import com.practice.task_scheduler.exceptions.exception.TaskListException;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler({UserRequestException.class, FileProcessException.class, TaskListException.class})
    public ResponseEntity<ErrorResponse> handleException(Exception exception, WebRequest request){
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        if (exception instanceof UserRequestException){
            errorCode = ((UserRequestException) exception).getErrorCode();
        }else if (exception instanceof FileProcessException){
            errorCode = ((FileProcessException) exception).getErrorCode();
        }else if (exception instanceof TaskListException){
            errorCode = ((TaskListException) exception).getErrorCode();
        }

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(ErrorResponse.builder()
                        .status(errorCode.getCode())
                        .message(exception.getMessage())
                        .timestamp(new Date())
                        .path(request.getDescription(false))
                        .error(errorCode.name())
                        .build());
    }

    @ExceptionHandler({SQLException.class})
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException exception, WebRequest request){

        ErrorCode errorCode = ErrorCode.SQL_EXCEPTION;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(ErrorResponse.builder()
                .status(errorCode.getCode())
//                .message(exception.getMessage()) -- khong nen viet nhu nay vi se tra ra: Statement.executeQuery() cannot issue statements that do not produce result sets.
                .message(exception.getMessage().contains("executeQuery") ? errorCode.getMessage(): "Database error occurred")
                .timestamp(new Date())
                .path(request.getDescription(false))
                .error(errorCode.name())
                .build());

    }

}

