package com.example.taskapp.dao;

import com.example.taskapp.model.Comment;
import com.example.taskapp.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {
    public List<Comment> findByTask(int taskId) throws SQLException {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.id, c.task_id, u.name, c.text, c.created_at FROM comments c "
                + "JOIN users u ON u.id = c.author_id WHERE c.task_id = ? ORDER BY c.created_at";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Comment(rs.getInt("id"), rs.getInt("task_id"), rs.getString("name"),
                            rs.getString("text"), rs.getTimestamp("created_at").toString()));
                }
            }
        }
        return list;
    }

    public void insert(int taskId, int authorId, String text) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO comments (task_id, author_id, text) VALUES (?, ?, ?)")) {
            ps.setInt(1, taskId);
            ps.setInt(2, authorId);
            ps.setString(3, text);
            ps.executeUpdate();
        }
    }
}
