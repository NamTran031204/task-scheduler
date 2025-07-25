package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.TaskRecurrence;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRecurrenceRepository extends JpaRepository<TaskRecurrence, Long> {

    @Query(value = "SELECT * FROM task_recurrences WHERE is_active = true", nativeQuery = true)
    List<TaskRecurrence> findByIsActiveEqualTrue();

    @Query(value = "SELECT * FROM task_recurrences WHERE task_id = :taskId AND is_active = true", nativeQuery = true)
    TaskRecurrence findByTaskId(@Param("taskId") long taskId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE TaskRecurrence tr SET
            tr.recurrenceType = :#{#newTaskRecurrence.recurrenceType},
            tr.recurrenceInterval = :#{#newTaskRecurrence.recurrenceInterval},
            tr.recurrenceEndDate = :#{#newTaskRecurrence.recurrenceEndDate},
            tr.nextDueDate = :#{#newTaskRecurrence.nextDueDate},
            tr.isActive = :#{#newTaskRecurrence.isActive},
            tr.updatedAt = CURRENT_TIMESTAMP
        WHERE tr.id = :id
    """)
    void updateTaskRecurrence(@Param("id") long id, @Param("newTaskRecurrence") TaskRecurrence newTaskRecurrence);


}
