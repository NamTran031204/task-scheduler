package com.practice.task_scheduler.entities.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.task_scheduler.entities.models.TaskReminder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReminderDTO {

    @JsonProperty("remind_at")
    private LocalDateTime remindAt;

    @JsonProperty("message")
    private String message;

    @JsonProperty("reminder_type")
    private TaskReminder.RemindType remindType;

    @JsonProperty("minutes_before_due")
    private Integer minutesBeforeDue;
}