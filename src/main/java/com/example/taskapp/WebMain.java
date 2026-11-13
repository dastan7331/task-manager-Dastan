package com.example.taskapp;

import com.example.taskapp.dao.*;
import com.example.taskapp.util.Db;
import com.example.taskapp.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class WebMain {
    private static final int PORT = 8080;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final UserDao users = new UserDao();
    private static final ProjectDao projects = new ProjectDao();
    private static final CategoryDao categories = new CategoryDao();
    private static final TaskDao tasks = new TaskDao();
    private static final ReportDao reports = new ReportDao();

    public static void main(String[] args) throws Exception {
        Db.get().close();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/", WebMain::handle);
        server.start();
        String url = "http://localhost:" + PORT;
        System.out.println("Task Manager is running: " + url);
        openBrowser(url);
    }

    private static void openBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            }
        } catch (Exception e) {
            System.out.println("Open " + url + " in your browser.");
        }
    }

    private static void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (!path.startsWith("/api/")) {
                page(ex);
                return;
            }
            String[] parts = path.substring(5).split("/");
            Object result = route(ex.getRequestMethod(), parts, query(ex.getRequestURI().getRawQuery()), form(ex));
            send(ex, 200, "application/json", Json.of(result));
        } catch (IllegalArgumentException e) {
            send(ex, 400, "application/json", Json.of(Map.of("error", e.getMessage())));
        } catch (SQLException e) {
            String state = e.getSQLState();
            String text = "23505".equals(state) ? "Такое значение уже существует."
                    : ("23503".equals(state) || "23506".equals(state) || "23504".equals(state))
                    ? "Связанная запись не найдена или используется." : "Ошибка базы данных: " + e.getMessage();
            send(ex, 409, "application/json", Json.of(Map.of("error", text)));
        } catch (Exception e) {
            send(ex, 500, "application/json", Json.of(Map.of("error", "Ошибка сервера: " + e)));
        }
    }

    private static Object route(String method, String[] p, Map<String, String> q, Map<String, String> f) throws SQLException {
        String resource = p[0];
        Integer id = p.length > 1 ? Integer.valueOf(p[1]) : null;
        switch (resource) {
            case "users" -> {
                switch (method) {
                    case "GET" -> { return users.findAll(); }
                    case "POST" -> { users.insert(text(f, "name", 100), email(f)); return ok(); }
                    case "PUT" -> { found(users.update(id, text(f, "name", 100), email(f))); return ok(); }
                    case "DELETE" -> { found(users.delete(id)); return ok(); }
                    default -> { }
                }
            }
            case "projects" -> {
                switch (method) {
                    case "GET" -> { return projects.findAll(); }
                    case "POST" -> { projects.insert(text(f, "name", 100), number(f, "ownerId", 1, Integer.MAX_VALUE)); return ok(); }
                    case "PUT" -> {
                        found(projects.update(id, text(f, "name", 100), number(f, "ownerId", 1, Integer.MAX_VALUE)));
                        return ok();
                    }
                    case "DELETE" -> { found(projects.delete(id)); return ok(); }
                    default -> { }
                }
            }
            case "tasks" -> {
                switch (method) {
                    case "GET" -> {
                        String status = q.getOrDefault("status", "");
                        return tasks.search(q.getOrDefault("q", ""), status.isEmpty() ? null : status);
                    }
                    case "POST" -> {
                        tasks.insert(text(f, "title", 150), null, status(f), number(f, "priority", 1, 5), date(f),
                                number(f, "projectId", 1, Integer.MAX_VALUE), optional(f, "assigneeId"), optional(f, "categoryId"));
                        return ok();
                    }
                    case "PUT" -> {
                        found(tasks.updateFull(id, text(f, "title", 150), status(f), number(f, "priority", 1, 5), date(f),
                                number(f, "projectId", 1, Integer.MAX_VALUE), optional(f, "assigneeId"), optional(f, "categoryId")));
                        return ok();
                    }
                    case "DELETE" -> { found(tasks.delete(id)); return ok(); }
                    default -> { }
                }
            }
            case "categories" -> {
                if (method.equals("GET")) return categories.findAll();
            }
            case "reports" -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("projects", reports.projectProgressData());
                r.put("users", reports.userWorkloadData());
                return r;
            }
            default -> { }
        }
        throw new IllegalArgumentException("Неизвестный запрос: " + method + " /api/" + String.join("/", p));
    }

    private static String text(Map<String, String> f, String key, int max) {
        String s = f.getOrDefault(key, "").trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Поле «" + key + "» обязательно.");
        if (s.length() > max) throw new IllegalArgumentException("Поле «" + key + "» слишком длинное (максимум " + max + ").");
        return s;
    }

    private static String email(Map<String, String> f) {
        String s = text(f, "email", 150);
        if (!EMAIL.matcher(s).matches()) throw new IllegalArgumentException("Неверный формат email.");
        return s;
    }

    private static int number(Map<String, String> f, String key, int min, int max) {
        try {
            int n = Integer.parseInt(f.getOrDefault(key, "").trim());
            if (n < min || n > max) throw new IllegalArgumentException("Поле «" + key + "»: число от " + min + " до " + max + ".");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + key + "» должно быть числом.");
        }
    }

    private static Integer optional(Map<String, String> f, String key) {
        String s = f.getOrDefault(key, "").trim();
        return s.isEmpty() ? null : number(f, key, 1, Integer.MAX_VALUE);
    }

    private static LocalDate date(Map<String, String> f) {
        String s = f.getOrDefault("dueDate", "").trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Дата должна быть в формате гггг-мм-дд.");
        }
    }

    private static String status(Map<String, String> f) {
        String s = f.getOrDefault("status", "").trim().toUpperCase();
        if (!s.equals("TODO") && !s.equals("IN_PROGRESS") && !s.equals("DONE")) {
            throw new IllegalArgumentException("Статус: TODO, IN_PROGRESS или DONE.");
        }
        return s;
    }

    private static void found(boolean ok) {
        if (!ok) throw new IllegalArgumentException("Запись не найдена.");
    }

    private static Map<String, Object> ok() {
        return Map.of("ok", true);
    }

    private static Map<String, String> form(HttpExchange ex) throws IOException {
        return parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private static Map<String, String> query(String raw) {
        return raw == null ? new HashMap<>() : parse(raw);
    }

    private static Map<String, String> parse(String s) {
        Map<String, String> map = new HashMap<>();
        if (s.isBlank()) return map;
        for (String pair : s.split("&")) {
            int i = pair.indexOf('=');
            String k = URLDecoder.decode(i < 0 ? pair : pair.substring(0, i), StandardCharsets.UTF_8);
            String v = i < 0 ? "" : URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8);
            map.put(k, v);
        }
        return map;
    }

    private static void page(HttpExchange ex) throws IOException {
        try (InputStream in = WebMain.class.getResourceAsStream("/static/index.html")) {
            send(ex, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type.startsWith("application/json") ? "application/json; charset=utf-8" : type);
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
