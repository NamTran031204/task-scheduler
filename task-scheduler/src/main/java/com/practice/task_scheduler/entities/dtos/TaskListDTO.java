package com.practice.task_scheduler.entities.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.task_scheduler.entities.models.User;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListDTO {

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("color")
    private String color;

}
