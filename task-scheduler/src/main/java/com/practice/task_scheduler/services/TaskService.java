package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.TaskDTO;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(long userId, TaskDTO taskDTO);
    String uploadTaskFiles(long taskId, long userId, List<MultipartFile> files);

    TaskResponse getTaskById(long taskId, long userId);
    Page<TaskResponse> getTasksByTaskListId(long taskListId, long userId, PageRequest pageRequest);
    Page<TaskResponse> getTasksByUserId(long userId, PageRequest pageRequest);
    TaskResponse updateTask(long taskId, long userId, TaskDTO taskDTO);
    void deleteTask(long taskId, long userId);
    TaskResponse completeTask(long taskId, long userId);
    TaskResponse assignTask(long taskId, long userId, long assignedToUserId);
}