package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.entities.models.TaskList;
import com.practice.task_scheduler.entities.models.User;
import com.practice.task_scheduler.entities.models.UserTaskList;
import com.practice.task_scheduler.entities.responses.TaskListResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.TaskException;
import com.practice.task_scheduler.exceptions.exception.TaskListException;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.repositories.TaskListRepository;
import com.practice.task_scheduler.repositories.UserRepository;
import com.practice.task_scheduler.repositories.UserTaskListRepository;
import com.practice.task_scheduler.services.TaskListService;
import com.practice.task_scheduler.services.UserService;
import com.practice.task_scheduler.utils.GenerateShareCode;
import jakarta.validation.constraints.Max;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskListServiceImpl implements TaskListService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskListRepository taskListRepository;

    @Autowired
    UserTaskListRepository userTaskListRepository;

    @Override
    public TaskListResponse createTaskList(long userId, TaskListDTO taskListDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        if (taskListRepository.existByNameAndUserId(taskListDTO.getName(), userId).isPresent()){
            throw new TaskListException(ErrorCode.TASKLIST_ALREADY_EXIST,"Already exist another list with name: "+ taskListDTO.getName());
        }

        TaskList taskList = TaskList.builder()
                .name(taskListDTO.getName())
                .description(taskListDTO.getDescription())
                .isShared(false)
                .ownerId(userId)
                .ownerUser(user)
                .color(taskListDTO.getColor() != null ? taskListDTO.getColor() : "#3b82f6")
                .build();

        if (taskListDTO.getColor() != null){
            taskList.setColor(taskList.getColor());
        }
        taskList = taskListRepository.save(taskList);

        saveUserTaskList(userId, user, taskList.getId(), taskList, UserTaskList.Role.HOST);

        return TaskListResponse.toTaskList(taskList);
    }

    @Override
    public TaskListResponse getTaskListById(long taskListId) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        return TaskListResponse.toTaskList(taskList);
    }

    @Override
    public Page<TaskListResponse> getAllTaskListsByUserId(long userId, PageRequest pageRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        Page<TaskList> taskListPage = taskListRepository.findByOwnerIdOrderByCreatedAtDesc(userId, pageRequest);

        return taskListPage.map(TaskListResponse::toTaskList);
    }

    @Override
    public TaskListResponse updateTaskList(long taskListId, long userId, TaskListDTO taskListDTO) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!taskList.getOwnerId().equals(userId)) {
            throw new TaskListException(ErrorCode.TASKLIST_ACCESS_DENIED, "Only owner can update this task list");
        }

        if (taskListDTO.getName() != null) {
            if (!taskList.getName().equals(taskListDTO.getName()) &&
                    taskListRepository.existByNameAndUserId(taskListDTO.getName(), userId).isPresent()) {
                throw new TaskListException(ErrorCode.TASKLIST_ALREADY_EXIST, "Already exist another list with name: " + taskListDTO.getName());
            }
        }
        taskList.setName(taskListDTO.getName() == null ? taskList.getName() : taskListDTO.getName());
        taskList.setDescription(taskListDTO.getDescription() == null ? taskList.getDescription() : taskListDTO.getDescription());

        if (taskListDTO.getColor() != null) {
            taskList.setColor(taskListDTO.getColor());
        }

        TaskList updatedTaskList = taskListRepository.save(taskList);
        return TaskListResponse.toTaskList(updatedTaskList);
    }

    @Override
    public void deleteTaskList(long taskListId, long userId) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!taskList.getOwnerId().equals(userId)) {
            throw new TaskListException(ErrorCode.TASKLIST_ACCESS_DENIED, "Only owner can delete this task list");
        }

        if (taskList.getIsShared() && taskList.getUserTaskLists() != null && !taskList.getUserTaskLists().isEmpty()) {
            throw new TaskListException(ErrorCode.TASKLIST_EXCEPTION, "Cannot delete shared task list with members. Remove all members first.");
        }

        taskListRepository.delete(taskList);
    }

    @Override
    public TaskListResponse shareTaskList(long taskListId, long userId) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!taskList.getOwnerId().equals(userId)) {
            throw new TaskListException(ErrorCode.TASKLIST_ACCESS_DENIED, "Only owner can share this task list");
        }

        if (taskList.getIsShared()) {
            throw new TaskListException(ErrorCode.TASKLIST_ALREADY_EXIST, "TaskList is already shared");
        }

        taskList.setIsShared(true);

        if (taskList.getShareCode() == null) {
            User owner = userRepository.findById(userId)
                    .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));
            taskList.setShareCode(GenerateShareCode.generateShareCode(owner.getUsername(), taskList.getName()));
        }

        TaskList updatedTaskList = taskListRepository.save(taskList);
        return TaskListResponse.toTaskList(updatedTaskList);
    }

    @Override
    public TaskListResponse joinTaskListByShareCode(String shareCode, long userId) {
        TaskList taskList = taskListRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_INVALID_SHARECODE, "Invalid share code"));

        if (!taskList.getIsShared()) {
            throw new TaskListException(ErrorCode.TASKLIST_NOT_SHARED, "TaskList is not shared");
        }

        if (taskList.getOwnerId().equals(userId)) {
            throw new TaskListException(ErrorCode.TASKLIST_CANNOT_JOIN, "Owner cannot join their own task list");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        if (userTaskListRepository.existsByUserIdAndTaskListId(userId, taskList.getId())) {
            throw new TaskListException(ErrorCode.TASKLIST_CANNOT_JOIN, "User is already a member of this task list");
        }

        saveUserTaskList(userId, user, taskList.getId(), taskList, UserTaskList.Role.MEMBER);

        return TaskListResponse.toTaskList(taskList);
    }


    private void saveUserTaskList(long userId, User owner,  long taskListId, TaskList taskList, UserTaskList.Role role){
        UserTaskList userTaskList = UserTaskList.builder()
                .userId(userId)
                .taskListId(taskListId)
                .taskList(taskList)
                .owner(owner)
                .role(role)
                .build();
        userTaskListRepository.save(userTaskList);
    }

}
