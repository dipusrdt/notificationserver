package com.example.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"todo_id", "due_date"}))
public class ReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "todo_id", nullable = false)
    private Long todoId;

    @Column(name = "due_date", nullable = false)
    private String dueDate;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public ReminderLog() {
    }

    public ReminderLog(Long todoId, String dueDate, String ownerEmail, LocalDateTime sentAt) {
        this.todoId = todoId;
        this.dueDate = dueDate;
        this.ownerEmail = ownerEmail;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public Long getTodoId() { return todoId; }
    public String getDueDate() { return dueDate; }
    public String getOwnerEmail() { return ownerEmail; }
    public LocalDateTime getSentAt() { return sentAt; }
}