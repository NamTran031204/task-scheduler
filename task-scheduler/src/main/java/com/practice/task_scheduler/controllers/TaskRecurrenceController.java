package com.practice.task_scheduler.controllers;

import com.practice.task_scheduler.entities.dtos.TaskRecurrenceDTO;
import com.practice.task_scheduler.services.TaskRecurrenceService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/task_recurrence")
@RequiredArgsConstructor
@Validated
public class TaskRecurrenceController {

    private final TaskRecurrenceService taskRecurrenceService;

    @PostMapping("/task/{taskId}")
    public ResponseEntity<?> createRecurrence(
            @PathVariable("taskId") long taskId,
            @Valid @RequestBody TaskRecurrenceDTO taskRecurrenceDTO
    ){
        return ResponseEntity.ok(taskRecurrenceService.createRecurrence(taskId, taskRecurrenceDTO));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getRecurrenceByTaskId(@PathVariable("taskId") long taskId){
        return ResponseEntity.ok(taskRecurrenceService.getRecurrenceByTaskId(taskId));
    }

    @PutMapping("/update/{recurrenceId}")
    public ResponseEntity<?> updateRecurrence(
            @PathVariable("recurrenceId") long id,
            @RequestBody TaskRecurrenceDTO taskRecurrenceDTO
    ){
        return ResponseEntity.ok(taskRecurrenceService.updateRecurrence(id, taskRecurrenceDTO));
    }

    @DeleteMapping("/delete/{recurrenceId}")
    public ResponseEntity<?> deleteRecurrence(@PathVariable("recurrenceId") long id){
        taskRecurrenceService.deleteRecurrence(id);
        return ResponseEntity.ok("delete complete");
    }
}
