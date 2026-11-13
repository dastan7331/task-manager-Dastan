package com.example.taskapp.dao;

import com.example.taskapp.model.User;
import com.example.taskapp.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, email FROM users ORDER BY id")) {
            while (rs.next()) list.add(new User(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
        }
        return list;
    }

    public void insert(String name, String email) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public boolean update(int id, String name, String email) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET name = ?, email = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
