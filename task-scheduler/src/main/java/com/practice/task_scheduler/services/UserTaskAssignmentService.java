package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.UserTaskAssignment;

import java.util.List;

public interface UserTaskAssignmentService {
    public void createAssignment(List<Long> assignedUsers, Task task);
    public String userTaskStatusChange(long userId, long taskId, UserTaskAssignment.Status status);
    public void deleteAssignedUserTask(long userId, Task task);
}
