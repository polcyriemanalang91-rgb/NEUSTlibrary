package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDAO {

    private static final Logger LOGGER = Logger.getLogger(AdminDAO.class.getName());

    // ── AUTHENTICATION ────────────────────────────────────────────────────────

    public Admin authenticate(String username, String password) {
        String sql = "SELECT * FROM Admin WHERE Username = ? AND IsActive = 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("PasswordHash");
                    if (PasswordUtil.verifyPassword(password, storedHash)) {
                        return mapAdmin(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Admin authentication error.", e);
        }
        return null;
    }

    // ── LIBRARIAN MANAGEMENT ──────────────────────────────────────────────────

    public List<Librarian> getAllLibrarians() {
        List<Librarian> list = new ArrayList<>();
        String sql = "SELECT * FROM Librarian ORDER BY LastName, FirstName";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapLibrarian(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching librarians.", e);
        }
        return list;
    }

    public boolean addLibrarian(Librarian librarian, String plainPassword) {
        String sql = "INSERT INTO Librarian (EmployeeID, FirstName, LastName, Email, " +
                     "PasswordHash, ContactNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, librarian.getEmployeeID());
            ps.setString(2, librarian.getFirstName());
            ps.setString(3, librarian.getLastName());
            ps.setString(4, librarian.getEmail());
            ps.setString(5, PasswordUtil.hashPassword(plainPassword));
            ps.setString(6, librarian.getContactNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding librarian.", e);
        }
        return false;
    }

    public boolean deactivateLibrarian(int librarianID) {
        String sql = "UPDATE Librarian SET IsActive = 0, UpdatedAt = GETDATE() WHERE LibrarianID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, librarianID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating librarian.", e);
        }
        return false;
    }

    public boolean activateLibrarian(int librarianID) {
        String sql = "UPDATE Librarian SET IsActive = 1, UpdatedAt = GETDATE() WHERE LibrarianID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, librarianID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error activating librarian.", e);
        }
        return false;
    }

    // ── REPORTS ───────────────────────────────────────────────────────────────

    public int[] getSummaryReport() {
        int[] stats = new int[4];
        String[] queries = {
            "SELECT COUNT(*) FROM Book WHERE IsActive = 1",
            "SELECT COUNT(*) FROM Member WHERE IsActive = 1",
            "SELECT COUNT(*) FROM BorrowedRecord WHERE Status IN ('Borrowed','Overdue')",
            "SELECT COUNT(*) FROM BorrowedRecord WHERE Status = 'Overdue'"
        };
        try (Connection con = DatabaseConnection.getConnection()) {
            for (int i = 0; i < queries.length; i++) {
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(queries[i])) {
                    if (rs.next()) stats[i] = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching summary report.", e);
        }
        return stats;
    }

    // ── SYSTEM SETTINGS ───────────────────────────────────────────────────────

    public String getSetting(String key) {
        String sql = "SELECT SettingValue FROM SystemSettings WHERE SettingKey = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("SettingValue");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching setting.", e);
        }
        return null;
    }

    public boolean updateSetting(String key, String value, int adminID) {
        String sql = "UPDATE SystemSettings SET SettingValue = ?, UpdatedBy = ?, " +
                     "UpdatedAt = GETDATE() WHERE SettingKey = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt   (2, adminID);
            ps.setString(3, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating setting.", e);
        }
        return false;
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId    (rs.getInt("AdminID"));
        a.setUsername   (rs.getString("Username"));
        a.setFullName   (rs.getString("FullName"));
        a.setEmail      (rs.getString("Email"));
        a.setPasswordHash(rs.getString("PasswordHash"));
        a.setActive     (rs.getBoolean("IsActive"));
        Timestamp ca = rs.getTimestamp("CreatedAt");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        return a;
    }

    private Librarian mapLibrarian(ResultSet rs) throws SQLException {
        Librarian l = new Librarian();
        l.setLibrarianID  (rs.getInt("LibrarianID"));
        l.setEmployeeID   (rs.getString("EmployeeID"));
        l.setFirstName    (rs.getString("FirstName"));
        l.setLastName     (rs.getString("LastName"));
        l.setEmail        (rs.getString("Email"));
        l.setPasswordHash (rs.getString("PasswordHash"));
        l.setContactNumber(rs.getString("ContactNumber"));
        l.setActive       (rs.getBoolean("IsActive"));
        Timestamp ca = rs.getTimestamp("CreatedAt");
        if (ca != null) l.setCreatedAt(ca.toLocalDateTime());
        return l;
    }
}