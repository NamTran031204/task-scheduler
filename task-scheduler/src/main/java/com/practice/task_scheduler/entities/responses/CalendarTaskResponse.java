package com.practice.task_scheduler.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarTaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String priority;
    private boolean isCompleted;
    private String color;
    private String listName;
    private String recurringInstanceId;
    private boolean isRecurring;
    private Integer assignedUsersCount;
    private Integer completedUsersCount;
}
