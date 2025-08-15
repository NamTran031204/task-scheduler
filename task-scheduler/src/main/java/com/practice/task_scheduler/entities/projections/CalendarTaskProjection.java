package com.practice.task_scheduler.entities.projections;

import java.time.LocalDateTime;

public interface CalendarTaskProjection {
    Long getId();
    String getTitle();
    String getDescription();
    String getPriority();
    Boolean getIsCompleted();
    LocalDateTime getDueDate();
    String getColor();
    String getListName();
    Integer getAssignedUsersCount();
    Integer getCompletedUsersCount();
}
