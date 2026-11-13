package com.example.taskapp.dao;

import com.example.taskapp.model.Category;
import com.example.taskapp.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM categories ORDER BY id")) {
            while (rs.next()) list.add(new Category(rs.getInt("id"), rs.getString("name")));
        }
        return list;
    }

    public void insert(String name) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}
