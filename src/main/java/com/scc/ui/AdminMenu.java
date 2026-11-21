package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void showMenu(User admin) {
        while (true) {
            System.out.println("\n--- ADMIN MENU (USER CRUD) ---");
            System.out.println("1. Create User");
            System.out.println("2. Read (View) All Users");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. View Activity Logs");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createUser(admin);
                    break;
                case "2":
                    viewAllUsers();
                    break;
                case "3":
                    updateUser(admin);
                    break;
                case "4":
                    deleteUser(admin);
                    break;
                case "5":
                    viewLogs();
                    break;
                case "6":
                    logDao.logAction(admin.getUserId(), "LOGOUT", "Admin logged out");
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void createUser(User admin) {
        System.out.print("Enter full name: ");
        String fullname = scanner.nextLine();
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter role (ADMIN/STAFF/BORROWER): ");
        String role = scanner.nextLine().toUpperCase();

        if (userDao.addUser(new User(fullname, username, password, role, "ACTIVE"))) {
            System.out.println("User created successfully!");
            logDao.logAction(admin.getUserId(), "CREATE_USER", "Created user: " + username);
        } else {
            System.out.println("Failed to create user.");
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n--- User List ---");
        for (User u : users) {
            System.out.println("ID: " + u.getUserId() +
                    " | Full Name: " + u.getFullname() +
                    " | Username: " + u.getUsername() +
                    " | Role: " + u.getRole() +
                    " | Status: " + u.getAccountStatus());
        }
    }

    private void updateUser(User admin) {
        viewAllUsers();
        System.out.print("Enter User ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine();
            System.out.print("Enter new role (ADMIN/STAFF/BORROWER): ");
            String newRole = scanner.nextLine().toUpperCase();
            System.out.print("Enter new status (ACTIVE/PENDING/SUSPENDED): ");
            String newStatus = scanner.nextLine().toUpperCase();

            if (userDao.updateUser(id, newPassword, newRole, newStatus)) {
                System.out.println("User updated successfully!");
                logDao.logAction(admin.getUserId(), "UPDATE_USER", "Updated user ID: " + id);
            } else {
                System.out.println("User ID not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        }
    }

    private void deleteUser(User admin) {
        viewAllUsers();
        System.out.print("Enter User ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            if (id == admin.getUserId()) {
                System.out.println("You cannot delete your own active admin account.");
                return;
            }
            if (userDao.deleteUser(id)) {
                System.out.println("User deleted successfully!");
                logDao.logAction(admin.getUserId(), "DELETE_USER", "Deleted user ID: " + id);
            } else {
                System.out.println("User ID not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        }
    }

    private void viewLogs() {
        List<ActivityLog> logs = logDao.getAllLogs();
        System.out.println("\n--- Activity Logs ---");
        for (ActivityLog log : logs) {
            System.out.println("[" + log.getTimestamp() + "] User ID " + log.getUsername() + ": " + log.getAction());
        }
    }
}