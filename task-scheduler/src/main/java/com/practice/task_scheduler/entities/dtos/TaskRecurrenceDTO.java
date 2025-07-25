package com.practice.task_scheduler.entities.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.task_scheduler.entities.models.TaskRecurrence;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRecurrenceDTO {
    @NonNull
    @JsonProperty("recurrence_type")
    private TaskRecurrence.RecurrenceType recurrenceType;

    @JsonProperty("recurrence_interval")
    private Integer recurrenceInterval = 1;

    @JsonProperty("recurrence_end_date")
    private LocalDate recurrenceEndDate;

    @JsonProperty("is_active")
    private Boolean isActive = true; // only use this field on Json for update
}
