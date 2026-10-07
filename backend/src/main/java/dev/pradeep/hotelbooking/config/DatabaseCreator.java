package dev.pradeep.hotelbooking.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs before the datasource is created. If the PostgreSQL database named in
 * spring.datasource.url does not exist yet, it is created, so the application
 * can start on a fresh PostgreSQL install. Hibernate then creates the tables.
 */
public class DatabaseCreator implements EnvironmentPostProcessor, Ordered {

    private static final String PREFIX = "jdbc:postgresql://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        String url = env.getProperty("spring.datasource.url");
        String user = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        if (url == null || !url.startsWith(PREFIX)) {
            return;
        }

        String rest = url.substring(PREFIX.length());
        int slash = rest.indexOf('/');
        if (slash < 0) {
            return;
        }

        String hostAndPort = rest.substring(0, slash);
        String dbPart = rest.substring(slash + 1);
        int query = dbPart.indexOf('?');
        String dbName = query >= 0 ? dbPart.substring(0, query) : dbPart;

        if (!dbName.matches("[A-Za-z0-9_]+")) {
            return;
        }

        String adminUrl = PREFIX + hostAndPort + "/postgres";

        try (Connection connection = DriverManager.getConnection(adminUrl, user, password);
             PreparedStatement check = connection.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {

            check.setString(1, dbName);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (Statement create = connection.createStatement()) {
                        create.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                        System.out.println("[DatabaseCreator] Created database '" + dbName + "'");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseCreator] Could not verify database '" + dbName + "': " + e.getMessage());
            System.err.println("[DatabaseCreator] Check the username/password in application.properties and that PostgreSQL is running.");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
