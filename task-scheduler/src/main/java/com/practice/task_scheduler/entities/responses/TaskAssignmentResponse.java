package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignmentResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private LocalDateTime assignedAt;
    private LocalDateTime changedStatus;
    private Long assignedBy;
    private String assignedByName;

    public static TaskAssignmentResponse fromUserTaskAssignment(UserTaskAssignment assignment) {
        return TaskAssignmentResponse.builder()
                .userId(assignment.getUserId())
                .username(assignment.getUser() != null ? assignment.getUser().getUsername() : null)
                .fullName(assignment.getUser() != null ? assignment.getUser().getFullName() : null)
                .avatarUrl(assignment.getUser() != null ? assignment.getUser().getAvatarUrl() : null)
                .assignedAt(assignment.getAssignedAt())
                .changedStatus(assignment.getChangedStatus())
                .assignedBy(assignment.getAssignedBy())
                .assignedByName(assignment.getAssignedByUser() != null ? assignment.getAssignedByUser().getUsername() : null)
                .build();
    }
}