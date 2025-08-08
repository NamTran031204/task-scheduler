package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.TaskRecurrence;
import com.practice.task_scheduler.entities.projections.CalendarTaskProjection;
import com.practice.task_scheduler.entities.responses.CalendarResponse;
import com.practice.task_scheduler.entities.responses.CalendarTaskResponse;
import com.practice.task_scheduler.repositories.TaskRepository;
import com.practice.task_scheduler.services.CalendarService;
import com.practice.task_scheduler.utils.RecurrenceIterator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final TaskRepository taskRepository;

    @Override
    public CalendarResponse getTasksForCalendar(Long userId, LocalDate startDate, LocalDate endDate) {
        List<CalendarTaskProjection> singleTasksForCalendar = taskRepository.findSingleTasksForCalendar(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(23,59,59));

        List<Task> recurringTaskForCalendar = taskRepository.findRecurringTasksForUser(userId);

        Map<String, List<CalendarTaskResponse>> tasksByDate = new ConcurrentHashMap<>();

        singleTasksForCalendar.parallelStream()
                .filter(task -> task.getDueDate() != null)
                .forEach(task -> {
                    String dateKey = task.getDueDate().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    tasksByDate.computeIfAbsent(dateKey, k -> new CopyOnWriteArrayList<>())
                            .add(convertProjectionToObject(task));
                });

        processRecurringTasks(recurringTaskForCalendar, startDate, endDate, tasksByDate);

        tasksByDate.values().parallelStream().forEach(this::sortTasksInDay);

        return CalendarResponse.builder()
                .tasksByDate(new TreeMap<>(tasksByDate))
                .startDate(startDate)
                .endDate(endDate)
                .totalTasks(calculateTotalTasks(tasksByDate))
                .taskCountsByPriority(calculatePriorityDistribution(tasksByDate))
                .build();
    }

    private CalendarTaskResponse convertProjectionToObject(CalendarTaskProjection projection){
        return CalendarTaskResponse.builder()
                .id(projection.getId())
                .title(projection.getTitle())
                .description(projection.getDescription())
                .dueDate(projection.getDueDate())
                .priority(projection.getPriority())
                .isCompleted(projection.getIsCompleted())
                .color(projection.getColor())
                .listName(projection.getListName())
                .isRecurring(false)
                .build();
    }

    private void processRecurringTasks(
            List<Task> recurringTasks,
            LocalDate startDate,
            LocalDate endDate,
            Map<String, List<CalendarTaskResponse>> tasksByDate
    ) {
        recurringTasks.parallelStream()
                .filter(this::hasActiveRecurrence)
                .forEach(task -> {
                    TaskRecurrence recurrence = task.getTaskRecurrences().stream()
                            .filter(TaskRecurrence::getIsActive)
                            .findFirst()
                            .orElse(null);

                    if (recurrence == null) return;

                    LocalDate originalDueDate = task.getDueDate().toLocalDate();
                    if (!originalDueDate.isBefore(startDate) && !originalDueDate.isAfter(endDate)) {
                        String originalDateKey = originalDueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        CalendarTaskResponse originalTask = CalendarTaskResponse.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .dueDate(task.getDueDate())
                                .priority(task.getPriority().name())
                                .isCompleted(task.getIsCompleted())
                                .color(task.getTaskList().getColor())
                                .listName(task.getTaskList().getName())
                                .isRecurring(true)
                                .recurringInstanceId("original_" + task.getId())
                                .build();
                        tasksByDate.computeIfAbsent(originalDateKey, k -> new CopyOnWriteArrayList<>())
                                .add(originalTask);
                    }

                    RecurrenceIterator recurrenceIterator = new RecurrenceIterator(recurrence, startDate, endDate);
                    while (recurrenceIterator.hasNext()) {
                        LocalDateTime nextDue = recurrenceIterator.next();

                        if (nextDue.toLocalDate().equals(originalDueDate)) {
                            continue;
                        }

                        String dateKey = nextDue.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        CalendarTaskResponse calendarTaskResponse = CalendarTaskResponse.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .dueDate(nextDue)
                                .priority(task.getPriority().name())
                                .isCompleted(task.getIsCompleted())
                                .color(task.getTaskList().getColor())
                                .listName(task.getTaskList().getName())
                                .isRecurring(true)
                                .recurringInstanceId(generateInstanceId(task.getId(), nextDue))
                                .build();

                        tasksByDate.computeIfAbsent(dateKey, k -> new CopyOnWriteArrayList<>())
                                .add(calendarTaskResponse);
                    }
                });
    }

    private boolean hasActiveRecurrence(Task task){
        return task.getTaskRecurrences() != null &&
                task.getTaskRecurrences().stream().anyMatch(TaskRecurrence::getIsActive);
    }

    private String generateInstanceId(Long taskId, LocalDateTime dueDate) {
        return taskId + "_" + dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private void sortTasksInDay(List<CalendarTaskResponse> tasks) {
        tasks.sort(Comparator
                .comparing((CalendarTaskResponse t) -> getPriorityWeight(t.getPriority()), Comparator.reverseOrder())
                .thenComparing(t -> t.getDueDate() != null ? t.getDueDate() : LocalDateTime.MAX)
                .thenComparing(CalendarTaskResponse::getTitle));
    }

    private int getPriorityWeight(String priority) {
        switch (priority.toUpperCase()) {
            case "URGENT": return 4;
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    private int calculateTotalTasks(Map<String, List<CalendarTaskResponse>> tasksByDate) {
        return tasksByDate.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    private Map<String, Integer> calculatePriorityDistribution(Map<String, List<CalendarTaskResponse>> tasksByDate) {
        Map<String, Integer> distribution = new HashMap<>();
        tasksByDate.values().stream()
                .flatMap(List::stream)
                .forEach(task -> {
                    String priority = task.getPriority();
                    distribution.merge(priority, 1, Integer::sum);
                });
        return distribution;
    }

}
