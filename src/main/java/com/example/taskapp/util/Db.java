package com.example.taskapp.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class Db {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            PROPS.load(in);
            initIfEmpty();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialise database", e);
        }
    }

    private Db() { }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"), PROPS.getProperty("db.user"), PROPS.getProperty("db.password"));
    }

    private static void initIfEmpty() throws Exception {
        try (Connection c = get()) {
            try (Statement st = c.createStatement()) {
                st.executeQuery("SELECT 1 FROM users WHERE 1 = 0");
                return;
            } catch (SQLException e) {

            }
            runScript(c, "/schema.sql");
            runScript(c, "/seed.sql");
        }
    }

    private static void runScript(Connection c, String resource) throws Exception {
        String sql;
        try (InputStream in = Db.class.getResourceAsStream(resource)) {
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String part : sql.split(";")) {
            if (part.isBlank()) continue;
            try (Statement st = c.createStatement()) {
                st.execute(part);
            }
        }
    }
}
