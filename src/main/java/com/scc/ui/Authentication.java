package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner = new Scanner(System.in);

    public void showLoginMenu() {
        while (true) {
            System.out.println("\n=== EQUIPMENT SYSTEM ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.getUserByUsername(username);

        if (user != null && user.getPasswordHash().equals(password)) {
            if (!"ACTIVE".equalsIgnoreCase(user.getAccountStatus()) && !"APPROVED".equalsIgnoreCase(user.getAccountStatus())) {
                System.out.println("Access denied. Account status is: " + user.getAccountStatus());
                return;
            }

            System.out.println("Login successful! Welcome, " + user.getFullname() + " (" + user.getRole() + ")");
            logDao.logAction(user.getUserId(), "LOGIN", "User logged in");

            switch (user.getRole().toUpperCase()) {
                case "ADMIN":
                    new AdminMenu().showMenu(user);
                    break;
                case "STAFF":
                    new StaffMenu().showMenu(user);
                    break;
                case "BORROWER":
                    new BorrowerMenu().showMenu(user);
                    break;
                default:
                    System.out.println("Unknown user role.");
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER NEW ACCOUNT ---");
        System.out.print("Enter full name: ");
        String fullname = scanner.nextLine().trim();

        System.out.print("Enter desired username: ");
        String username = scanner.nextLine().trim();

        if (fullname.isEmpty() || username.isEmpty()) {
            System.out.println("Full name and username cannot be empty.");
            return;
        }

        if (userDao.getUserByUsername(username) != null) {
            System.out.println("Username already exists. Please choose another.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Select role (STAFF / BORROWER) [Default: BORROWER]: ");
        String role = scanner.nextLine().trim().toUpperCase();
        if (!role.equals("STAFF") && !role.equals("BORROWER")) {
            role = "BORROWER";
        }

        User newUser = new User(fullname, username, password, role, "ACTIVE");
        if (userDao.addUser(newUser)) {
            System.out.println("Registration successful! You can now log in as " + role + ".");
            User created = userDao.getUserByUsername(username);
            logDao.logAction(created != null ? created.getUserId() : null, "REGISTER", "Registered new " + role + " account: " + username);
        } else {
            System.out.println("Registration failed.");
        }
    }
}