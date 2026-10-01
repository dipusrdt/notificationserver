package com.example.notificationservice.service;

import com.example.notificationservice.dto.DueTodoResponse;
import com.example.notificationservice.entity.ReminderLog;
import com.example.notificationservice.repository.ReminderLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final TodoApiClient todoApiClient;
    private final ReminderLogRepository reminderLogRepository;
    private final JavaMailSender mailSender;
    private final int daysAhead;

    public ReminderService(TodoApiClient todoApiClient,
                           ReminderLogRepository reminderLogRepository,
                           JavaMailSender mailSender,
                           @Value("${notification.reminder.days-ahead}") int daysAhead) {
        this.todoApiClient = todoApiClient;
        this.reminderLogRepository = reminderLogRepository;
        this.mailSender = mailSender;
        this.daysAhead = daysAhead;
    }

    @Scheduled(cron = "${notification.reminder.cron}")
    public void runReminders() {
        try {
            LocalDate targetDate = LocalDate.now().plusDays(daysAhead);
            log.info("Reminder run started for due date {}", targetDate);

            List<DueTodoResponse> todos = todoApiClient.getTodosDueOn(targetDate);

            List<DueTodoResponse> notSentYet = todos.stream()
                    .filter(t -> !reminderLogRepository
                            .existsByTodoIdAndDueDate(t.todoId(), t.dueDate()))
                    .toList();

            Map<String, List<DueTodoResponse>> byUser = notSentYet.stream()
                    .collect(Collectors.groupingBy(DueTodoResponse::ownerEmail));

            log.info("Found {} todo(s), {} not yet reminded, for {} user(s)",
                    todos.size(), notSentYet.size(), byUser.size());

            for (Map.Entry<String, List<DueTodoResponse>> entry : byUser.entrySet()) {
                String email = entry.getKey();
                List<DueTodoResponse> userTodos = entry.getValue();
                try {
                    sendEmail(email, targetDate, userTodos);

                    LocalDateTime now = LocalDateTime.now();
                    List<ReminderLog> rows = userTodos.stream()
                            .map(t -> new ReminderLog(t.todoId(), t.dueDate(), email, now))
                            .toList();
                    reminderLogRepository.saveAll(rows);

                    log.info("Reminder sent to {} for {} todo(s)", email, userTodos.size());
                } catch (Exception e) {
                    log.error("Could not remind {}: {}", email, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Reminder run failed: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, LocalDate dueDate, List<DueTodoResponse> todos) {
        String name = todos.get(0).ownerName();
        String greeting = (name == null || name.isBlank()) ? "Hello," : "Hello " + name + ",";

        StringBuilder text = new StringBuilder();
        text.append(greeting).append("\n\n");
        text.append("These todos are due on ").append(dueDate).append(":\n\n");
        for (DueTodoResponse t : todos) {
            text.append(" - ").append(t.title()).append("\n");
        }
        text.append("\nGood luck!");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("reminder@todoapp.local");
        message.setTo(to);
        message.setSubject("Reminder: " + todos.size() + " todo(s) due on " + dueDate);
        message.setText(text.toString());
        mailSender.send(message);
    }
}