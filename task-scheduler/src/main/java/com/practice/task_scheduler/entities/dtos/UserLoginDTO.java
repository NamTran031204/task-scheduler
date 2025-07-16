package com.practice.task_scheduler.entities.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserLoginDTO {
    @JsonProperty("email")
    @NotBlank(message = "email cannot blank")
    private String email;

    @JsonProperty("password")
    @NotBlank(message = "require password")
    private String password;
}
