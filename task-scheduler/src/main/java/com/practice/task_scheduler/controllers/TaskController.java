package com.practice.task_scheduler.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.task_scheduler.entities.dtos.TaskDTO;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import com.practice.task_scheduler.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/task")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @PostMapping(value = "/user/{userId}/create")
    public ResponseEntity<?> createTask(
            @PathVariable("userId") long userId,
            @Valid @RequestBody TaskDTO taskDTO
    ) {
        return ResponseEntity.ok(taskService.createTask(userId, taskDTO));
    }

    @PutMapping(value = "/user/{userId}/create/task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(
            @PathVariable("userId") long userId,
            @PathVariable("taskId") long taskId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        return ResponseEntity.ok(taskService.uploadTaskFiles(taskId, userId, files));
    }

    @GetMapping("/user/{userId}/{taskId}")
    public ResponseEntity<?> getTaskById(
            @PathVariable("id") long taskId,
            @PathVariable("userId") long userId
    ) {
        return ResponseEntity.ok(taskService.getTaskById(taskId, userId));
    }

    @GetMapping("/user/{userId}/task-list/{taskListId}")
    public ResponseEntity<?> getTasksByTaskListId(
            @PathVariable("taskListId") long taskListId,
            @PathVariable("userId") long userId,
            @Param("record") int record,
            @Param("page") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page, record, Sort.by("createdAt").descending());
        Page<TaskResponse> taskPage = taskService.getTasksByTaskListId(taskListId, userId, pageRequest);
        return ResponseEntity.ok(taskPage);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTasksByUserId(
            @PathVariable("userId") long userId,
            @Param("record") int record,
            @Param("page") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page, record, Sort.by("createdAt").descending());
        Page<TaskResponse> taskPage = taskService.getTasksByUserId(userId, pageRequest);
        return ResponseEntity.ok(taskPage);
    }

    @PutMapping("/user/{userId}/update/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId,
            @Valid @RequestBody TaskDTO taskDTO
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, userId, taskDTO));
    }

    @DeleteMapping("/user/{userId}/delete/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        taskService.deleteTask(id, userId);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @PutMapping("/user/{userId}/complete/{id}")
    public ResponseEntity<?> completeTask(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return ResponseEntity.ok(taskService.completeTask(id, userId));
    }

    @PutMapping("/user/{userId}/assign/{id}/assign-to/{assignedToUserId}")
    public ResponseEntity<?> assignTask(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId,
            @PathVariable("assignedToUserId") long assignedToUserId
    ) {
        return ResponseEntity.ok(taskService.assignTask(id, userId, assignedToUserId));
    }

    @PutMapping("/user/{userId}/undo_complete/{taskId}")
    public ResponseEntity<?> revertTaskComplete(
            @PathVariable("userId") long userId,
            @PathVariable("taskId") long taskId
    ) {
        return ResponseEntity.ok(taskService.undoComplete(taskId, userId));
    }
}