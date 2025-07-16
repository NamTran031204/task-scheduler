package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.UserDTO;
import com.practice.task_scheduler.entities.responses.UserResponse;

public interface UserService {
    public UserResponse register(UserDTO userDTO);
    public String login(String email, String password);
}
