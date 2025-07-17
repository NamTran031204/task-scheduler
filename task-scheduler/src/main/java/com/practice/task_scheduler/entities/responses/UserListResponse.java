package com.practice.task_scheduler.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
public class UserListResponse {
    private List<UserResponse> userResponses;
    private long totalPage;
}
