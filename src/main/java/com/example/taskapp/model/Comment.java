package com.example.taskapp.model;

public record Comment(int id, int taskId, String authorName, String text, String createdAt) {
    @Override
    public String toString() {
        return String.format("[%d] %s (%s): %s", id, authorName, createdAt, text);
    }
}
