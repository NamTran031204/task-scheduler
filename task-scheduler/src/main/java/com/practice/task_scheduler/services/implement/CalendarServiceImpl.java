package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.TaskRecurrence;
import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import com.practice.task_scheduler.entities.projections.CalendarTaskProjection;
import com.practice.task_scheduler.entities.responses.CalendarResponse;
import com.practice.task_scheduler.entities.responses.CalendarTaskResponse;
import com.practice.task_scheduler.repositories.TaskRepository;
import com.practice.task_scheduler.repositories.UserTaskAssignmentRepository;
import com.practice.task_scheduler.services.CalendarService;
import com.practice.task_scheduler.utils.RecurrenceIterator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final TaskRepository taskRepository;

    private final UserTaskAssignmentRepository userTaskAssignmentRepository;

    @Override
    public CalendarResponse getTasksForCalendar(Long userId, LocalDate startDate, LocalDate endDate) {

        // cau hinh multithread
        Map<String, List<CalendarTaskResponse>> tasksByDate = new ConcurrentHashMap<>();

        CompletableFuture<List<CalendarTaskProjection>> singleTasksForCalendar = loadSingleTasksData(userId, startDate, endDate);
        CompletableFuture<Void> singleProcessFuture = singleTasksForCalendar.thenAccept(singleTasks -> processSingleTasks(singleTasks, tasksByDate));

        CompletableFuture<List<Task>> recurringTaskForCalendar = loadRecurringTasksWithFullData(userId);
        CompletableFuture<Void> recurringProcessFuture = recurringTaskForCalendar.thenAccept(recurringTasks ->processRecurringTasks(recurringTasks, startDate, endDate, tasksByDate));

        CompletableFuture<Void> allProcessingComplete = CompletableFuture.allOf(
                singleProcessFuture,
                recurringProcessFuture
        );

        CompletableFuture<Void> sort = allProcessingComplete.thenRun( () -> tasksByDate.values().parallelStream().forEach(this::sortTasksInDay));

        return sort.thenApply(v -> CalendarResponse.builder()
                    .tasksByDate(new TreeMap<>(tasksByDate))
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalTasks(calculateTotalTasks(tasksByDate))
                    .taskCountsByPriority(calculatePriorityDistribution(tasksByDate))
                    .build())
                .join();
    }

    @Async("calendarTaskAsync")
    private CompletableFuture<List<CalendarTaskProjection>> loadSingleTasksData(
            long userId,
            LocalDate startDate,
            LocalDate endDate
    ){
        return CompletableFuture.completedFuture(taskRepository
                .findSingleTasksForCalendarWithAssignments(
                        userId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23,59,59)));
    }

    private void processSingleTasks(
            List<CalendarTaskProjection> singleTasksForCalendar,
            Map<String, List<CalendarTaskResponse>> tasksByDate){
        singleTasksForCalendar.parallelStream()
                .filter(task -> task.getDueDate() != null)
                .forEach(task -> {
                    String dateKey = task.getDueDate().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    tasksByDate.computeIfAbsent(dateKey, k -> new CopyOnWriteArrayList<>())
                            .add(convertProjectionToObject(task));
                });
    }

    @Async("calendarTaskAsync")
    private CompletableFuture<List<Task>> loadRecurringTasksWithFullData(Long userId) {
        List<Task> tasks = taskRepository.findRecurringTasksForUserBasic(userId);

        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(tasks);
        }

        List<Long> taskIds = tasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());

        List<TaskRecurrence> recurrences = taskRepository.findActiveRecurrencesByTaskIds(taskIds);

        List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskIdIn(taskIds);

        Map<Long, List<TaskRecurrence>> recurrencesByTask = recurrences.stream()
                .collect(Collectors.groupingBy(TaskRecurrence::getTaskId));

        Map<Long, List<UserTaskAssignment>> assignmentsByTask = assignments.stream()
                .collect(Collectors.groupingBy(UserTaskAssignment::getTaskId));

        tasks.forEach(task -> {
            task.setTaskRecurrences(recurrencesByTask.getOrDefault(task.getId(), new ArrayList<>()));
            task.setUserTaskAssignments(assignmentsByTask.getOrDefault(task.getId(), new ArrayList<>()));
        });

        return CompletableFuture.completedFuture(tasks);
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
                .assignedUsersCount(projection.getAssignedUsersCount())
                .completedUsersCount(projection.getCompletedUsersCount())
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

                    int totalAssignments = task.getUserTaskAssignments() != null ? task.getUserTaskAssignments().size() : 0;
                    int completedAssignments = task.getUserTaskAssignments() != null ?
                            (int) task.getUserTaskAssignments().stream()
                                    .filter(a -> a.getStatus() == UserTaskAssignment.Status.COMPLETED)
                                    .count() : 0;

                    LocalDate originalDueDate = task.getDueDate().toLocalDate();
                    if (!originalDueDate.isBefore(startDate) && !originalDueDate.isAfter(endDate) && recurrence.getRecurrenceInterval()>0) {
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
                                .assignedUsersCount(totalAssignments)
                                .completedUsersCount(completedAssignments)
                                .recurringInstanceId("original_" + task.getId())
                                .build();
                        tasksByDate.computeIfAbsent(originalDateKey, k -> new CopyOnWriteArrayList<>())
                                .add(originalTask);
                    }

                    RecurrenceIterator recurrenceIterator = new RecurrenceIterator(recurrence, startDate, endDate);
                    long interval = recurrence.getRecurrenceInterval();
                    while (recurrenceIterator.hasNext() && interval > 0) {
                        LocalDateTime nextDue = recurrenceIterator.next();
                        interval--;

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
                                .assignedUsersCount(totalAssignments)
                                .completedUsersCount(0)
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
