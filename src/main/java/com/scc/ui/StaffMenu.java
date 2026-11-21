package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseConnection;
import com.scc.model.User;

import java.sql.*;
import java.util.Scanner;

public class StaffMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void showMenu(User staff) {
        while (true) {
            System.out.println("\n--- STAFF MENU (EQUIPMENT CRUD) ---");
            System.out.println("1. Add Equipment (Create)");
            System.out.println("2. View All Equipment (Read)");
            System.out.println("3. Update Equipment (Update)");
            System.out.println("4. Delete Equipment (Delete)");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addEquipment(staff);
                    break;
                case "2":
                    viewEquipment();
                    break;
                case "3":
                    updateEquipment(staff);
                    break;
                case "4":
                    deleteEquipment(staff);
                    break;
                case "5":
                    logDao.logAction(staff.getUsername(), "Logged out");
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void addEquipment(User staff) {
        System.out.print("Enter equipment name: ");
        String name = scanner.nextLine();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        String sql = "INSERT INTO equipment (name, category, status, borrowed_by) VALUES (?, ?, 'AVAILABLE', NULL)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.executeUpdate();
            System.out.println("Equipment added successfully!");
            logDao.logAction(staff.getUsername(), "Added equipment: " + name);
        } catch (SQLException e) {
            System.err.println("Error adding equipment: " + e.getMessage());
        }
    }

    private void viewEquipment() {
        String sql = "SELECT * FROM equipment";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- Equipment Inventory ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | Name: " + rs.getString("name") +
                        " | Category: " + rs.getString("category") +
                        " | Status: " + rs.getString("status") +
                        " | Borrowed By: " + (rs.getString("borrowed_by") == null ? "None" : rs.getString("borrowed_by")));
            }
        } catch (SQLException e) {
            System.err.println("Error reading equipment: " + e.getMessage());
        }
    }

    private void updateEquipment(User staff) {
        viewEquipment();
        System.out.print("Enter Equipment ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter new name: ");
            String name = scanner.nextLine();
            System.out.print("Enter new category: ");
            String category = scanner.nextLine();
            System.out.print("Enter new status (AVAILABLE / BORROWED / MAINTENANCE): ");
            String status = scanner.nextLine().toUpperCase();

            String sql = "UPDATE equipment SET name = ?, category = ?, status = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, category);
                pstmt.setString(3, status);
                pstmt.setInt(4, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Equipment updated successfully!");
                    logDao.logAction(staff.getUsername(), "Updated equipment ID: " + id);
                } else {
                    System.out.println("Equipment ID not found.");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error updating equipment: " + e.getMessage());
        }
    }

    private void deleteEquipment(User staff) {
        viewEquipment();
        System.out.print("Enter Equipment ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            String sql = "DELETE FROM equipment WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Equipment deleted successfully!");
                    logDao.logAction(staff.getUsername(), "Deleted equipment ID: " + id);
                } else {
                    System.out.println("Equipment ID not found.");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error deleting equipment: " + e.getMessage());
        }
    }
}