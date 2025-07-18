package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.Task;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Boolean isCompleted;
    private Task.Priority priority;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private Long taskListId;
    private Long createdBy;
    private Long assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse toTask(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .isCompleted(task.getIsCompleted())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .taskListId(task.getTaskListId())
                .createdBy(task.getCreatedBy())
                .assignedTo(task.getAssignedTo())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}