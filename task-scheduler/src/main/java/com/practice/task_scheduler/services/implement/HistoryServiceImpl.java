package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.models.TaskHistory;
import com.practice.task_scheduler.repositories.TaskHistoryRepository;
import com.practice.task_scheduler.services.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

    @Override
    @Async("historyTaskAsync")
    public void insertHistory(long taskId, long userId, TaskHistory.HistoryAction action, String value, String description) {
        String oldHistory = taskHistoryRepository.findByTaskIdAndUserId(taskId, userId)
                .orElse(null);
        TaskHistory taskHistory = TaskHistory.builder()
                .taskId(taskId)
                .userId(userId)
                .action(action)
                .oldValue(oldHistory)
                .newValue(value)
                .description(description)
                .build();
        taskHistoryRepository.save(taskHistory);
    }

    @Override
    public TaskHistory getHistoryOfTask(long taskId) {
        return taskHistoryRepository.findLatestByTaskId(taskId)
                .orElse(null);
    }

    public List<TaskHistory> getTaskHistoryWithAssignments(long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Override
    public void deleteHistory(long historyId) {
        if (taskHistoryRepository.existsById(historyId)) {
            taskHistoryRepository.deleteById(historyId);
        }
    }
}
