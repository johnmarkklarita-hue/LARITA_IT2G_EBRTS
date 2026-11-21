package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key constraints in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // TABLE 1: Users
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fullname TEXT NOT NULL, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "accountstatus TEXT NOT NULL DEFAULT 'ACTIVE');";

            // TABLE 2: Equipment
            String createEquipmentTable = "CREATE TABLE IF NOT EXISTS equipment (" +
                    "equipment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "equipmentname TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL DEFAULT 1, " +
                    "condition TEXT NOT NULL, " + // e.g., Excellent, Good, Fair, Damaged
                    "availabilitystatus TEXT NOT NULL DEFAULT 'AVAILABLE');"; // AVAILABLE, BORROWED, MAINTENANCE

            // TABLE 3: Borrowing / Transactions (with Foreign Keys)
            String createBorrowingTable = "CREATE TABLE IF NOT EXISTS borrowing (" +
                    "borrowing_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "equipment_id INTEGER NOT NULL, " +
                    "borrowdate DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "expectedreturndate DATETIME NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE);";

            // TABLE 4 (Optional): Activity_Logs (with Foreign Key)
            String createActivityLogsTable = "CREATE TABLE IF NOT EXISTS activity_logs (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "action TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "description TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL);";

            // Execute creation queries
            stmt.execute(createUsersTable);
            stmt.execute(createEquipmentTable);
            stmt.execute(createBorrowingTable);
            stmt.execute(createActivityLogsTable);

            // Seed default admin account if not already present
            String seedAdmin = "INSERT OR IGNORE INTO users (user_id, fullname, username, password_hash, role, accountstatus) " +
                    "VALUES (1, 'System Administrator', 'admin', 'admin123', 'ADMIN', 'ACTIVE');";
            stmt.execute(seedAdmin);

            System.out.println("Database and tables initialized successfully according to specification.");

        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
        }
    }
}