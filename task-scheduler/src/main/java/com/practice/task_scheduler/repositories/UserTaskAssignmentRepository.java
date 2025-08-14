package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.UserTaskAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserTaskAssignmentRepository extends JpaRepository<UserTaskAssignment, Long> {

    List<UserTaskAssignment> findByTaskId(Long taskId);

    List<UserTaskAssignment> findByUserId(Long userId);

    Optional<UserTaskAssignment> findByTaskIdAndUserId(Long taskId, Long userId);

    Optional<UserTaskAssignment> findByTaskIdAndAssignedBy(Long taskId, Long assignedBy);

    @Query("SELECT uta FROM UserTaskAssignment uta WHERE uta.userId = :userId AND uta.status = :status")
    List<UserTaskAssignment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") UserTaskAssignment.Status status);

    @Query("SELECT COUNT(uta) FROM UserTaskAssignment uta WHERE uta.taskId = :taskId AND uta.status = 'COMPLETED'")
    long countCompletedAssignmentsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(uta) FROM UserTaskAssignment uta WHERE uta.taskId = :taskId")
    long countAssignmentsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT ua.userId FROM UserTaskAssignment ua WHERE ua.taskId = :taskId")
    Set<Long> findUserIdsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT CASE WHEN COUNT(uta) > 0 THEN true ELSE false END FROM UserTaskAssignment uta WHERE uta.taskId = :taskId AND uta.userId = :userId")
    boolean existsByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(uta) > 0 THEN true ELSE false END FROM UserTaskAssignment uta WHERE uta.taskId = :taskId AND uta.assignedBy = :assignedBy")
    boolean existsByTaskIdAndAssignedBy(@Param("taskId") Long taskId, @Param("assignedBy") Long assignedBy);

    @Query("SELECT uta FROM UserTaskAssignment uta LEFT JOIN FETCH uta.task WHERE uta.userId = :userId ORDER BY uta.assignedAt DESC")
    Page<UserTaskAssignment> findActiveAssignmentsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ua FROM UserTaskAssignment ua WHERE ua.taskId IN :taskIds")
    List<UserTaskAssignment> findByTaskIdIn(@Param("taskIds") List<Long> taskIds);
}