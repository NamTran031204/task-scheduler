package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.TaskRecurrence;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskRecurrenceResponse {
    private Long id;
    private Long taskId;
    private TaskRecurrence.RecurrenceType recurrenceType;
    private Integer recurrenceInterval;
    private LocalDate recurrenceEndDate;
    private LocalDateTime nextDueDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskRecurrenceResponse toTaskRecurrence(TaskRecurrence taskRecurrence){
        return TaskRecurrenceResponse.builder()
                .id(taskRecurrence.getId())
                .taskId(taskRecurrence.getTaskId())
                .recurrenceType(taskRecurrence.getRecurrenceType())
                .recurrenceEndDate(taskRecurrence.getRecurrenceEndDate())
                .nextDueDate(taskRecurrence.getNextDueDate())
                .isActive(taskRecurrence.getIsActive())
                .createdAt(taskRecurrence.getCreatedAt())
                .updatedAt(taskRecurrence.getUpdatedAt())
                .build();
    }
}
