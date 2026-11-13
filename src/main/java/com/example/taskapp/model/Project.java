package com.example.taskapp.model;

public record Project(int id, String name, int ownerId, String ownerName) {
    @Override
    public String toString() {
        return String.format("[%d] %s (owner: %s)", id, name, ownerName);
    }
}
