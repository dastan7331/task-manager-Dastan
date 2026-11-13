package com.example.taskapp;

import com.example.taskapp.dao.*;
import com.example.taskapp.model.*;
import com.example.taskapp.util.Input;

import java.sql.SQLException;
import java.util.List;

public class Main {
    private static final UserDao users = new UserDao();
    private static final ProjectDao projects = new ProjectDao();
    private static final CategoryDao categories = new CategoryDao();
    private static final TaskDao tasks = new TaskDao();
    private static final CommentDao comments = new CommentDao();
    private static final ReportDao reports = new ReportDao();

    @FunctionalInterface
    private interface Action { void run() throws SQLException; }

    private static void safe(Action a) {
        try {
            a.run();
        } catch (SQLException e) {
            String state = e.getSQLState();
            if ("23505".equals(state)) System.out.println("Error: this value already exists (must be unique).");
            else if ("23503".equals(state) || "23506".equals(state) || "23504".equals(state)) System.out.println("Error: related record with this id does not exist.");
            else System.out.println("Database error: " + e.getMessage());
        }
    }

    private static <T> void printAll(List<T> list) {
        if (list.isEmpty()) System.out.println("(nothing found)");
        list.forEach(System.out::println);
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Task Manager ===");
            System.out.println("1. Users\n2. Projects\n3. Tasks\n4. Comments\n5. Search tasks\n6. Reports\n0. Exit");
            switch (Input.integer("Choose: ", 0, 6)) {
                case 1 -> usersMenu();
                case 2 -> projectsMenu();
                case 3 -> tasksMenu();
                case 4 -> commentsMenu();
                case 5 -> safe(Main::searchTasks);
                case 6 -> reportsMenu();
                default -> { System.out.println("Bye!"); return; }
            }
        }
    }

    private static void usersMenu() {
        System.out.println("\n-- Users --\n1. List\n2. Add\n3. Edit\n4. Delete\n0. Back");
        switch (Input.integer("Choose: ", 0, 4)) {
            case 1 -> safe(() -> printAll(users.findAll()));
            case 2 -> safe(() -> {
                users.insert(Input.text("Name: ", 100, true), Input.email("Email: "));
                System.out.println("User added.");
            });
            case 3 -> safe(() -> {
                printAll(users.findAll());
                int id = Input.integer("User id: ", 1, Integer.MAX_VALUE);
                boolean ok = users.update(id, Input.text("New name: ", 100, true), Input.email("New email: "));
                System.out.println(ok ? "User updated." : "User not found.");
            });
            case 4 -> safe(() -> {
                printAll(users.findAll());
                int id = Input.integer("User id: ", 1, Integer.MAX_VALUE);
                if (Input.confirm("Delete user? Their projects and comments will be deleted too")) {
                    System.out.println(users.delete(id) ? "User deleted." : "User not found.");
                }
            });
            default -> { }
        }
    }

    private static void projectsMenu() {
        System.out.println("\n-- Projects --\n1. List\n2. Add\n3. Rename\n4. Delete\n0. Back");
        switch (Input.integer("Choose: ", 0, 4)) {
            case 1 -> safe(() -> printAll(projects.findAll()));
            case 2 -> safe(() -> {
                printAll(users.findAll());
                String name = Input.text("Project name: ", 100, true);
                projects.insert(name, Input.integer("Owner user id: ", 1, Integer.MAX_VALUE));
                System.out.println("Project added.");
            });
            case 3 -> safe(() -> {
                printAll(projects.findAll());
                int id = Input.integer("Project id: ", 1, Integer.MAX_VALUE);
                System.out.println(projects.update(id, Input.text("New name: ", 100, true)) ? "Renamed." : "Project not found.");
            });
            case 4 -> safe(() -> {
                printAll(projects.findAll());
                int id = Input.integer("Project id: ", 1, Integer.MAX_VALUE);
                if (Input.confirm("Delete project and all its tasks?")) {
                    System.out.println(projects.delete(id) ? "Project deleted." : "Project not found.");
                }
            });
            default -> { }
        }
    }

    private static void tasksMenu() {
        System.out.println("\n-- Tasks --\n1. List\n2. Add\n3. Edit\n4. Delete\n5. Add category\n0. Back");
        switch (Input.integer("Choose: ", 0, 5)) {
            case 1 -> safe(() -> printAll(tasks.findAll()));
            case 2 -> safe(Main::addTask);
            case 3 -> safe(() -> {
                printAll(tasks.findAll());
                int id = Input.integer("Task id: ", 1, Integer.MAX_VALUE);
                String title = Input.text("New title: ", 150, true);
                String status = Input.status("New status");
                int priority = Input.integer("New priority (1-5): ", 1, 5);
                var due = Input.optionalDate("New due date");
                System.out.println(tasks.update(id, title, status, priority, due) ? "Task updated." : "Task not found.");
            });
            case 4 -> safe(() -> {
                printAll(tasks.findAll());
                int id = Input.integer("Task id: ", 1, Integer.MAX_VALUE);
                if (Input.confirm("Delete task?")) {
                    System.out.println(tasks.delete(id) ? "Task deleted." : "Task not found.");
                }
            });
            case 5 -> safe(() -> {
                categories.insert(Input.text("Category name: ", 50, true));
                System.out.println("Category added.");
            });
            default -> { }
        }
    }

    private static void addTask() throws SQLException {
        String title = Input.text("Title: ", 150, true);
        String description = Input.text("Description (optional): ", 1000, false);
        String status = Input.status("Status");
        int priority = Input.integer("Priority (1-5): ", 1, 5);
        var due = Input.optionalDate("Due date");
        printAll(projects.findAll());
        int projectId = Input.integer("Project id: ", 1, Integer.MAX_VALUE);
        printAll(users.findAll());
        Integer assigneeId = Input.optionalInteger("Assignee user id");
        printAll(categories.findAll());
        Integer categoryId = Input.optionalInteger("Category id");
        tasks.insert(title, description.isEmpty() ? null : description, status, priority, due,
                projectId, assigneeId, categoryId);
        System.out.println("Task added.");
    }

    private static void commentsMenu() {
        System.out.println("\n-- Comments --\n1. Show comments of a task\n2. Add comment\n0. Back");
        switch (Input.integer("Choose: ", 0, 2)) {
            case 1 -> safe(() -> printAll(comments.findByTask(Input.integer("Task id: ", 1, Integer.MAX_VALUE))));
            case 2 -> safe(() -> {
                printAll(tasks.findAll());
                int taskId = Input.integer("Task id: ", 1, Integer.MAX_VALUE);
                printAll(users.findAll());
                int authorId = Input.integer("Your user id: ", 1, Integer.MAX_VALUE);
                comments.insert(taskId, authorId, Input.text("Comment: ", 1000, true));
                System.out.println("Comment added.");
            });
            default -> { }
        }
    }

    private static void searchTasks() throws SQLException {
        String keyword = Input.text("Part of title: ", 100, true);
        System.out.print("Filter by status? ");
        String status = Input.confirm("") ? Input.status("Status") : null;
        printAll(tasks.search(keyword, status));
    }

    private static void reportsMenu() {
        System.out.println("\n-- Reports --\n1. Progress by project\n2. Workload by user\n0. Back");
        switch (Input.integer("Choose: ", 0, 2)) {
            case 1 -> safe(reports::projectProgress);
            case 2 -> safe(reports::userWorkload);
            default -> { }
        }
    }
}
