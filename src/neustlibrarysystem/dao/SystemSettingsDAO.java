package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.SystemSettings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemSettingsDAO {

    public List<SystemSettings> getAllSettings() throws SQLException {
        List<SystemSettings> list = new ArrayList<>();
        String sql = "SELECT s.*, a.FullName AS UpdatedByName FROM SystemSettings s " +
                     "LEFT JOIN Admin a ON s.UpdatedBy = a.AdminID ORDER BY s.SettingKey";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public SystemSettings getSettingByKey(String key) throws SQLException {
        String sql = "SELECT * FROM SystemSettings WHERE SettingKey = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public String getSettingValue(String key) throws SQLException {
        SystemSettings s = getSettingByKey(key);
        return s != null ? s.getSettingValue() : null;
    }

    public int getSettingInt(String key, int defaultValue) {
        try {
            String val = getSettingValue(key);
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (Exception e) { return defaultValue; }
    }

    public double getSettingDouble(String key, double defaultValue) {
        try {
            String val = getSettingValue(key);
            return val != null ? Double.parseDouble(val) : defaultValue;
        } catch (Exception e) { return defaultValue; }
    }

    public boolean updateSetting(String key, String value, int adminID) throws SQLException {
        String sql = "UPDATE SystemSettings SET SettingValue = ?, UpdatedBy = ?, UpdatedAt = GETDATE() WHERE SettingKey = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, adminID);
            ps.setString(3, key);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSetting(SystemSettings setting) throws SQLException {
        String sql = "UPDATE SystemSettings SET SettingValue = ?, Description = ?, UpdatedBy = ?, UpdatedAt = GETDATE() WHERE SettingID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, setting.getSettingValue());
            ps.setString(2, setting.getDescription());
            if (setting.getUpdatedBy() != null) ps.setInt(3, setting.getUpdatedBy());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, setting.getSettingID());
            return ps.executeUpdate() > 0;
        }
    }

    private SystemSettings mapRow(ResultSet rs) throws SQLException {
        SystemSettings s = new SystemSettings();
        s.setSettingID(rs.getInt("SettingID"));
        s.setSettingKey(rs.getString("SettingKey"));
        s.setSettingValue(rs.getString("SettingValue"));
        s.setDescription(rs.getString("Description"));
        int updatedBy = rs.getInt("UpdatedBy");
        if (!rs.wasNull()) s.setUpdatedBy(updatedBy);
        Timestamp ts = rs.getTimestamp("UpdatedAt");
        if (ts != null) s.setUpdatedAt(ts.toLocalDateTime());
        try { s.setUpdatedByName(rs.getString("UpdatedByName")); } catch (Exception ignored) {}
        return s;
    }
}