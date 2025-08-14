package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskReminderDTO;
import com.practice.task_scheduler.entities.models.*;
import com.practice.task_scheduler.entities.responses.TaskReminderResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.TaskException;
import com.practice.task_scheduler.exceptions.exception.TaskListException;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.repositories.*;
import com.practice.task_scheduler.services.TaskReminderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskReminderServiceImpl implements TaskReminderService {

    private final TaskReminderRepository taskReminderRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskListRepository taskListRepository;

    private final UserTaskListRepository userTaskListRepository;

    private final NotificationRepository notificationRepository;

    private final UserTaskAssignmentRepository userTaskAssignmentRepository;

    @Override
    @Transactional
    public void autoCreateOrUpdateReminder(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        validateTaskAccess(task, userId);

        TaskReminder reminder = taskReminderRepository.findByTaskIdAndCreatedBy(taskId, userId)
                .orElse(null);

        if (reminder == null) {
            int minutes = getMinuteBeforeNow(task.getDueDate());
            LocalDateTime remindAt = task.getDueDate().minusMinutes(Math.min(minutes, 30));

            reminder = TaskReminder.builder()
                    .taskId(taskId)
                    .remindAt(remindAt)
                    .message("Task '" + task.getTitle() + "' is due soon!")
                    .remindType(TaskReminder.RemindType.PUSH)
                    .isSent(false)
                    .createdBy(userId)
                    .build();
        } else {
            LocalDateTime remindAt = task.getDueDate().minusMinutes(30);
            reminder.setRemindAt(remindAt);
            reminder.setMessage("Task '" + task.getTitle() + "' is due soon!");
            reminder.setIsSent(false);
            reminder.setSentAt(null);
        }

        taskReminderRepository.save(reminder);
    }

    @Override
    public TaskReminderResponse getReminderById(Long reminderId, Long userId) {
        TaskReminder reminder = taskReminderRepository.findById(reminderId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Reminder not found"));

        validateReminderAccess(reminder, userId);

        return TaskReminderResponse.toTaskReminder(reminder);
    }

    @Override
    public List<TaskReminderResponse> getRemindersByTaskId(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));
        validateTaskAccess(task, userId);

        List<TaskReminder> reminders = taskReminderRepository.findByTaskIdOrderByRemindAtAsc(taskId);
        return reminders.stream()
                .map(TaskReminderResponse::toTaskReminder)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TaskReminderResponse> getRemindersByUserId(Long userId, PageRequest pageRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        Page<TaskReminder> reminderPage = taskReminderRepository
                .findByCreatedByOrderByRemindAtAsc(userId, pageRequest);

        return reminderPage.map(TaskReminderResponse::toTaskReminder);
    }

    @Override
    public Page<TaskReminderResponse> getPendingRemindersByUserId(Long userId, PageRequest pageRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        Page<TaskReminder> reminderPage = taskReminderRepository
                .findPendingByCreatedByOrderByRemindAtAsc(userId, pageRequest);

        return reminderPage.map(TaskReminderResponse::toTaskReminder);
    }

    @Override
    @Transactional
    public TaskReminderResponse updateReminder(Long reminderId, Long userId, TaskReminderDTO reminderDTO) {
        TaskReminder reminder = taskReminderRepository.findById(reminderId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Reminder not found"));

        validateReminderAccess(reminder, userId);

        boolean ok = false;
        if (reminderDTO.getRemindAt() != null) {
            reminder.setRemindAt(reminderDTO.getRemindAt());
            ok = true;
        }
        if (reminderDTO.getMessage() != null) {
            reminder.setMessage(reminderDTO.getMessage());
        }
        if (reminderDTO.getRemindType() != null) {
            reminder.setRemindType(reminderDTO.getRemindType());
        }
        if (reminderDTO.getMinutesBeforeDue() != null && ok != true) {
            Task task = taskRepository.findById(reminder.getTaskId())
                    .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

            if (task.getDueDate() != null) {
                int minutes = getMinuteBeforeNow(task.getDueDate());
                int min = reminderDTO.getMinutesBeforeDue();
                LocalDateTime remindAt = task.getDueDate().minusMinutes(Math.min(min, minutes));
                reminder.setRemindAt(remindAt);
            }
        }

        reminder.setSentAt(null);
        reminder.setIsSent(false);

        TaskReminder updatedReminder = taskReminderRepository.save(reminder);
        return TaskReminderResponse.toTaskReminder(updatedReminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Long reminderId, Long userId) {
        TaskReminder reminder = taskReminderRepository.findById(reminderId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Reminder not found"));

        validateReminderAccess(reminder, userId);

        taskReminderRepository.delete(reminder);
    }

    @Override
    @Transactional
    public void markReminderAsSent(Long reminderId) { // tích hợp email để send notification
        TaskReminder reminder = taskReminderRepository.findById(reminderId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Reminder not found"));

        reminder.setIsSent(true);
        reminder.setSentAt(LocalDateTime.now());
        taskReminderRepository.save(reminder);
    }

    @Override
    @Transactional
    public List<TaskReminderResponse> sendDueReminders() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<TaskReminder> dueReminders = taskReminderRepository.findDueReminders(currentTime);

        return dueReminders.stream().map(reminder -> {
            try {
                Task task = taskRepository.findById(reminder.getTaskId()).orElse(null);
                if (task == null) return null;

                List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskId(task.getId());
                List<Notification> notifications = assignments.stream()
                        .filter(assignment -> assignment.getStatus() == UserTaskAssignment.Status.IN_PROGRESS)
                        .map(assignment -> Notification.builder()
                                .userId(assignment.getUserId())
                                .taskId(reminder.getTaskId())
                                .title("Task Reminder!")
                                .message(reminder.getMessage())
                                .notificationType(Notification.NotificationType.REMINDER)
                                .isRead(false)
                                .build())
                        .collect(Collectors.toList());

                notificationRepository.saveAll(notifications);
                markReminderAsSent(reminder.getId());
                return TaskReminderResponse.toTaskReminder(reminder);
            } catch (Exception e) {
                System.err.println("Failed to send reminder " + reminder.getId() + ": " + e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void validateReminderAccess(TaskReminder reminder, Long userId) {
        if (!reminder.getCreatedBy().equals(userId)) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Access denied to this reminder");
        }
    }

    private void validateTaskAccess(Task task, Long userId) {
        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        boolean hasAccess = userTaskAssignmentRepository.existsByTaskIdAndUserId(task.getId(), userId) ||
                userTaskAssignmentRepository.existsByTaskIdAndAssignedBy(task.getId(), userId) ||
                userTaskListRepository.existsByUserIdAndTaskListId(userId, task.getTaskListId()) ||
                task.getCreatedBy().equals(userId);

        if (!hasAccess) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Access denied to this task");
        }
    }

    private int getMinuteBeforeNow(LocalDateTime time){
        LocalDateTime currentTime = LocalDateTime.now();
        if (time.isBefore(currentTime)) return 0;
        int year = time.getYear() - currentTime.getYear();
        int month = time.getMonthValue() - currentTime.getMonthValue();
        int day = time.getDayOfMonth() - currentTime.getDayOfMonth();
        int hour = time.getHour() - currentTime.getHour();
        int minute = time.getMinute() - currentTime.getMinute();

        return (minute==0?1:minute)*(hour==0?1:hour)*(day==0?1:day)*(month==0?1:month)*(year==0?1:year);
    }
}