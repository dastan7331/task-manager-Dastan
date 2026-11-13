package com.example.taskapp.model;

import java.time.LocalDate;

public record Task(int id, String title, String description, String status, int priority,
                   LocalDate dueDate, int projectId, Integer assigneeId, Integer categoryId,
                   String projectName, String assigneeName, String categoryName) {
    @Override
    public String toString() {
        return String.format("[%d] %s | %s | priority %d | due %s | project: %s | assignee: %s | category: %s",
                id, title, status, priority, dueDate == null ? "-" : dueDate, projectName,
                assigneeName == null ? "-" : assigneeName, categoryName == null ? "-" : categoryName);
    }
}
