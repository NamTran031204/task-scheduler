package com.practice.task_scheduler.services;

import com.practice.task_scheduler.entities.dtos.UserDTO;
import com.practice.task_scheduler.entities.responses.UserResponse;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    public UserResponse register(UserDTO userDTO);
    public String login(String email, String password);
    public String updateAvatar(Long id, MultipartFile file) throws RuntimeException;

    public UserResponse getUserById(long id);
    public Page<UserResponse> getAllUser(PageRequest pageRequest);

    void updateInfor(long id, String username, String fullName, MultipartFile avatar);
    void deleteUser(long id) throws UserRequestException;
}
