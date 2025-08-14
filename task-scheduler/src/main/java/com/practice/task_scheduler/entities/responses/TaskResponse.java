package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Long taskListId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Map<UserTaskAssignment.Status, List<TaskAssignmentResponse>> userTasksResponse;

    public static TaskResponse toTask(Task task, List<UserTaskAssignment> assignments) {
        Map<UserTaskAssignment.Status, List<TaskAssignmentResponse>> userTasks = new HashMap<>();
        userTasks.put(UserTaskAssignment.Status.IN_PROGRESS, new ArrayList<>());
        userTasks.put(UserTaskAssignment.Status.COMPLETED, new ArrayList<>());
        assignments.forEach(assignment -> {
            TaskAssignmentResponse response = TaskAssignmentResponse.fromUserTaskAssignment(assignment);
            userTasks.get(assignment.getStatus()).add(response);
        });

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .isCompleted(task.getIsCompleted())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .taskListId(task.getTaskListId())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .userTasksResponse(userTasks)
                .build();
    }
}