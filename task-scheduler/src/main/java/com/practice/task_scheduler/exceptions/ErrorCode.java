package com.practice.task_scheduler.exceptions;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER(1000, "user error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001, "user existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "user is not found", HttpStatus.NOT_FOUND),
    USER_PASSWORD_INCORRECT(1003, "password is incorrect", HttpStatus.UNAUTHORIZED),
    USER_PASSWORD_FOUND(1013, "password used by another account", HttpStatus.BAD_REQUEST),

    TASKLIST_EXCEPTION(2000, "TASK LIST EXCEPTION", HttpStatus.BAD_REQUEST),
    TASKLIST_ALREADY_EXIST(2001, "TASK lIST ALREADY EXIST", HttpStatus.BAD_REQUEST),
    TASKLIST_NOT_FOUND(2002, "TASK LIST NOT FOUND", HttpStatus.NOT_FOUND),
    TASKLIST_ACCESS_DENIED(2003, "ACCESS DENIED", HttpStatus.FORBIDDEN),
    TASKLIST_INVALID_SHARECODE(2004, "INVALID SHARE CODE", HttpStatus.FORBIDDEN),
    TASKLIST_NOT_SHARED(2005, "TASK LIST IS NOT SHARED", HttpStatus.FORBIDDEN),
    TASKLIST_CANNOT_JOIN(2006, "CANNOT JOIN TASK LIST", HttpStatus.BAD_REQUEST)
    ,

    DATABASE_EXCEPTION(10000, "DATABASE EXCEPTION", HttpStatus.BAD_REQUEST),
    SQL_EXCEPTION(10001, "SQL Exception: Invalid SQL operator", HttpStatus.BAD_REQUEST),
    /*
        SUB CLASS:{
            DataIntegrityViolationException - Foreign Key Violation
            BadSqlGrammarException  - SQL syntax in @Query


     */

    FILE_EXCEPTION(11000, "FILE EXCEPTION", HttpStatus.BAD_REQUEST),
    FILE_PROCESS(11001, "ERROR IN PROCESS FILE", HttpStatus.PROCESSING),
    FILE_NOT_EXIST(11002, "FILE UPLOAD IS NOT EXIST", HttpStatus.NOT_FOUND),
    FILE_TOO_LARGE(11003, "FILE MUST BE <10Mb", HttpStatus.PROCESSING),
    FILE_TYPE_NOT_SUPPORTED(11004, "FILE IS NOT SUPPORTED", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_UPLOAD_FAILED(11005, "UPLOAD FAILED", HttpStatus.PROCESSING),

    INTERNAL_SERVER_ERROR(12000, "INTERNAL SERVER ERROR", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

}
