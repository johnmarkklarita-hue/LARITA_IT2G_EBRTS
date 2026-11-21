package com.scc.model;

public class User {
    private int userId;
    private String fullname;
    private String username;
    private String passwordHash;
    private String role;
    private String accountStatus;

    public User() {}

    public User(int userId, String fullname, String username, String passwordHash, String role, String accountStatus) {
        this.userId = userId;
        this.fullname = fullname;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public User(String fullname, String username, String passwordHash, String role, String accountStatus) {
        this.fullname = fullname;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}