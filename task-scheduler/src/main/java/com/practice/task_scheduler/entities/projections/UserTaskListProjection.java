package com.practice.task_scheduler.entities.projections;

import com.practice.task_scheduler.entities.models.UserTaskList;

import java.time.LocalDateTime;

public interface UserTaskListProjection {
    Long getId();
    String getUsername();
    String getEmail();
    String getFullName();
    String getAvatarUrl();
    UserTaskList.Role getRole();
    LocalDateTime getJoinedAt();
}
