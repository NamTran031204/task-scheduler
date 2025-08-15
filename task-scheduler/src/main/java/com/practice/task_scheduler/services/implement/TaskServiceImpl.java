package com.practice.task_scheduler.services.implement;

import com.practice.task_scheduler.entities.dtos.TaskDTO;
import com.practice.task_scheduler.entities.models.*;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import com.practice.task_scheduler.exceptions.ErrorCode;
import com.practice.task_scheduler.exceptions.exception.*;
import com.practice.task_scheduler.repositories.*;
import com.practice.task_scheduler.services.*;
import com.practice.task_scheduler.utils.StoreFile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final TaskListRepository taskListRepository;

    private final UserTaskListRepository userTaskListRepository;

    private final TaskHistoryRepository taskHistoryRepository;

    private final AttachmentRepository attachmentRepository;

    private final TaskReminderService taskReminderService;

    private final TaskRecurrenceService taskRecurrenceService;

    private final HistoryService historyService;

    private final UserTaskAssignmentService userTaskAssignmentService;

    private final UserTaskAssignmentRepository userTaskAssignmentRepository;

    @Override
    @Transactional
    public String createTask(long userId, TaskDTO taskDTO) {
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


        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : Task.Priority.MEDIUM)
                .dueDate(taskDTO.getDueDate())
                .taskListId(taskDTO.getTaskListId())
                .isCompleted(false)
                .createdBy(userId)
                .build();

        Task savedTask = taskRepository.save(task);
        userTaskAssignmentService.createAssignment(taskDTO.getAssignedTo(), savedTask);
        // notification

        if (savedTask.getDueDate() != null) {
            List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskId(savedTask.getId());
            assignments.forEach(assignment ->
                    taskReminderService.autoCreateOrUpdateReminder(savedTask.getId(), assignment.getUserId())
            );
        }

        historyService.insertHistory(savedTask.getId(), userId, TaskHistory.HistoryAction.CREATED, savedTask.getId().toString(), "Task Created");
        return "Create task successfull";
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

        boolean canUpload = taskList.getOwnerId().equals(userId) ||
                userTaskListRepository.existsByUserIdAndTaskListId(userId, taskList.getId()) ||
                userTaskAssignmentRepository.findByTaskIdAndUserId(taskId, userId).isPresent();

        if (!canUpload) {
            throw new TaskListException(ErrorCode.TASKLIST_ACCESS_DENIED,
                    "User must be task list member, task assignee, or task list owner to upload files");
        }

        String newValue = "upload: {";
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.getSize() > 0) {
                    StoreFile.checkFile(file);

                    String fileName = null;
                    try {
                        fileName = StoreFile.storeFile(file).get();
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
                    } catch (InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        throw new FileProcessException(ErrorCode.FILE_UPLOAD_FAILED, file.getName(), e.getMessage());
                    }
                }
            }
        }
        newValue += "}";

        // notification
        historyService.insertHistory(taskId, userId, TaskHistory.HistoryAction.UPDATED, newValue, "upload files");
        return "upload files completed";
    }

    @Override
    public TaskResponse getTaskById(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        boolean hasTaskListAccess = userTaskListRepository.existsByUserIdAndTaskListId(userId, task.getTaskListId());
        boolean hasDirectTaskAssignment = userTaskAssignmentRepository.findByTaskIdAndUserId(taskId, userId).isPresent();
        boolean isTaskCreator = task.getCreatedBy().equals(userId);

        if (!hasTaskListAccess && !hasDirectTaskAssignment && !isTaskCreator) {
            throw new UserTaskListException(ErrorCode.TASK_ACCESS_DENIED, "ACCESS DENIED");
        }

        List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskId(taskId);

        return TaskResponse.toTask(task, assignments);
    }

    @Override
        public Page<TaskResponse> getTasksByTaskListId(long taskListId, long userId, PageRequest pageRequest){
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (!userTaskListRepository.existsByUserIdAndTaskListId(userId, taskListId)) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED);
        }

        Page<Task> taskPage = taskRepository.findByTaskListIdOrderByCreatedAtDesc(taskListId, pageRequest)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND));
        return taskPage.map(task -> {
            List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskId(task.getId());
            return TaskResponse.toTask(task, assignments);
        });
    }

    @Override
    public Page<TaskResponse> getTasksByUserId(long userId, PageRequest pageRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));


        Page<UserTaskAssignment> assignmentPage = userTaskAssignmentRepository
                .findActiveAssignmentsByUserId(userId, pageRequest);

        return assignmentPage.map(assignment -> {
            Task task = assignment.getTask();
            List<UserTaskAssignment> allAssignments = userTaskAssignmentRepository.findByTaskId(task.getId());
            return TaskResponse.toTask(task, allAssignments);
        });
    }

    @Override
    @Transactional
    public TaskResponse updateTask(long taskId, long userId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));

        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "TaskList not found"));

        if (userTaskAssignmentRepository.findByTaskIdAndAssignedBy(task.getId(), userId).isEmpty()) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only task creator or assigner can update this task");
        }

        if (taskDTO.getDueDate() != null && taskDTO.getDueDate().isBefore(LocalDateTime.now())) {
            throw new TaskException(ErrorCode.TASK_INVALID_DUE_DATE, "Due date cannot be in the past");
        }

        task.setTitle(taskDTO.getTitle() == null ? task.getTitle() : taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription() == null ? task.getDescription() : taskDTO.getDescription());
        task.setPriority(taskDTO.getPriority() == null ? task.getPriority() : taskDTO.getPriority());
        task.setDueDate(taskDTO.getDueDate() == null ? task.getDueDate() : taskDTO.getDueDate());

        Task updatedTask = taskRepository.save(task);
        if (taskDTO.getAssignedTo() != null && !taskDTO.getAssignedTo().isEmpty()) {
            userTaskAssignmentService.createAssignment(taskDTO.getAssignedTo(), updatedTask);
        }

        if (taskDTO.getRemovedUser() != null && !taskDTO.getRemovedUser().isEmpty()) {
            taskDTO.getRemovedUser().forEach(removedUserId ->
                    userTaskAssignmentService.deleteAssignedUserTask(removedUserId, updatedTask));
        }
        // notification

        if (updatedTask.getDueDate() != null) {
            List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskId(updatedTask.getId());
            assignments.forEach(assignment ->
                    taskReminderService.autoCreateOrUpdateReminder(updatedTask.getId(), assignment.getUserId())
            );
        }

        historyService.insertHistory(taskId, userId, TaskHistory.HistoryAction.UPDATED, "update infor", "update");

        if (task.getDueDate() != null) {
            taskReminderService.autoCreateOrUpdateReminder(task.getId(), userId);
        }
        return TaskResponse.toTask(updatedTask, updatedTask.getUserTaskAssignments());
    }

    @Override
    @Transactional
    public void deleteTask(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND, "Task not found"));
        TaskList taskList = taskListRepository.findById(task.getTaskListId())
                .orElseThrow(() -> new TaskListException(ErrorCode.TASKLIST_NOT_FOUND, "Cannot find task list"));

        UserTaskList userTaskList = userTaskListRepository.findByUserIdAndTaskListId(userId, taskList.getId())
                .orElseThrow(() -> new UserTaskListException(ErrorCode.USERTASKLIST_NOT_FOUND));

        if (!task.getCreatedBy().equals(userId) && !userTaskList.getRole().equals(UserTaskList.Role.HOST)) {
            throw new TaskException(ErrorCode.TASK_ACCESS_DENIED, "Only user with permission can delete this task");
        }

        // notification
        historyService.insertHistory(taskId, userId, TaskHistory.HistoryAction.DELETED, "delete task with id: %d" + taskId, "delete");
        taskRepository.delete(task);
    }


    @Override
    public List<Attachment> getAllFilesInTask(long taskId, long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(ErrorCode.TASK_NOT_FOUND));

        UserTaskList userTaskList = userTaskListRepository.findByUserIdAndTaskListId(userId, task.getTaskListId())
                .orElseThrow(() -> new UserTaskListException(ErrorCode.USERTASKLIST_NOT_FOUND));

        boolean isTaskListHost = userTaskList.getRole().equals(UserTaskList.Role.HOST);
        boolean hasDirectTaskAssignment = userTaskAssignmentRepository.findByTaskIdAndUserId(taskId, userId).isPresent();
        boolean isTaskCreator = task.getCreatedBy().equals(userId);

        if (!isTaskListHost && !hasDirectTaskAssignment && !isTaskCreator) {
            throw new UserTaskListException(ErrorCode.TASK_ACCESS_DENIED, "No access to this task");
        }

        return attachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId)
                .orElse(Collections.emptyList());
    }

    public Page<TaskResponse> getCreatedTasksByUserId(long userId, PageRequest pageRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserRequestException(ErrorCode.USER_NOT_FOUND));

        Page<Task> taskPage = taskRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageRequest);
        return taskPage.map(task -> {
            List<UserTaskAssignment> assignments = userTaskAssignmentRepository.findByTaskId(task.getId());
            return TaskResponse.toTask(task, assignments);
        });
    }
}