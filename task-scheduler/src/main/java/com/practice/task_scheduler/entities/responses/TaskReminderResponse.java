package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.TaskReminder;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskReminderResponse {
    private Long id;
    private Long taskId;
    private String taskTitle; // Thêm title của task để dễ hiểu
    private LocalDateTime remindAt;
    private String message;
    private TaskReminder.RemindType remindType;
    private Boolean isSent;
    private LocalDateTime sentAt;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static TaskReminderResponse toTaskReminder(TaskReminder taskReminder) {
        return TaskReminderResponse.builder()
                .id(taskReminder.getId())
                .taskId(taskReminder.getTaskId())
                .taskTitle(taskReminder.getTask() != null ? taskReminder.getTask().getTitle() : null)
                .remindAt(taskReminder.getRemindAt())
                .message(taskReminder.getMessage())
                .remindType(taskReminder.getRemindType())
                .isSent(taskReminder.getIsSent())
                .sentAt(taskReminder.getSentAt())
                .createdBy(taskReminder.getCreatedBy())
                .createdAt(taskReminder.getCreatedAt())
                .build();
    }
}