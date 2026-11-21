package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseConnection;
import com.scc.model.User;

import java.sql.*;
import java.util.Scanner;

public class BorrowerMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void showMenu(User borrower) {
        while (true) {
            System.out.println("\n--- BORROWER MENU ---");
            System.out.println("1. View Available Equipment (Read)");
            System.out.println("2. Borrow Equipment (Update)");
            System.out.println("3. View My Borrowed Equipment (Read)");
            System.out.println("4. Return Equipment (Update)");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAvailableEquipment();
                    break;
                case "2":
                    borrowEquipment(borrower);
                    break;
                case "3":
                    viewMyEquipment(borrower);
                    break;
                case "4":
                    returnEquipment(borrower);
                    break;
                case "5":
                    logDao.logAction(borrower.getUsername(), "Logged out");
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void viewAvailableEquipment() {
        String sql = "SELECT * FROM equipment WHERE status = 'AVAILABLE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- Available Equipment ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | Name: " + rs.getString("name") +
                        " | Category: " + rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available equipment: " + e.getMessage());
        }
    }

    private void borrowEquipment(User borrower) {
        viewAvailableEquipment();
        System.out.print("Enter Equipment ID to borrow: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            String sql = "UPDATE equipment SET status = 'BORROWED', borrowed_by = ? WHERE id = ? AND status = 'AVAILABLE'";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, borrower.getUsername());
                pstmt.setInt(2, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Equipment borrowed successfully!");
                    logDao.logAction(borrower.getUsername(), "Borrowed equipment ID: " + id);
                } else {
                    System.out.println("Equipment unavailable or invalid ID.");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error borrowing equipment: " + e.getMessage());
        }
    }

    private void viewMyEquipment(User borrower) {
        String sql = "SELECT * FROM equipment WHERE borrowed_by = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, borrower.getUsername());
            ResultSet rs = pstmt.executeQuery();
            System.out.println("\n--- My Borrowed Equipment ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | Name: " + rs.getString("name") +
                        " | Category: " + rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrowed equipment: " + e.getMessage());
        }
    }

    private void returnEquipment(User borrower) {
        viewMyEquipment(borrower);
        System.out.print("Enter Equipment ID to return: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            String sql = "UPDATE equipment SET status = 'AVAILABLE', borrowed_by = NULL WHERE id = ? AND borrowed_by = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, borrower.getUsername());

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("Equipment returned successfully!");
                    logDao.logAction(borrower.getUsername(), "Returned equipment ID: " + id);
                } else {
                    System.out.println("Invalid Equipment ID or you did not borrow this item.");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error returning equipment: " + e.getMessage());
        }
    }
}