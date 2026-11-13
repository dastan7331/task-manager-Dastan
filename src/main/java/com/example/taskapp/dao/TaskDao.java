package com.example.taskapp.dao;

import com.example.taskapp.model.Task;
import com.example.taskapp.util.Db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private static final String SELECT =
            "SELECT t.*, p.name AS pn, u.name AS un, c.name AS cn FROM tasks t "
            + "JOIN projects p ON p.id = t.project_id "
            + "LEFT JOIN users u ON u.id = t.assignee_id "
            + "LEFT JOIN categories c ON c.id = t.category_id ";

    private Task map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("due_date");
        return new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getString("status"), rs.getInt("priority"), d == null ? null : d.toLocalDate(),
                rs.getInt("project_id"), rs.getObject("assignee_id", Integer.class),
                rs.getObject("category_id", Integer.class),
                rs.getString("pn"), rs.getString("un"), rs.getString("cn"));
    }

    public List<Task> findAll() throws SQLException {
        List<Task> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT + "ORDER BY t.id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Task> search(String keyword, String status) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = SELECT + "WHERE LOWER(t.title) LIKE ? " + (status != null ? "AND t.status = ? " : "") + "ORDER BY t.id";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            if (status != null) ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(String title, String description, String status, int priority, LocalDate due,
                       int projectId, Integer assigneeId, Integer categoryId) throws SQLException {
        String sql = "INSERT INTO tasks (title, description, status, priority, due_date, project_id, assignee_id, category_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setInt(4, priority);
            ps.setObject(5, due);
            ps.setInt(6, projectId);
            ps.setObject(7, assigneeId);
            ps.setObject(8, categoryId);
            ps.executeUpdate();
        }
    }

    public boolean update(int id, String title, String status, int priority, LocalDate due) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, status = ?, priority = ?, due_date = ? WHERE id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, status);
            ps.setInt(3, priority);
            ps.setObject(4, due);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateFull(int id, String title, String status, int priority, LocalDate due,
                              int projectId, Integer assigneeId, Integer categoryId) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, status = ?, priority = ?, due_date = ?, "
                + "project_id = ?, assignee_id = ?, category_id = ? WHERE id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, status);
            ps.setInt(3, priority);
            ps.setObject(4, due);
            ps.setInt(5, projectId);
            ps.setObject(6, assigneeId);
            ps.setObject(7, categoryId);
            ps.setInt(8, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
