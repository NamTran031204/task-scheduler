package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.TaskHistory;
import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.TaskException;
import com.practice.task_scheduler.exceptions.exception.UserTaskException;
import com.practice.task_scheduler.repositories.TaskRepository;
import com.practice.task_scheduler.repositories.UserTaskAssignmentRepository;
import com.practice.task_scheduler.repositories.UserTaskListRepository;
import com.practice.task_scheduler.services.HistoryService;
import com.practice.task_scheduler.services.TaskService;
import com.practice.task_scheduler.services.UserTaskAssignmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTaskAssignmentServiceImpl implements UserTaskAssignmentService {

    private final UserTaskAssignmentRepository userTaskAssignmentRepository;
    private final UserTaskListRepository userTaskListRepository;
    private final TaskRepository taskRepository;

    private final HistoryService historyService;

    @Override
    @Transactional
    public void createAssignment(List<Long> assignedUsers, Task task) {
        Set<Long> uniqueAssignedUsers = new HashSet<>();
        if (assignedUsers == null || assignedUsers.isEmpty()) {
            uniqueAssignedUsers.add(task.getCreatedBy());
        } else {
            List<Long> validUsers = assignedUsers.stream()
                    .distinct()
                    .filter(userId -> userTaskListRepository.existsByUserIdAndTaskListId(userId, task.getTaskListId()))
                    .toList();

            if (validUsers.isEmpty()) {
                uniqueAssignedUsers.add(task.getCreatedBy());
            } else {
                uniqueAssignedUsers.addAll(validUsers);
            }
        }

        Set<Long> existingAssignees = userTaskAssignmentRepository.findUserIdsByTaskId(task.getId());
        uniqueAssignedUsers.removeAll(existingAssignees);

        if (uniqueAssignedUsers.isEmpty()) {
            return;
        }

        List<UserTaskAssignment> assignments = uniqueAssignedUsers.stream()
                .map(userId -> UserTaskAssignment.builder()
                        .taskId(task.getId())
                        .userId(userId)
                        .assignedBy(task.getCreatedBy())
                        .status(UserTaskAssignment.Status.IN_PROGRESS)
                        .build())
                .collect(Collectors.toList());

        userTaskAssignmentRepository.saveAll(assignments);

        // history
        assignments.forEach(assignment ->
                historyService.insertHistory(
                        assignment.getTaskId(),
                        task.getCreatedBy(),
                        TaskHistory.HistoryAction.ASSIGNED,
                        "Assigned to user " + assignment.getUserId(),
                        "Task assignment created"
                )
        );
    }

    @Override
    @Transactional
    public String userTaskStatusChange(long userId, long taskId, UserTaskAssignment.Status status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        UserTaskAssignment userTaskAssignment = task.getUserTaskAssignments().stream()
                .filter(assignment -> assignment.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserTaskException(ErrorCode.USERTASK_NOT_FOUND));

        userTaskAssignment.setStatus(status);
        updateTaskCompletionStatusById(taskId);

        historyService.insertHistory(
                taskId,
                userId,
                TaskHistory.HistoryAction.UPDATED,
                userTaskAssignment.getStatus().toString(),
                userId + (status.equals(UserTaskAssignment.Status.COMPLETED) ? "Completed" : "Undo complete") + taskId);

        return userId + (status.equals(UserTaskAssignment.Status.COMPLETED) ? "Completed" : "Undo complete") + taskId;
    }

    @Override
    public void deleteAssignedUserTask(long userId, Task task) {
        UserTaskAssignment assignment = userTaskAssignmentRepository.findByTaskIdAndUserId(task.getId(), userId)
                .orElse(null);
        if (assignment == null) return;
        userTaskAssignmentRepository.delete(assignment);

        updateTaskCompletionStatusById(task.getId());

        historyService.insertHistory(
                task.getId(),
                userId,
                TaskHistory.HistoryAction.UPDATED,
                "Assignment removed",
                "User " + userId + " removed from task " + task.getId()
        );
    }

    private void updateTaskCompletionStatusById(Long taskId) {
        long totalAssignments = userTaskAssignmentRepository.countAssignmentsByTaskId(taskId);
        long completedAssignments = userTaskAssignmentRepository.countCompletedAssignmentsByTaskId(taskId);

        boolean taskCompleted = (totalAssignments > 0 && totalAssignments == completedAssignments);
        taskRepository.updateCompletionStatus(taskId, taskCompleted);
    }

}
