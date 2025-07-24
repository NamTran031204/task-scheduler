package com.practice.task_scheduler.entities.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.task_scheduler.entities.models.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {

    @JsonProperty("title")
    @NotBlank(message = "Task title is required")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("priority")
    private Task.Priority priority;

    @JsonProperty("due_date")
//    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime dueDate;

    @JsonProperty("task_list_id")
    @NotNull(message = "Task list ID is required")
    private Long taskListId;

    @JsonProperty("assigned_to")
    private Long assignedTo;
}