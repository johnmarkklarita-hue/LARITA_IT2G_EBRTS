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
                    logDao.logAction(staff.getUserId(), "LOGOUT", "Staff logged out");
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
        System.out.print("Enter quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter condition (e.g., Good, Fair, New): ");
        String condition = scanner.nextLine();

        String sql = "INSERT INTO equipment (equipmentname, category, quantity, condition, availabilitystatus) VALUES (?, ?, ?, ?, 'AVAILABLE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, condition);
            pstmt.executeUpdate();
            System.out.println("Equipment added successfully!");
            logDao.logAction(staff.getUserId(), "CREATE_EQUIPMENT", "Added equipment: " + name);
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
                System.out.println("ID: " + rs.getInt("equipment_id") +
                        " | Name: " + rs.getString("equipmentname") +
                        " | Category: " + rs.getString("category") +
                        " | Qty: " + rs.getInt("quantity") +
                        " | Condition: " + rs.getString("condition") +
                        " | Status: " + rs.getString("availabilitystatus"));
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
            System.out.print("Enter new quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter new condition: ");
            String condition = scanner.nextLine();
            System.out.print("Enter new availability status (AVAILABLE / BORROWED / MAINTENANCE): ");
            String status = scanner.nextLine().toUpperCase();

            String sql = "UPDATE equipment SET equipmentname = ?, category = ?, quantity = ?, condition = ?, availabilitystatus = ? WHERE equipment_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, category);
                pstmt.setInt(3, quantity);
                pstmt.setString(4, condition);
                pstmt.setString(5, status);
                pstmt.setInt(6, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Equipment updated successfully!");
                    logDao.logAction(staff.getUserId(), "UPDATE_EQUIPMENT", "Updated equipment ID: " + id);
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
            String sql = "DELETE FROM equipment WHERE equipment_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Equipment deleted successfully!");
                    logDao.logAction(staff.getUserId(), "DELETE_EQUIPMENT", "Deleted equipment ID: " + id);
                } else {
                    System.out.println("Equipment ID not found.");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error deleting equipment: " + e.getMessage());
        }
    }
}