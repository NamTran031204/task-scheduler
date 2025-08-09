package com.practice.task_scheduler.utils;

import com.practice.task_scheduler.entities.models.TaskRecurrence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

// custom lai Iterator de giai quyet bai toan thoi gian
public class RecurrenceIterator implements Iterator<LocalDateTime> {
    private final TaskRecurrence recurrence;
    private final LocalDate endDate;
    private LocalDateTime current;
    private final LocalDate recurrenceEndDate;

    public RecurrenceIterator(TaskRecurrence recurrence, LocalDate startDate, LocalDate endDate) {
        this.recurrence = recurrence;
        this.endDate = endDate;
        this.recurrenceEndDate = recurrence.getRecurrenceEndDate();
        this.current = findOptimalStartPoint(recurrence, startDate);
    }

    private LocalDateTime findOptimalStartPoint(TaskRecurrence recurrence, LocalDate startDate) {
        LocalDateTime nextDue = recurrence.getNextDueDate();
        LocalDate nextDueDate = nextDue.toLocalDate();

        if (!nextDueDate.isBefore(startDate)) {
            return nextDue;
        }

        LocalDateTime calculatedStart = nextDue;

        switch (recurrence.getRecurrenceType()) {
            case DAILY:
                long daysBetween = ChronoUnit.DAYS.between(nextDueDate, startDate);
                long cycles = (daysBetween + recurrence.getRecurrenceInterval() - 1) / recurrence.getRecurrenceInterval();
                calculatedStart = nextDue.plusDays(cycles * recurrence.getRecurrenceInterval());
                break;

            case WEEKLY:
                long weeksBetween = ChronoUnit.WEEKS.between(nextDueDate, startDate);
                long weekCycles = (weeksBetween + recurrence.getRecurrenceInterval() - 1) / recurrence.getRecurrenceInterval();
                calculatedStart = nextDue.plusWeeks(weekCycles * recurrence.getRecurrenceInterval());
                break;

            case MONTHLY:
                long monthsBetween = ChronoUnit.MONTHS.between(
                        YearMonth.from(nextDueDate), YearMonth.from(startDate));
                long monthCycles = (monthsBetween + recurrence.getRecurrenceInterval() - 1) / recurrence.getRecurrenceInterval();
                calculatedStart = nextDue.plusMonths(monthCycles * recurrence.getRecurrenceInterval());
                break;

            case YEARLY:
                long yearsBetween = ChronoUnit.YEARS.between(nextDueDate, startDate);
                long yearCycles = (yearsBetween + recurrence.getRecurrenceInterval() - 1) / recurrence.getRecurrenceInterval();
                calculatedStart = nextDue.plusYears(yearCycles * recurrence.getRecurrenceInterval());
                break;
        }

        while (calculatedStart.toLocalDate().isBefore(startDate)) {
            calculatedStart = calculateNext(calculatedStart);
        }

        return calculatedStart;
    }

    private LocalDateTime calculateNext(LocalDateTime current) {
        return switch (recurrence.getRecurrenceType()) {
            case DAILY -> current.plusDays(recurrence.getRecurrenceInterval());
            case WEEKLY -> current.plusWeeks(recurrence.getRecurrenceInterval());
            case MONTHLY -> current.plusMonths(recurrence.getRecurrenceInterval());
            case YEARLY -> current.plusYears(recurrence.getRecurrenceInterval());
            default -> null;
        };
    }

    @Override
    public boolean hasNext() {
        return current != null &&
                current.toLocalDate().isBefore(endDate.plusDays(1)) &&
                (recurrenceEndDate == null || current.toLocalDate().isBefore(recurrenceEndDate.plusDays(1)));
    }

    @Override
    public LocalDateTime next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        LocalDateTime result = current;
        current = calculateNext(current);
        return result;
    }

}