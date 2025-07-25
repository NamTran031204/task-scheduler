package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.TaskReminderDTO;
import com.practice.task_scheduler.entities.responses.TaskReminderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface TaskReminderService {
    // Auto create/update reminder when task due_date changes (called internally)
    void autoCreateOrUpdateReminder(Long taskId, Long userId);

    // Get reminder by ID
    TaskReminderResponse getReminderById(Long reminderId, Long userId);

    // Get all reminders for a task
    List<TaskReminderResponse> getRemindersByTaskId(Long taskId, Long userId);

    // Get all reminders for a user
    Page<TaskReminderResponse> getRemindersByUserId(Long userId, PageRequest pageRequest);

    // Get pending reminders (not sent yet)
    Page<TaskReminderResponse> getPendingRemindersByUserId(Long userId, PageRequest pageRequest);

    // Update reminder settings
    TaskReminderResponse updateReminder(Long reminderId, Long userId, TaskReminderDTO reminderDTO);

    // Delete reminder
    void deleteReminder(Long reminderId, Long userId);

    // Mark reminder as sent (system use)
    void markReminderAsSent(Long reminderId);

    // Send notifications for due reminders (system job)
    List<TaskReminderResponse> sendDueReminders();
}