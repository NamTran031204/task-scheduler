package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.responses.CalendarResponse;

import java.time.LocalDate;

public interface CalendarService {
    CalendarResponse getTasksForCalendar(Long userId, LocalDate startDate, LocalDate endDate);
}
