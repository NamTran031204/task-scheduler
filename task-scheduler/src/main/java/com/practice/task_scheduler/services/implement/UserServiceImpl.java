package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.UserDTO;
import com.practice.task_scheduler.entities.models.User;
import com.practice.task_scheduler.entities.responses.UserResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.repositories.UserRepository;
import com.practice.task_scheduler.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse register(UserDTO userDTO) {

        if (userRepository.findUserByEmail(userDTO.getEmail()).isPresent()){
            throw new UserRequestException(ErrorCode.USER_EXISTED);
        }

        if(userRepository.findUserByPassword(userDTO.getPassword()).isPresent()){
            throw new UserRequestException(ErrorCode.USER_PASSWORD_FOUND);
        }

        User user = User.builder()
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .username(userDTO.getUsername())
                .createdAt(userDTO.getCreatedAt())
                .updatedAt(userDTO.getUpdatedAt())
                .password(userDTO.getPassword())
                .build();

        userRepository.save(user);

        UserResponse userResponse = UserResponse.toUser(user);
        return userResponse;
    }

    @Override
    public String login(String email, String password) {

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        if (!user.getPassword().equals(password)){
            throw new UserRequestException(ErrorCode.USER_PASSWORD_INCORRECT);
        }
        return "login success";
    }
}
