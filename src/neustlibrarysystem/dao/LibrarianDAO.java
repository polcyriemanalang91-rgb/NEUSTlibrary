package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for Librarian authentication and management.
 */
public class LibrarianDAO {

    private static final Logger LOGGER = Logger.getLogger(LibrarianDAO.class.getName());

    // ── AUTH ──────────────────────────────────────────────────────────────────

    public Librarian login(String email, String plainPassword) {
        String sql = "SELECT * FROM Librarian WHERE Email = ? AND IsActive = 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (PasswordUtil.verifyPassword(plainPassword, rs.getString("PasswordHash")))
                        return map(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Librarian login error.", e);
        }
        return null;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Librarian> getAll() {
        List<Librarian> list = new ArrayList<>();
        String sql = "SELECT * FROM Librarian ORDER BY LastName, FirstName";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching librarians.", e);
        }
        return list;
    }

    public Librarian getByID(int id) {
        String sql = "SELECT * FROM Librarian WHERE LibrarianID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching librarian.", e);
        }
        return null;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public boolean add(Librarian lib, String plainPassword) {
        String sql = "INSERT INTO Librarian (EmployeeID, FirstName, LastName, Email, PasswordHash, ContactNumber) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lib.getEmployeeID());
            ps.setString(2, lib.getFirstName());
            ps.setString(3, lib.getLastName());
            ps.setString(4, lib.getEmail());
            ps.setString(5, PasswordUtil.hashPassword(plainPassword));
            ps.setString(6, lib.getContactNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding librarian.", e);
        }
        return false;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean update(Librarian lib) {
        String sql = "UPDATE Librarian SET FirstName=?, LastName=?, Email=?, ContactNumber=?, UpdatedAt=GETDATE() WHERE LibrarianID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lib.getFirstName());
            ps.setString(2, lib.getLastName());
            ps.setString(3, lib.getEmail());
            ps.setString(4, lib.getContactNumber());
            ps.setInt   (5, lib.getLibrarianID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating librarian.", e);
        }
        return false;
    }

    public boolean deactivate(int id) {
        String sql = "UPDATE Librarian SET IsActive=0, UpdatedAt=GETDATE() WHERE LibrarianID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating librarian.", e);
        }
        return false;
    }

    // ── MAP ───────────────────────────────────────────────────────────────────

    private Librarian map(ResultSet rs) throws SQLException {
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