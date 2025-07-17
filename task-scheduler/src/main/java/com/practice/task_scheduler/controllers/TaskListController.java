package com.practice.task_scheduler.controllers;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.services.TaskListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/task-list")
@RequiredArgsConstructor
@Validated
public class TaskListController {

    private final TaskListService taskListService;

    @PostMapping("/create/{id}")
    public ResponseEntity<?> createList(
            @PathVariable("id") long id,
            @Valid @RequestBody TaskListDTO taskListDTO
    ){
        try {
            return ResponseEntity.ok(taskListService.createTaskList(id, taskListDTO));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
