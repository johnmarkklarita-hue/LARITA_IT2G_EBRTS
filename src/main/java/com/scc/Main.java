package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        // Initialize the SQLite database and create tables if not existing
        DatabaseInitializer.initializeDatabase();

        // Launch the application login prompt
        Authentication auth = new Authentication();
        auth.showLoginMenu();
    }
}