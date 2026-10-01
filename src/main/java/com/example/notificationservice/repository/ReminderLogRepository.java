package com.example.notificationservice.repository;

import com.example.notificationservice.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {

    boolean existsByTodoIdAndDueDate(Long todoId, String dueDate);
}