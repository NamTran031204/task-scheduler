package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.TaskList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    @Query(value = "SELECT t.* FROM task_lists t JOIN users u ON t.owner_id = u.id WHERE t.name = :name AND t.owner_id = :userId ", nativeQuery = true)
    Optional<TaskList> existByNameAndUserId(@Param("name") String name,@Param("userId") long userId);

    @Query("SELECT t FROM TaskList t WHERE t.ownerId = :userId ORDER BY t.createdAt DESC")
    Page<TaskList> findByOwnerIdOrderByCreatedAtDesc(@Param("userId") long userId, Pageable pageable);

    Optional<TaskList> findByShareCode(String shareCode);

    long countByOwnerId(long userId);
}
