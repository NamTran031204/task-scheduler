package com.practice.task_scheduler.entities.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"task", "user"})
@EqualsAndHashCode(exclude = {"task", "user", "uploadedAt"})
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "note_content")
    @Lob
    private String noteContent;

    @Column(name = "attachment_type", nullable = false)
    private AttachmentType attachmentType;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }


    @JoinColumn(name = "task_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @JoinColumn(name = "uploaded_by", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;


    public enum AttachmentType{
        FILE, NOTE, BOTH
    }
}
