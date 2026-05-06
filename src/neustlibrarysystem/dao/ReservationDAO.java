package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationDAO {

    private static final Logger LOGGER = Logger.getLogger(ReservationDAO.class.getName());

    public boolean createReservation(int bookID, int memberID) {
        String check = "SELECT COUNT(*) FROM Reservation WHERE BookID=? AND MemberID=? AND Status IN ('Pending','Confirmed')";
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(check)) {
                ps.setInt(1, bookID); ps.setInt(2, memberID);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
            int expiryDays = 3;
            String settingSQL = "SELECT SettingValue FROM SystemSettings WHERE SettingKey='reservation_expiry_days'";
            try (PreparedStatement ps2 = con.prepareStatement(settingSQL);
                 ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) expiryDays = Integer.parseInt(rs2.getString("SettingValue"));
            }
            String insert = "INSERT INTO Reservation (BookID, MemberID, ExpiryDate) VALUES (?, ?, DATEADD(DAY,?,GETDATE()))";
            try (PreparedStatement ps3 = con.prepareStatement(insert)) {
                ps3.setInt(1, bookID); ps3.setInt(2, memberID); ps3.setInt(3, expiryDays);
                return ps3.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation.", e);
        }
        return false;
    }

    public List<Reservation> getPendingReservations() {
        return getByStatus("Pending");
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.Title AS BookTitle, m.FirstName+' '+m.LastName AS MemberName, m.StudentID " +
                     "FROM Reservation r " +
                     "JOIN Book b ON r.BookID=b.BookID " +
                     "JOIN Member m ON r.MemberID=m.MemberID " +
                     "ORDER BY r.ReservedDate DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching reservations.", e);
        }
        return list;
    }

    public List<Reservation> getMemberReservations(int memberID) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.Title AS BookTitle, m.FirstName+' '+m.LastName AS MemberName, m.StudentID " +
                     "FROM Reservation r " +
                     "JOIN Book b ON r.BookID=b.BookID " +
                     "JOIN Member m ON r.MemberID=m.MemberID " +
                     "WHERE r.MemberID=? ORDER BY r.ReservedDate DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching member reservations.", e);
        }
        return list;
    }

    private List<Reservation> getByStatus(String status) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.Title AS BookTitle, m.FirstName+' '+m.LastName AS MemberName, m.StudentID " +
                     "FROM Reservation r " +
                     "JOIN Book b ON r.BookID=b.BookID " +
                     "JOIN Member m ON r.MemberID=m.MemberID " +
                     "WHERE r.Status=? ORDER BY r.ReservedDate ASC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching reservations by status.", e);
        }
        return list;
    }

    public boolean updateStatus(int reservationID, String status, int librarianID) {
        String sql = "UPDATE Reservation SET Status=?, ProcessedBy=?, ProcessedDate=GETDATE() WHERE ReservationID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, librarianID);
            ps.setInt   (3, reservationID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation status.", e);
        }
        return false;
    }

    public boolean cancelReservation(int reservationID) {
        String sql = "UPDATE Reservation SET Status='Cancelled' WHERE ReservationID=? AND Status IN ('Pending','Confirmed')";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling reservation.", e);
        }
        return false;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationID(rs.getInt("ReservationID"));
        r.setBookID       (rs.getInt("BookID"));
        r.setMemberID     (rs.getInt("MemberID"));
        r.setStatus       (rs.getString("Status")); // ← String na, hindi enum
        r.setNotes        (rs.getString("Notes"));
        Timestamp rd = rs.getTimestamp("ReservedDate");
        if (rd != null) r.setReservedDate(rd.toLocalDateTime());
        Timestamp ed = rs.getTimestamp("ExpiryDate");
        if (ed != null) r.setExpiryDate(ed.toLocalDateTime());
        Timestamp pd = rs.getTimestamp("ProcessedDate");
        if (pd != null) r.setProcessedDate(pd.toLocalDateTime());
        try { r.setBookTitle  (rs.getString("BookTitle"));  } catch (SQLException ignored) {}
        try { r.setMemberName (rs.getString("MemberName")); } catch (SQLException ignored) {}
        try { r.setStudentID  (rs.getString("StudentID"));  } catch (SQLException ignored) {}
        return r;
    }
}