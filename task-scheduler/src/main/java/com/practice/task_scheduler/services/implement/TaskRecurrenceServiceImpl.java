package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskRecurrenceDTO;
import com.practice.task_scheduler.entities.models.Notification;
import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.TaskRecurrence;
import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import com.practice.task_scheduler.entities.responses.TaskRecurrenceResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.RecurrenceException;
import com.practice.task_scheduler.exceptions.exception.TaskException;
import com.practice.task_scheduler.repositories.NotificationRepository;
import com.practice.task_scheduler.repositories.TaskRecurrenceRepository;
import com.practice.task_scheduler.repositories.TaskRepository;
import com.practice.task_scheduler.repositories.UserTaskAssignmentRepository;
import com.practice.task_scheduler.services.TaskRecurrenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRecurrenceServiceImpl implements TaskRecurrenceService {

    private final TaskRecurrenceRepository taskRecurrenceRepository;

    private final TaskRepository taskRepository;

    private final NotificationRepository notificationRepository;

    private final UserTaskAssignmentRepository userTaskAssignmentRepository;

    @Override
    @Transactional
    public TaskRecurrenceResponse createRecurrence(long taskId, TaskRecurrenceDTO taskRecurrenceDTO) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND));
        TaskRecurrence taskRecurrences = taskRecurrenceRepository.findByTaskId(taskId);
        if (taskRecurrenceRepository.findByTaskId(taskId) != null){
            throw new RecurrenceException(ErrorCode.RECURRENCE_ALREADY_EXIST, "You need to complete already recurrence");
        }

        TaskRecurrence taskRecurrence = TaskRecurrence.builder()
                .taskId(taskId)
                .recurrenceType(taskRecurrenceDTO.getRecurrenceType())
                .isActive(true)
                .build();

        if (taskRecurrenceDTO.getRecurrenceInterval() >= 1){
            taskRecurrence.setRecurrenceInterval(taskRecurrenceDTO.getRecurrenceInterval());
            if (task.getDueDate() != null){
                LocalDateTime localDateTime = updateNextDueDate(taskRecurrenceDTO.getRecurrenceType(), task.getDueDate());

                if (taskRecurrenceDTO.getRecurrenceEndDate() != null && taskRecurrenceDTO.getRecurrenceEndDate().isBefore(Objects.requireNonNull(localDateTime).toLocalDate())){
                    taskRecurrence.setNextDueDate(task.getDueDate());
                }else {
                    taskRecurrence.setNextDueDate(localDateTime);
                }
            } else {
                taskRecurrence.setNextDueDate(updateNextDueDate(taskRecurrenceDTO.getRecurrenceType(), LocalDateTime.now()));
            }
        } else taskRecurrence.setIsActive(false);

        if (taskRecurrenceDTO.getRecurrenceEndDate() != null) {
            if (taskRecurrenceDTO.getRecurrenceEndDate().isBefore(LocalDate.now())){
                taskRecurrence.setIsActive(false);
            }
            taskRecurrence.setRecurrenceEndDate(taskRecurrenceDTO.getRecurrenceEndDate());
        }

        taskRecurrenceRepository.save(taskRecurrence);
        return TaskRecurrenceResponse.toTaskRecurrence(taskRecurrence);
    }

    @Override
    @Transactional
    public void updateAllTaskRecurrenceSchedule() {
        List<TaskRecurrence> taskRecurrences = taskRecurrenceRepository.findByIsActiveTrue();
        for (TaskRecurrence recurrence : taskRecurrences){
            try {
                Task task = taskRepository.findById(recurrence.getTaskId()).orElse(null);
                if (task == null) {
                    recurrence.setIsActive(false);
                    taskRecurrenceRepository.save(recurrence);
                    return;
                }

                LocalDateTime now = LocalDateTime.now();

                if (recurrence.getNextDueDate() != null && recurrence.getNextDueDate().isBefore(now)) {
                    if (shouldContinueRecurrence(recurrence)) {
                        createNextTaskInstance(task, recurrence);

                        LocalDateTime currentNextDue = recurrence.getNextDueDate();
                        LocalDateTime newNextDue = updateNextDueDate(
                                recurrence.getRecurrenceType(),
                                currentNextDue
                        );

                        recurrence.setNextDueDate(newNextDue);
                        taskRecurrenceRepository.save(recurrence);

                    } else {
                        recurrence.setIsActive(false);
                        taskRecurrenceRepository.save(recurrence);
                    }
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @Transactional
    public void autoSaveTaskRecurrenceOnUpdate(Task task) { //
        TaskRecurrence taskRecurrence = taskRecurrenceRepository.findByTaskId(task.getId());
        if (taskRecurrence == null){
            return;
        }
        taskRecurrence.setRecurrenceInterval(taskRecurrence.getRecurrenceInterval() - 1);
        if (taskRecurrence.getRecurrenceInterval() >= 1){
            LocalDateTime nextDueDate = updateNextDueDate(taskRecurrence.getRecurrenceType(), task.getDueDate());

            if (taskRecurrence.getRecurrenceEndDate() != null){
                if (taskRecurrence.getRecurrenceEndDate().isBefore(Objects.requireNonNull(nextDueDate).toLocalDate())){
                    taskRecurrence.setIsActive(false);
                }
            }
            taskRecurrence.setNextDueDate(nextDueDate);
            taskRepository.updateNextDueDateById(task.getId(), nextDueDate);
        } else taskRecurrence.setIsActive(false);
        taskRecurrenceRepository.save(taskRecurrence);
    }

    @Override
    public TaskRecurrenceResponse getRecurrenceByTaskId(long taskId) {
        TaskRecurrence taskRecurrences = taskRecurrenceRepository.findByTaskId(taskId);
        if (taskRecurrenceRepository.findByTaskId(taskId) == null){
            throw new RecurrenceException(ErrorCode.RECURRENCE_NOT_FOUND);
        }
        return TaskRecurrenceResponse.toTaskRecurrence(taskRecurrences);
    }

    @Override
    @Transactional
    public TaskRecurrenceResponse updateRecurrence(long id, TaskRecurrenceDTO taskRecurrenceDTO) {
        TaskRecurrence taskRecurrence = taskRecurrenceRepository.findById(id)
                .orElseThrow(() -> new RecurrenceException(ErrorCode.RECURRENCE_NOT_FOUND));
        long taskId = taskRecurrence.getTaskId();

        taskRecurrence.setRecurrenceType(taskRecurrenceDTO.getRecurrenceType());

        if (taskRecurrenceDTO.getRecurrenceInterval() != null){
            taskRecurrence.setRecurrenceInterval(taskRecurrenceDTO.getRecurrenceInterval());
        }

        if (taskRecurrenceDTO.getIsActive() != null){
            taskRecurrence.setIsActive(taskRecurrenceDTO.getIsActive());
        }

        if (taskRecurrenceDTO.getRecurrenceEndDate() != null){
            if (taskRecurrenceDTO.getRecurrenceEndDate().isBefore(LocalDate.now())){
                taskRecurrence.setIsActive(false);
            }
            taskRecurrence.setRecurrenceEndDate(taskRecurrenceDTO.getRecurrenceEndDate());
        }
        taskRecurrenceRepository.updateTaskRecurrence(id, taskRecurrence);
        return TaskRecurrenceResponse.toTaskRecurrence(taskRecurrence);
    }

    @Override
    @Transactional
    public void deleteRecurrence(long id) {
        if (!taskRecurrenceRepository.existsById(id)) {
            throw new RecurrenceException(ErrorCode.RECURRENCE_NOT_FOUND);
        }
        taskRecurrenceRepository.deleteById(id);
    }

    private long hourAfterDue(LocalDateTime time){
        LocalDateTime currentTime = LocalDateTime.now();
        if (time.isBefore(currentTime)) return 0;
        int year = time.getYear() - currentTime.getYear();
        int month = time.getMonthValue() - currentTime.getMonthValue();
        int day = time.getDayOfMonth() - currentTime.getDayOfMonth();
        int hour = time.getHour() - currentTime.getHour();

        return (long) (hour==0?1:hour)*(day==0?1:day)*(month==0?1:month)*(year==0?1:year);
    }

    private LocalDateTime updateNextDueDate(TaskRecurrence.RecurrenceType type, LocalDateTime dateTime){
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        switch (type) {
            case DAILY -> date =  date.plusDays(1);
            case MONTHLY -> date = date.plusMonths(1);
            case YEARLY -> date = date.plusYears(1);
            case WEEKLY -> date = date.plusWeeks(1);
            default -> {return null;}
        }
        return LocalDateTime.of(date, time);
    }

    private boolean shouldContinueRecurrence(TaskRecurrence recurrence) {
        if (recurrence.getRecurrenceEndDate() != null &&
                LocalDate.now().isAfter(recurrence.getRecurrenceEndDate())) {
            return false;
        }

        return recurrence.getRecurrenceInterval() != null && recurrence.getRecurrenceInterval() > 0;
    }

    private void createNextTaskInstance(Task originalTask, TaskRecurrence recurrence) {
        Task newTask = Task.builder()
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .priority(originalTask.getPriority())
                .dueDate(recurrence.getNextDueDate())
                .taskListId(originalTask.getTaskListId())
                .createdBy(originalTask.getCreatedBy())
                .isCompleted(false)
                .build();

        Task savedTask = taskRepository.save(newTask);

        List<UserTaskAssignment> originalAssignments = userTaskAssignmentRepository.findByTaskId(originalTask.getId());
        List<UserTaskAssignment> newAssignments = originalAssignments.stream()
                .map(assignment -> UserTaskAssignment.builder()
                        .taskId(savedTask.getId())
                        .userId(assignment.getUserId())
                        .assignedBy(assignment.getAssignedBy())
                        .status(UserTaskAssignment.Status.IN_PROGRESS)
                        .build())
                .collect(Collectors.toList());

        userTaskAssignmentRepository.saveAll(newAssignments);

        // notification
        createRecurringTaskNotifications(savedTask, newAssignments);
    }

    private void createRecurringTaskNotifications(Task task, List<UserTaskAssignment> assignments) {
        List<Notification> notifications = assignments.stream()
                .map(assignment -> Notification.builder()
                        .userId(assignment.getUserId())
                        .taskId(task.getId())
                        .title("New Recurring Task")
                        .message("A new instance of recurring task '" + task.getTitle() +
                                "' is now available. Due: " +
                                (task.getDueDate() != null ? task.getDueDate() : "No due date"))
                        .notificationType(Notification.NotificationType.TASK_ASSIGNED)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
    }
}
