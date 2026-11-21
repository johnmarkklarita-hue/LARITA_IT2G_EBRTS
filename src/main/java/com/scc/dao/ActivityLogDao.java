package com.scc.dao;

import com.scc.model.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logAction(Integer userId, String action, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                pstmt.setInt(1, userId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, action);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        String.valueOf(rs.getInt("user_id")),
                        rs.getString("action") + " - " + rs.getString("description"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return logs;
    }
}