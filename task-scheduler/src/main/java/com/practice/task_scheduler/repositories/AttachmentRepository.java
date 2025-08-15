package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("SELECT a FROM Attachment a WHERE a.taskId = :taskId ORDER BY a.uploadedAt DESC")
    Optional<List<Attachment>> findByTaskIdOrderByUploadedAtDesc(@Param("taskId") Long taskId);

    @Query("SELECT a FROM Attachment a WHERE a.uploadedBy = :userId ORDER BY a.uploadedAt DESC")
    List<Attachment> findByUploadedByOrderByUploadedAtDesc(@Param("userId") Long userId);

    long countByTaskId(Long taskId);
}