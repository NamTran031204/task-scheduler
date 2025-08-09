package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.Task;
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

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId ORDER BY t.createdAt DESC")
    Page<Task> findByTaskListIdOrderByCreatedAtDesc(@Param("taskListId") Long taskListId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.createdBy = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByCreatedByOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByAssignedToOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentTime AND t.isCompleted = false")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId AND t.isCompleted = true ORDER BY t.completedAt DESC")
    Page<Task> findCompletedTasksByTaskListId(@Param("taskListId") Long taskListId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId AND t.isCompleted = false ORDER BY t.dueDate ASC")
    Page<Task> findPendingTasksByTaskListId(@Param("taskListId") Long taskListId, Pageable pageable);

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
}
//Cannot deserialize value of type `java.time.LocalDateTime`