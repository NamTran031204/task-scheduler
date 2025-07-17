package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.entities.models.TaskList;
import com.practice.task_scheduler.entities.models.User;
import com.practice.task_scheduler.entities.responses.TaskListResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.repositories.TaskListRepository;
import com.practice.task_scheduler.repositories.UserRepository;
import com.practice.task_scheduler.services.TaskListService;
import com.practice.task_scheduler.services.UserService;
import com.practice.task_scheduler.utils.GenerateShareCode;
import jakarta.validation.constraints.Max;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskListServiceImpl implements TaskListService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskListRepository taskListRepository;

    @Override
    public TaskListResponse createTaskList(long userId, TaskListDTO taskListDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        if (taskListRepository.existByNameAndUserId(taskListDTO.getName(), userId).isPresent()){
            throw new RuntimeException("Already exist another list with name: "+ taskListDTO.getName());
        }

        TaskList taskList = TaskList.builder()
                .name(taskListDTO.getName())
                .description(taskListDTO.getDescription())
                .isShared(false)
                .ownerId(userId)
                .ownerUser(user)
                .shareCode(GenerateShareCode.generateShareCode(user.getUsername(), taskListDTO.getName()))
                .color(taskListDTO.getColor() != null ? taskListDTO.getColor() : "#3b82f6")
                .build();

        if (taskListDTO.getColor() != null){
            taskList.setColor(taskList.getColor());
        }
        taskListRepository.save(taskList);

        return TaskListResponse.toTaskList(taskList);
    }



}
