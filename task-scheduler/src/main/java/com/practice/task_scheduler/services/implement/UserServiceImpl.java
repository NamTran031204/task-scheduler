package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.UserDTO;
import com.practice.task_scheduler.entities.models.User;
import com.practice.task_scheduler.entities.responses.UserResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.repositories.UserRepository;
import com.practice.task_scheduler.services.UserService;
import com.practice.task_scheduler.utils.StoreFile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
                .isActive(true)
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

    @Override
    public String updateAvatar(Long id, MultipartFile file) throws RuntimeException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));
        StoreFile.checkImage(file);

        String fileName = StoreFile.storeFile(file);

        userRepository.updateImageUrl(id, fileName);

        return "Update User Avatar Successfully";
    }

    @Override
    public UserResponse getUserById(long id) {
        return UserResponse.toUser(userRepository.findById(id)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    public Page<UserResponse> getAllUser(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest).map(user -> UserResponse.toUser(user));
    }

    @Override
    public void updateInfor(long id, String username, String fullName, MultipartFile avatar) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));
        user.setUsername(username == null ? user.getUsername() : username);
        user.setFullName(fullName == null ? user.getFullName() : fullName);

        if (avatar != null) {
            StoreFile.checkImage(avatar);

            StoreFile.deleteImage(user.getAvatarUrl());

            String avatarPath = StoreFile.storeFile(avatar);
            user.setAvatarUrl(avatarPath);
        }
        userRepository.save(user);
    }

    @Override
    public void deleteUser(long id) throws UserRequestException{
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
