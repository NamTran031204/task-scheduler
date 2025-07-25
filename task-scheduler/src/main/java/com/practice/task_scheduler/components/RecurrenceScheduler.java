package com.practice.task_scheduler.components;

import com.practice.task_scheduler.services.TaskRecurrenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class RecurrenceScheduler {

    private final TaskRecurrenceService taskRecurrenceService;

    @Scheduled(fixedRate = 60000) // every 5 minutes
    private void TaskRecurrenceSchedule(){
        taskRecurrenceService.updateAllTaskRecurrenceSchedule();
    }
}