package com.practice.task_scheduler.controllers;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.entities.responses.TaskListResponse;
import com.practice.task_scheduler.services.TaskListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
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
        return ResponseEntity.ok(taskListService.createTaskList(id, taskListDTO));

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskListById(@PathVariable("id") long id) {
        return ResponseEntity.ok(taskListService.getTaskListById(id));

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllTaskListsByUserId(
            @PathVariable("userId") long userId,
            @Param("record") int record,
            @Param("page") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page, record, Sort.by("createdAt").descending());
        Page<TaskListResponse> taskListPage = taskListService.getAllTaskListsByUserId(userId, pageRequest);

        return ResponseEntity.ok(taskListPage);
    }

    @PutMapping("/update/{id}/user/{userId}")
    public ResponseEntity<?> updateTaskList(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId,
            @Valid @RequestBody TaskListDTO taskListDTO
    ) {
        return ResponseEntity.ok(taskListService.updateTaskList(id, userId, taskListDTO));

    }

    @DeleteMapping("/delete/{id}/user/{userId}")
    public ResponseEntity<?> deleteTaskList(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        taskListService.deleteTaskList(id, userId);
        return ResponseEntity.ok("TaskList deleted successfully");
    }

    @PutMapping("/share/{id}/user/{userId}")
    public ResponseEntity<?> shareTaskList(
            @PathVariable("id") long id,
            @PathVariable("userId") long userId
    ) {
        return ResponseEntity.ok(taskListService.shareTaskList(id, userId));
    }

    @PostMapping("/join/{shareCode}/user/{userId}")
    public ResponseEntity<?> joinTaskListByShareCode(
            @PathVariable("shareCode") String shareCode,
            @PathVariable("userId") long userId
    ) {
        return ResponseEntity.ok(taskListService.joinTaskListByShareCode(shareCode, userId));
    }
}
