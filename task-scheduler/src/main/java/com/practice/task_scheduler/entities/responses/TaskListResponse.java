package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.TaskList;
import com.practice.task_scheduler.entities.models.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskListResponse {
    private Long id;
    private String name;
    private String description;
    private String color;
    private Boolean isShared;
    private String shareCode;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskListResponse toTaskList(TaskList taskList){
        return TaskListResponse.builder()
                .id(taskList.getId())
                .name(taskList.getName())
                .description(taskList.getDescription())
                .color(taskList.getColor())
                .isShared(taskList.getIsShared())
                .shareCode(taskList.getShareCode())
                .ownerId(taskList.getOwnerId())
                .createdAt(taskList.getCreatedAt())
                .updatedAt(taskList.getUpdatedAt())
                .build();
    }
}
