package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.entities.responses.TaskListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskListService {
    public TaskListResponse createTaskList(long userId, TaskListDTO taskListDTO);
    public TaskListResponse getTaskListById(long taskListId);
    public Page<TaskListResponse> getAllTaskListsByUserId(long userId, PageRequest pageRequest);
    public TaskListResponse updateTaskList(long taskListId, long userId, TaskListDTO taskListDTO);
    public void deleteTaskList(long taskListId, long userId);
    public TaskListResponse shareTaskList(long taskListId, long userId);
    public TaskListResponse joinTaskListByShareCode(String shareCode, long userId);
}
