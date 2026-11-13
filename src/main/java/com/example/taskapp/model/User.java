package com.example.taskapp.model;

public record User(int id, String name, String email) {
    @Override
    public String toString() {
        return String.format("[%d] %s <%s>", id, name, email);
    }
}
