package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.models.TaskRecurrence;
import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import com.practice.task_scheduler.entities.projections.CalendarTaskProjection;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId ORDER BY t.createdAt DESC")
    Optional<Page<Task>> findByTaskListIdOrderByCreatedAtDesc(@Param("taskListId") Long taskListId, Pageable pageable);

    @Query("SELECT t FROM Task t JOIN UserTaskAssignment uta WHERE uta.userId = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByCreatedByOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.dueDate = :dueDate WHERE t.id = :id")
    void updateNextDueDateById(@Param("id") long id, @Param("dueDate") LocalDateTime dueDate);


    @Query(value = """
        SELECT 
            t.id as id,
            t.title as title,
            t.description as description,
            t.priority as priority,
            t.is_completed as isCompleted,
            t.due_date as dueDate,
            tl.color as color,
            tl.name as listName
        FROM tasks t
        JOIN task_lists tl ON t.task_list_id = tl.id
        JOIN user_task_lists utl ON tl.id = utl.task_list_id
        WHERE utl.user_id = :userId
        AND t.due_date BETWEEN :startDate AND :endDate
        AND NOT EXISTS (
            SELECT 1 FROM task_recurrences tr 
            WHERE tr.task_id = t.id AND tr.is_active = 1
        )
        ORDER BY t.due_date, 
                 FIELD(t.priority, 'URGENT', 'HIGH', 'MEDIUM', 'LOW')
        """, nativeQuery = true)
    List<CalendarTaskProjection> findSingleTasksForCalendar(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.taskRecurrences tr
        LEFT JOIN FETCH t.taskList tl
        LEFT JOIN tl.userTaskLists utl
        WHERE utl.owner.id = :userId
        AND tr.isActive = true
    """)
    List<Task> findRecurringTasksForUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.userTaskAssignments WHERE t.id = :taskId")
    Optional<Task> findByIdWithAssignments(@Param("taskId") Long taskId);

    @Modifying
    @Query("UPDATE Task t SET t.isCompleted = :completed WHERE t.id = :taskId")
    void updateCompletionStatus(@Param("taskId") Long taskId, @Param("completed") boolean completed);

    @Query(value = """
        SELECT DISTINCT
            t.id as id,
            t.title as title,
            t.description as description,
            t.priority as priority,
            t.is_completed as isCompleted,
            t.due_date as dueDate,
            tl.color as color,
            tl.name as listName,
            COALESCE(assignment_counts.total_assignments, 0) as assignedUsersCount,
            COALESCE(assignment_counts.completed_assignments, 0) as completedUsersCount
        FROM tasks t
        JOIN task_lists tl ON t.task_list_id = tl.id
        LEFT JOIN (
            SELECT 
                uta.task_id,
                COUNT(*) as total_assignments,
                SUM(CASE WHEN uta.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_assignments
            FROM user_task_assignments uta
            GROUP BY uta.task_id
        ) assignment_counts ON t.id = assignment_counts.task_id
        WHERE t.due_date BETWEEN :startDate AND :endDate
        AND (
            -- User có access thông qua task list membership
            EXISTS (
                SELECT 1 FROM user_task_lists utl 
                WHERE utl.task_list_id = tl.id AND utl.user_id = :userId
            )
            OR 
            -- User được assign trực tiếp task này
            EXISTS (
                SELECT 1 FROM user_task_assignments uta
                WHERE uta.task_id = t.id AND uta.user_id = :userId
            )
            OR
            -- User là creator của task
            t.created_by = :userId
        )
        AND NOT EXISTS (
            SELECT 1 FROM task_recurrences tr 
            WHERE tr.task_id = t.id AND tr.is_active = 1
        )
        ORDER BY t.due_date, 
                 FIELD(t.priority, 'URGENT', 'HIGH', 'MEDIUM', 'LOW')
        """, nativeQuery = true)
    List<CalendarTaskProjection> findSingleTasksForCalendarWithAssignments(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.taskList tl
        WHERE EXISTS (
            SELECT 1 FROM TaskRecurrence tr
            WHERE tr.taskId = t.id AND tr.isActive = true
        )
        AND (
            EXISTS (
                SELECT 1 FROM UserTaskList utl 
                WHERE utl.taskListId = t.taskListId AND utl.userId = :userId
            )
            OR 
            EXISTS (
                SELECT 1 FROM UserTaskAssignment ua
                WHERE ua.taskId = t.id AND ua.userId = :userId
            )
            OR
            t.createdBy = :userId
        )
        """)
    List<Task> findRecurringTasksForUserBasic(@Param("userId") Long userId);

    @Query("SELECT tr FROM TaskRecurrence tr WHERE tr.taskId IN :taskIds AND tr.isActive = true")
    List<TaskRecurrence> findActiveRecurrencesByTaskIds(@Param("taskIds") List<Long> taskIds);

    @Query("SELECT ua FROM UserTaskAssignment ua WHERE ua.taskId IN :taskIds")
    List<UserTaskAssignment> findAssignmentsByTaskIds(@Param("taskIds") List<Long> taskIds);
}
//Cannot deserialize value of type `java.time.LocalDateTime`