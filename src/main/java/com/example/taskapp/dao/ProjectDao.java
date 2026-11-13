package com.example.taskapp.dao;

import com.example.taskapp.model.Project;
import com.example.taskapp.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
    public List<Project> findAll() throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.owner_id, u.name AS owner_name "
                + "FROM projects p JOIN users u ON u.id = p.owner_id ORDER BY p.id";
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Project(rs.getInt("id"), rs.getString("name"), rs.getInt("owner_id"), rs.getString("owner_name")));
            }
        }
        return list;
    }

    public void insert(String name, int ownerId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO projects (name, owner_id) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, ownerId);
            ps.executeUpdate();
        }
    }

    public boolean update(int id, String name) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE projects SET name = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(int id, String name, int ownerId) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE projects SET name = ?, owner_id = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, ownerId);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM projects WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
