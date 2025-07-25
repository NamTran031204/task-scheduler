package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.TaskRecurrenceDTO;
import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.TaskRecurrence;
import com.practice.task_scheduler.entities.responses.TaskRecurrenceResponse;

public interface TaskRecurrenceService {

    TaskRecurrenceResponse createRecurrence(long taskId, TaskRecurrenceDTO taskRecurrenceDTO);
    void updateAllTaskRecurrenceSchedule();
    void autoSaveTaskRecurrenceOnUpdate(long taskId);
    TaskRecurrenceResponse getRecurrenceByTaskId(long taskId);
    TaskRecurrenceResponse updateRecurrence(long id, TaskRecurrenceDTO taskRecurrenceDTO);
    void deleteRecurrence(long id);
}
