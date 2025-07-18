package com.practice.task_scheduler.exceptions.exception;

import com.practice.task_scheduler.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class TaskException extends RuntimeException {
  private final ErrorCode errorCode;

  public TaskException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public TaskException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}