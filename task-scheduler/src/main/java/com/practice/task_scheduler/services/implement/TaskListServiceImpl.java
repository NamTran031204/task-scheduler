package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskListDTO;
import com.practice.task_scheduler.entities.models.TaskList;
import com.practice.task_scheduler.entities.models.User;
import com.practice.task_scheduler.entities.models.UserTaskList;
import com.practice.task_scheduler.entities.projections.UserTaskListProjection;
import com.practice.task_scheduler.entities.responses.TaskListResponse;
import com.practice.task_scheduler.entities.responses.UserResponse;
import com.practice.task_scheduler.entities.responses.UserTaskListResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.TaskException;
import com.practice.task_scheduler.exceptions.exception.TaskListException;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.exceptions.exception.UserTaskListException;
import com.practice.task_scheduler.repositories.TaskListRepository;
import com.practice.task_scheduler.repositories.TaskRepository;
import com.practice.task_scheduler.repositories.UserRepository;
import com.practice.task_scheduler.repositories.UserTaskListRepository;
import com.practice.task_scheduler.services.TaskListService;
import com.practice.task_scheduler.services.UserService;
import com.practice.task_scheduler.utils.GenerateShareCode;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TaskListServiceImpl implements TaskListService {

    private final UserRepository userRepository;

    private final TaskListRepository taskListRepository;

    private final UserTaskListRepository userTaskListRepository;

    private final TaskRepository taskRepository;

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
//                .ownerUser(user)
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
        TaskList taskList = taskListRepository.findByIdAndOwnerId(taskListId, userId)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND));

        taskList.setIsShared(!taskList.getIsShared());

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

    @Override
    public List<UserResponse> getAllUserByTaskList(long taskListId) {
        return List.of();
    }

    @Override
    @Transactional
    public String userLeaveTaskList(long userId, long taskListId) {
        UserTaskList userTaskList = userTaskListRepository.findByUserIdAndTaskListId(userId, taskListId)
                .orElseThrow(() -> new UserTaskListException(ErrorCode.USERTASKLIST_NOT_FOUND));
        userTaskListRepository.deleteById(userTaskList.getId());
        if (userTaskList.getRole().equals(UserTaskList.Role.HOST)){
            UserTaskList newHost = userTaskListRepository.findByTaskListIdIdOrderByJoinedAt(taskListId)
                    .orElse(null);
            if(newHost == null){
                taskListRepository.deleteById(taskListId);
                return "Cannot find authority, so delete task list";
            }
            newHost.setRole(UserTaskList.Role.HOST);
            TaskList taskList = taskListRepository.findById(taskListId).orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND));
            taskList.setOwnerId(newHost.getUserId());
            taskListRepository.save(taskList);
            userTaskListRepository.save(newHost);
            return "Authority role HOST to new User";
        }
        return "Delete successfully";
    }

    @Override
    public UserTaskListResponse getAllMemberInTaskList(long taskListId) {
        List<UserTaskListProjection> userTaskListProjections = userTaskListRepository.findMemberByTaskListId(taskListId);

        if (userTaskListProjections.isEmpty()) {
            return UserTaskListResponse.builder()
                    .userByRoleAndJoinedAt(new HashMap<>())
                    .build();
        }

        Map<UserTaskList.Role, List<Pair<UserResponse, LocalDateTime>>> userByRoleAndJoinedAt = new ConcurrentHashMap<>();
        userByRoleAndJoinedAt.put(UserTaskList.Role.HOST, new ArrayList<>());
        userByRoleAndJoinedAt.put(UserTaskList.Role.MEMBER, new ArrayList<>());

        userTaskListProjections.parallelStream()
                .forEach(userTaskList -> {
                    UserResponse userResponse = UserResponse.builder()
                            .id(userTaskList.getId())
                            .email(userTaskList.getEmail())
                            .username(userTaskList.getUsername())
                            .fullName(userTaskList.getFullName())
                            .avatarUrl(userTaskList.getAvatarUrl())
                            .build();
                    List<Pair<UserResponse, LocalDateTime>> users = userByRoleAndJoinedAt.get(userTaskList.getRole());
                    users.add(new Pair<>(userResponse, userTaskList.getJoinedAt()));
                    userByRoleAndJoinedAt.put(userTaskList.getRole(), users);
                });
        return UserTaskListResponse.builder()
                .userByRoleAndJoinedAt(userByRoleAndJoinedAt)
                .build();
    }


    private void saveUserTaskList(long userId, User owner,  long taskListId, TaskList taskList, UserTaskList.Role role){
        UserTaskList userTaskList = UserTaskList.builder()
                .userId(userId)
                .taskListId(taskListId)
//                .taskList(taskList)
//                .owner(owner)
                .role(role)
                .build();
        userTaskListRepository.save(userTaskList);
    }

}
