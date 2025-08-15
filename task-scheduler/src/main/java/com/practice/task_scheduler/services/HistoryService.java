package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.models.TaskHistory;

public interface HistoryService {
    public void insertHistory(long taskId, long userId, TaskHistory.HistoryAction action, String value, String description);
    public TaskHistory getHistoryOfTask(long taskId);
    public void deleteHistory(long historyId);
}
