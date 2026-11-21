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
            System.out.println("2. Borrow Equipment (Create Transaction)");
            System.out.println("3. View My Borrowed Equipment (Read)");
            System.out.println("4. Return Equipment (Update Transaction)");
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
                    logDao.logAction(borrower.getUserId(), "LOGOUT", "Borrower logged out");
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void viewAvailableEquipment() {
        String sql = "SELECT * FROM equipment WHERE availabilitystatus = 'AVAILABLE' AND quantity > 0";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- Available Equipment ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("equipment_id") +
                        " | Name: " + rs.getString("equipmentname") +
                        " | Category: " + rs.getString("category") +
                        " | Available Qty: " + rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available equipment: " + e.getMessage());
        }
    }

    private void borrowEquipment(User borrower) {
        viewAvailableEquipment();
        System.out.print("Enter Equipment ID to borrow: ");
        try {
            int equipmentId = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter expected return date (YYYY-MM-DD): ");
            String returnDate = scanner.nextLine();

            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transaction start

            try {
                // 1. Insert into borrowing table
                String borrowSql = "INSERT INTO borrowing (user_id, equipment_id, expectedreturndate) VALUES (?, ?, ?)";
                PreparedStatement borrowStmt = conn.prepareStatement(borrowSql);
                borrowStmt.setInt(1, borrower.getUserId());
                borrowStmt.setInt(2, equipmentId);
                borrowStmt.setString(3, returnDate);
                borrowStmt.executeUpdate();

                // 2. Update equipment availability/quantity
                String updateEq = "UPDATE equipment SET quantity = quantity - 1, availabilitystatus = CASE WHEN quantity - 1 <= 0 THEN 'BORROWED' ELSE availabilitystatus END WHERE equipment_id = ? AND availabilitystatus = 'AVAILABLE'";
                PreparedStatement updateStmt = conn.prepareStatement(updateEq);
                updateStmt.setInt(1, equipmentId);
                int rows = updateStmt.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    System.out.println("Equipment borrowed successfully!");
                    logDao.logAction(borrower.getUserId(), "BORROW_EQUIPMENT", "Borrowed equipment ID: " + equipmentId);
                } else {
                    conn.rollback();
                    System.out.println("Failed to borrow equipment. Item might be unavailable.");
                }
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Transaction error: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error processing borrow request: " + e.getMessage());
        }
    }

    private void viewMyEquipment(User borrower) {
        String sql = "SELECT b.borrowing_id, e.equipment_id, e.equipmentname, e.category, b.borrowdate, b.expectedreturndate " +
                "FROM borrowing b JOIN equipment e ON b.equipment_id = e.equipment_id " +
                "WHERE b.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, borrower.getUserId());
            ResultSet rs = pstmt.executeQuery();
            System.out.println("\n--- My Borrowed Equipment ---");
            while (rs.next()) {
                System.out.println("Transaction ID: " + rs.getInt("borrowing_id") +
                        " | Equipment ID: " + rs.getInt("equipment_id") +
                        " | Name: " + rs.getString("equipmentname") +
                        " | Borrow Date: " + rs.getString("borrowdate") +
                        " | Return Due: " + rs.getString("expectedreturndate"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrowed equipment: " + e.getMessage());
        }
    }

    private void returnEquipment(User borrower) {
        viewMyEquipment(borrower);
        System.out.print("Enter Borrowing ID to return: ");
        try {
            int borrowingId = Integer.parseInt(scanner.nextLine());

            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                PreparedStatement getEq = conn.prepareStatement("SELECT equipment_id FROM borrowing WHERE borrowing_id = ? AND user_id = ?");
                getEq.setInt(1, borrowingId);
                getEq.setInt(2, borrower.getUserId());
                ResultSet rs = getEq.executeQuery();

                if (rs.next()) {
                    int equipmentId = rs.getInt("equipment_id");

                    // 1. Delete borrowing transaction
                    PreparedStatement delTrans = conn.prepareStatement("DELETE FROM borrowing WHERE borrowing_id = ?");
                    delTrans.setInt(1, borrowingId);
                    delTrans.executeUpdate();

                    // 2. Restore equipment quantity & status
                    PreparedStatement updateEq = conn.prepareStatement("UPDATE equipment SET quantity = quantity + 1, availabilitystatus = 'AVAILABLE' WHERE equipment_id = ?");
                    updateEq.setInt(1, equipmentId);
                    updateEq.executeUpdate();

                    conn.commit();
                    System.out.println("Equipment returned successfully!");
                    logDao.logAction(borrower.getUserId(), "RETURN_EQUIPMENT", "Returned borrowing transaction ID: " + borrowingId);
                } else {
                    conn.rollback();
                    System.out.println("Invalid Borrowing ID or unauthorized.");
                }
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Return transaction error: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (NumberFormatException | SQLException e) {
            System.err.println("Error returning equipment: " + e.getMessage());
        }
    }
}