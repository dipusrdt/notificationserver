package com.example.notificationservice.dto;

public record DueTodoResponse(Long todoId, String title, String dueDate,
                              String ownerEmail, String ownerName) {}