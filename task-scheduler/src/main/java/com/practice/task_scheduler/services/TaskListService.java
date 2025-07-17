package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.entities.responses.TaskListResponse;

public interface TaskListService {
    public TaskListResponse createTaskList(long userId, TaskListDTO taskListDTO);
}
