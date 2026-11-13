package com.example.taskapp.model;

public record Category(int id, String name) {
    @Override
    public String toString() {
        return String.format("[%d] %s", id, name);
    }
}
