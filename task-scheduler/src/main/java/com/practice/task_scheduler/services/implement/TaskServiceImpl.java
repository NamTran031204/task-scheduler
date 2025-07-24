package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskDTO;
import com.practice.task_scheduler.entities.models.*;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.TaskException;
import com.practice.task_scheduler.exceptions.exception.TaskListException;
import com.practice.task_scheduler.exceptions.exception.UserRequestException;
import com.practice.task_scheduler.repositories.*;
import com.practice.task_scheduler.services.TaskReminderService;
import com.practice.task_scheduler.services.TaskService;
import com.practice.task_scheduler.utils.StoreFile;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskListRepository taskListRepository;

    @Autowired
    UserTaskListRepository userTaskListRepository;

    @Autowired
    TaskHistoryRepository taskHistoryRepository;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    TaskReminderService taskReminderService;

    @Override
    @Transactional
    public TaskResponse createTask(long userId, TaskDTO taskDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        TaskList taskList = taskListRepository.findById(taskDTO.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!taskList.getOwnerId().equals(userId) &&
                !userTaskListRepository.existsByUserIdAndTaskListId(userId, taskDTO.getTaskListId())) {
            throw new TaskListException(ErrorCode.TASKLIST_ACCESS_DENIED, "User is not owner or member of this task list");
        }

        if (taskDTO.getDueDate() != null && taskDTO.getDueDate().isBefore(LocalDateTime.now())) {
            throw new TaskException(ErrorCode.TASK_INVALID_DUE_DATE, "Due date cannot be in the past");
        }

        if (taskDTO.getAssignedTo() != null) {
            if (!userRepository.existsById(taskDTO.getAssignedTo())) {
                throw new UserRequestException(ErrorCode.USER_NOT_FOUND);
            }

            if (taskList.getIsShared() && !taskList.getOwnerId().equals(taskDTO.getAssignedTo()) &&
                    !userTaskListRepository.existsByUserIdAndTaskListId(taskDTO.getAssignedTo(), taskDTO.getTaskListId())) {
                throw new TaskException(ErrorCode.TASK_ASSIGNMENT_FAILED, "Cannot assign task to non-member user");
            }
        }

        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : Task.Priority.MEDIUM)
                .dueDate(taskDTO.getDueDate())
                .taskListId(taskDTO.getTaskListId())
                .createdBy(userId)
                .assignedTo(taskDTO.getAssignedTo())
                .isCompleted(false)
                .build();

        Task savedTask = taskRepository.save(task);
        updateTaskHistory(savedTask.getId(), userId, TaskHistory.HistoryAction.CREATED, null, savedTask.getId().toString(), "Task Created");

        if (savedTask.getDueDate() != null) {
            taskReminderService.autoCreateOrUpdateReminder(savedTask.getId(), userId);
        }
        return TaskResponse.toTask(savedTask);
    }

    @Override
    @Transactional
    public String uploadTaskFiles(long taskId, long userId, List<MultipartFile> files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Cannot find task"));
        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!taskList.getOwnerId().equals(userId) &&
                !userTaskListRepository.existsByUserIdAndTaskListId(userId, taskList.getId())) {
            throw new TaskListException(ErrorCode.TASKLIST_ACCESS_DENIED, "User is not owner or member of this task list");
        }

        String newValue = "upload: {";
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.getSize() > 0) {
                    StoreFile.checkFile(file);

                    String fileName = StoreFile.storeFile(file);
                    newValue += fileName + ",";

                    Attachment fileAttachment = Attachment.builder()
                            .taskId(task.getId())
                            .fileName(file.getOriginalFilename())
                            .filePath(StoreFile.storePath + fileName)
                            .fileSize(file.getSize())
                            .fileType(file.getContentType())
                            .attachmentType(Attachment.AttachmentType.FILE)
                            .uploadedBy(userId)
                            .build();
                    attachmentRepository.save(fileAttachment);
                }
            }
        }
        newValue += "}";
        String oldValue = taskHistoryRepository.findByTaskIdAndUserId(task.getId(), user.getId());

        updateTaskHistory(taskId, userId, TaskHistory.HistoryAction.UPDATED, oldValue, newValue, "upload files");
        return "upload files completed";
    }

    @Override
    public TaskResponse getTaskById(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!taskList.getOwnerId().equals(userId) &&
                !userTaskListRepository.existsByUserIdAndTaskListId(userId, task.getTaskListId())) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "User cannot view this task");
        }

        return TaskResponse.toTask(task);
    }

    @Override
    public Page<TaskResponse> getTasksByTaskListId(long taskListId, long userId, PageRequest pageRequest) {
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));


        if (!taskList.getOwnerId().equals(userId) &&
                !userTaskListRepository.existsByUserIdAndTaskListId(userId, taskListId)) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "User cannot view tasks in this task list");
        }

        Page<Task> taskPage = taskRepository.findByTaskListIdOrderByCreatedAtDesc(taskListId, pageRequest);
        return taskPage.map(TaskResponse::toTask);
    }

    @Override
    public Page<TaskResponse> getTasksByUserId(long userId, PageRequest pageRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        Page<Task> taskPage = taskRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageRequest);
        return taskPage.map(TaskResponse::toTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(long taskId, long userId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (task.getCreatedBy() != userId && userId != task.getAssignedTo()) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only task creator or assigned user can update this task");
        }

        if (taskDTO.getDueDate() != null && taskDTO.getDueDate().isBefore(LocalDateTime.now())) {
            throw new TaskException(ErrorCode.TASK_INVALID_DUE_DATE, "Due date cannot be in the past");
        }

        if (taskDTO.getAssignedTo() != null) {
            if (!userRepository.existsById(taskDTO.getAssignedTo())) {
                throw new UserRequestException(ErrorCode.USER_NOT_FOUND);
            }

            if (taskList.getIsShared() && !taskList.getOwnerId().equals(taskDTO.getAssignedTo()) &&
                    !userTaskListRepository.existsByUserIdAndTaskListId(taskDTO.getAssignedTo(), task.getTaskListId())) {
                throw new TaskException(ErrorCode.TASK_ASSIGNMENT_FAILED, "Cannot assign task to non-member user");
            }
        }

        task.setTitle(taskDTO.getTitle() == null ? task.getTitle(): taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription() == null? task.getDescription(): taskDTO.getDescription());
        task.setPriority(taskDTO.getPriority() == null ?  task.getPriority(): taskDTO.getPriority());
        task.setDueDate(taskDTO.getDueDate() == null ? task.getDueDate() : taskDTO.getDueDate());
        task.setAssignedTo(taskDTO.getAssignedTo() == null ? task.getAssignedTo(): taskDTO.getAssignedTo());

        Task updatedTask = taskRepository.save(task);

        String oldValue = taskHistoryRepository.findByTaskIdAndUserId(taskId, userId);

        updateTaskHistory(taskId, userId, TaskHistory.HistoryAction.UPDATED, oldValue, "update infor", "update");

        if (task.getDueDate() != null) {
            taskReminderService.autoCreateOrUpdateReminder(task.getId(), userId);
        }
        return TaskResponse.toTask(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));
        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "Cannot find task list"));

        if (!task.getCreatedBy().equals(userId) && !taskList.getOwnerId().equals(userId)) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only user with permission can delete this task");
        }

        String oldValue = taskHistoryRepository.findByTaskIdAndUserId(taskId, task.getCreatedBy());

        updateTaskHistory(taskId, userId, TaskHistory.HistoryAction.DELETED, oldValue, "delete task with id: %d" + taskId, "delete");
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse completeTask(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        if (task.getCreatedBy() != userId && userId != task.getAssignedTo()) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only task creator or assigned user can complete this task");
        }

        if (task.getIsCompleted()) {
            throw new TaskException(ErrorCode.TASK_ALREADY_COMPLETED, "Task is already completed");
        }

        task.setIsCompleted(true);
        task.setCompletedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        String oldValue = taskHistoryRepository.findByTaskIdAndUserId(taskId, task.getCreatedBy());

        updateTaskHistory(taskId, task.getCreatedBy(), TaskHistory.HistoryAction.COMPLETED, oldValue, "Complete", "Task Complete");

        return TaskResponse.toTask(updatedTask);
    }

    @Override
    public TaskResponse undoComplete(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        if (task.getCreatedBy() != userId && userId != task.getAssignedTo()) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only task creator or assigned user can complete this task");
        }

        if (task.getIsCompleted()) {
            throw new TaskException(ErrorCode.TASK_IS_NOT_COMPLETED, "Task is NOT completed");
        }

        task.setIsCompleted(false);
        task.setCompletedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        String oldValue = taskHistoryRepository.findByTaskIdAndUserId(taskId, task.getCreatedBy());

        updateTaskHistory(taskId, task.getCreatedBy(), TaskHistory.HistoryAction.COMPLETED, oldValue, "Undo Complete", "Undo Task Complete");

        return TaskResponse.toTask(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse assignTask(long taskId, long userId, long assignedToUserId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!task.getCreatedBy().equals(userId) && !taskList.getOwnerId().equals(userId)) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only task creator or task list owner can assign this task");
        }

        if (!userRepository.existsById(assignedToUserId)) {
            throw new UserRequestException(ErrorCode.USER_NOT_FOUND);
        }

        if (taskList.getIsShared() && !taskList.getOwnerId().equals(assignedToUserId) &&
                !userTaskListRepository.existsByUserIdAndTaskListId(assignedToUserId, task.getTaskListId())) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Cannot assign task to non-member user");
        }

        task.setAssignedTo(assignedToUserId);
        Task updatedTask = taskRepository.save(task);

        String oldValue = taskHistoryRepository.findByTaskIdAndUserId(taskId, task.getCreatedBy());

        updateTaskHistory(taskId, task.getCreatedBy(), TaskHistory.HistoryAction.ASSIGNED, oldValue, "Assign", "Task assigned");

        return TaskResponse.toTask(updatedTask);
    }

    private void updateTaskHistory(long taskId, long userId, TaskHistory.HistoryAction action, String oldValue, String newValue, String description){

        TaskHistory taskHistory = TaskHistory.builder()
                .taskId(taskId)
                .userId(userId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .build();
        taskHistoryRepository.save(taskHistory);
    }
}