package com.practice.task_scheduler.controllers;

import com.practice.task_scheduler.entities.dtos.TaskReminderDTO;
import com.practice.task_scheduler.entities.responses.TaskReminderResponse;
import com.practice.task_scheduler.services.TaskReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/reminder")
@RequiredArgsConstructor
@Validated
public class TaskReminderController {

    private final TaskReminderService taskReminderService;

    @GetMapping("/user/{userId}/{reminderId}")
    public ResponseEntity<?> getReminderById(
            @PathVariable("reminderId") Long reminderId,
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(taskReminderService.getReminderById(reminderId, userId));
    }

    @GetMapping("/user/{userId}/task/{taskId}")
    public ResponseEntity<?> getRemindersByTaskId(
            @PathVariable("taskId") Long taskId,
            @PathVariable("userId") Long userId
    ) {
        List<TaskReminderResponse> reminders = taskReminderService.getRemindersByTaskId(taskId, userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRemindersByUserId(
            @PathVariable("userId") Long userId,
            @Param("record") int record,
            @Param("page") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page, record, Sort.by("remindAt").ascending());
        Page<TaskReminderResponse> reminderPage = taskReminderService.getRemindersByUserId(userId, pageRequest);
        return ResponseEntity.ok(reminderPage);
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<?> getPendingRemindersByUserId(
            @PathVariable("userId") Long userId,
            @Param("record") int record,
            @Param("page") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page, record, Sort.by("remindAt").ascending());
        Page<TaskReminderResponse> reminderPage = taskReminderService.getPendingRemindersByUserId(userId, pageRequest);
        return ResponseEntity.ok(reminderPage);
    }

    @PutMapping("/user/{userId}/update/{reminderId}")
    public ResponseEntity<?> updateReminder(
            @PathVariable("reminderId") Long reminderId,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TaskReminderDTO reminderDTO
    ) {
        return ResponseEntity.ok(taskReminderService.updateReminder(reminderId, userId, reminderDTO));
    }

    @DeleteMapping("/delete/{reminderId}/user/{userId}")
    public ResponseEntity<?> deleteReminder(
            @PathVariable("reminderId") Long reminderId,
            @PathVariable("userId") Long userId
    ) {
        taskReminderService.deleteReminder(reminderId, userId);
        return ResponseEntity.ok("Reminder deleted successfully");
    }

    @PostMapping("/system/send-due-reminders")
    public ResponseEntity<?> sendDueReminders() {
        List<TaskReminderResponse> sentReminders = taskReminderService.sendDueReminders();
        return ResponseEntity.ok("Sent " + sentReminders.size() + " reminders");
    }
}