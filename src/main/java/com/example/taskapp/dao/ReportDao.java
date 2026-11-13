package com.example.taskapp.dao;

import com.example.taskapp.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDao {

    public void projectProgress() throws SQLException {
        String sql = "SELECT p.name, COUNT(t.id) AS total, "
                + "COUNT(t.id) FILTER (WHERE t.status = 'DONE') AS done "
                + "FROM projects p LEFT JOIN tasks t ON t.project_id = p.id "
                + "GROUP BY p.id, p.name ORDER BY p.id";
        System.out.printf("%-20s %6s %6s %8s%n", "Project", "Tasks", "Done", "Done %");
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int total = rs.getInt("total");
                int done = rs.getInt("done");
                System.out.printf("%-20s %6d %6d %7d%%%n", rs.getString("name"), total, done,
                        total == 0 ? 0 : done * 100 / total);
            }
        }
    }

    public void userWorkload() throws SQLException {
        String sql = "SELECT u.name, COUNT(t.id) AS total, COALESCE(ROUND(AVG(t.priority), 1), 0) AS avg_priority, "
                + "COUNT(t.id) FILTER (WHERE t.status <> 'DONE' AND t.due_date < CURRENT_DATE) AS overdue "
                + "FROM users u LEFT JOIN tasks t ON t.assignee_id = u.id "
                + "GROUP BY u.id, u.name ORDER BY total DESC, u.name";
        System.out.printf("%-20s %6s %13s %8s%n", "User", "Tasks", "Avg priority", "Overdue");
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%-20s %6d %13s %8d%n", rs.getString("name"), rs.getInt("total"),
                        rs.getBigDecimal("avg_priority"), rs.getInt("overdue"));
            }
        }
    }

    public List<Map<String, Object>> projectProgressData() throws SQLException {
        String sql = "SELECT p.name, COUNT(t.id) AS total, "
                + "COUNT(t.id) FILTER (WHERE t.status = 'DONE') AS done "
                + "FROM projects p LEFT JOIN tasks t ON t.project_id = p.id "
                + "GROUP BY p.id, p.name ORDER BY p.id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                int total = rs.getInt("total");
                int done = rs.getInt("done");
                row.put("name", rs.getString("name"));
                row.put("total", total);
                row.put("done", done);
                row.put("percent", total == 0 ? 0 : done * 100 / total);
                list.add(row);
            }
        }
        return list;
    }

    public List<Map<String, Object>> userWorkloadData() throws SQLException {
        String sql = "SELECT u.name, COUNT(t.id) AS total, COALESCE(ROUND(AVG(t.priority), 1), 0) AS avg_priority, "
                + "COUNT(t.id) FILTER (WHERE t.status <> 'DONE' AND t.due_date < CURRENT_DATE) AS overdue "
                + "FROM users u LEFT JOIN tasks t ON t.assignee_id = u.id "
                + "GROUP BY u.id, u.name ORDER BY total DESC, u.name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", rs.getString("name"));
                row.put("total", rs.getInt("total"));
                row.put("avgPriority", rs.getBigDecimal("avg_priority"));
                row.put("overdue", rs.getInt("overdue"));
                list.add(row);
            }
        }
        return list;
    }
}
