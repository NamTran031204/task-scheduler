package com.practice.task_scheduler.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponse {
    private Map<String, List<CalendarTaskResponse>> tasksByDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalTasks;
    private Map<String, Integer> taskCountsByPriority;
}