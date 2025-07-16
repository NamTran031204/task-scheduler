package com.practice.task_scheduler.entities.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @JsonProperty("username")
    @NotBlank(message = "username cannot be blank")
    private String username;

    @JsonProperty("email")
    @NotBlank(message = "email cannot be blank")
    @Email
    private String email;

    @JsonProperty("password")
    @NotBlank(message = "require password")
    private String password;

    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("created-at")
    private LocalDateTime createdAt;

    @JsonProperty("updated-at")
    private LocalDateTime updatedAt;
}
