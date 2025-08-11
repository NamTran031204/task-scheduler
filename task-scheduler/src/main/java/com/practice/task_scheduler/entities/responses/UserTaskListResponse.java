package com.practice.task_scheduler.entities.responses;

import com.practice.task_scheduler.entities.models.UserTaskList;
import lombok.*;
import org.antlr.v4.runtime.misc.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTaskListResponse {
    Map<UserTaskList.Role, List<Pair<UserResponse, LocalDateTime>>> userByRoleAndJoinedAt;
}
