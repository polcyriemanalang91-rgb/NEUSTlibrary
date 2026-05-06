package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.BorrowedRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    public int processBorrow(int bookID, int memberID, int librarianID, Integer reservationID) {
        String sql = "{call sp_ProcessBorrow(?,?,?,?)}";
        try (Connection con = DatabaseConnection.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, bookID);
            cs.setInt(2, memberID);
            cs.setInt(3, librarianID);
            if (reservationID != null) cs.setInt(4, reservationID);
            else                       cs.setNull(4, Types.INTEGER);
            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                if ("SUCCESS".equals(rs.getString("Result")))
                    return rs.getInt("BorrowID");
                else
                    System.err.println("sp_ProcessBorrow: " + rs.getString("ErrorMessage"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public double processReturn(int borrowID, int librarianID, String condition, String remarks) {
        String sql = "{call sp_ProcessReturn(?,?,?,?)}";
        try (Connection con = DatabaseConnection.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt   (1, borrowID);
            cs.setInt   (2, librarianID);
            cs.setString(3, condition);
            cs.setString(4, remarks);
            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                if ("SUCCESS".equals(rs.getString("Result")))
                    return rs.getDouble("FineAmount");
                else
                    System.err.println("sp_ProcessReturn: " + rs.getString("ErrorMessage"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<BorrowedRecord> getActiveBorrows() {
        List<BorrowedRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_ActiveBorrows ORDER BY DueDate";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapFromView(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<BorrowedRecord> searchActiveBorrows(String keyword) {
        List<BorrowedRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_ActiveBorrows WHERE "
                   + "MemberName LIKE ? OR StudentID LIKE ? OR BookTitle LIKE ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFromView(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<BorrowedRecord> getMemberHistory(int memberID) {
        List<BorrowedRecord> list = new ArrayList<>();
        String sql = "SELECT br.*, "
                   + "m.FirstName+' '+m.LastName AS MemberName, m.StudentID, "
                   + "b.Title AS BookTitle, b.ISBN, "
                   + "l.FirstName+' '+l.LastName AS LibrarianName "
                   + "FROM BorrowedRecord br "
                   + "JOIN Member m    ON br.MemberID    = m.MemberID "
                   + "JOIN Book b      ON br.BookID      = b.BookID "
                   + "JOIN Librarian l ON br.LibrarianID = l.LibrarianID "
                   + "WHERE br.MemberID = ? ORDER BY br.BorrowDate DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean markFinePaid(int borrowID) {
        String sql = "UPDATE BorrowedRecord SET FinePaid=1 WHERE BorrowID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, borrowID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── REPORT METHODS ────────────────────────────────────────────────────────

    public List<Object[]> getBorrowSummaryReport(String status) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT br.BorrowID, "
                   + "m.FirstName+' '+m.LastName AS MemberName, m.StudentID, "
                   + "b.Title AS BookTitle, "
                   + "br.BorrowDate, br.DueDate, br.Status, br.FineAmount "
                   + "FROM BorrowedRecord br "
                   + "JOIN Member m ON br.MemberID = m.MemberID "
                   + "JOIN Book b   ON br.BookID   = b.BookID ";
        if (!"All".equals(status)) sql += "WHERE br.Status = ? ";
        sql += "ORDER BY br.BorrowDate DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (!"All".equals(status)) ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt       ("BorrowID"),
                    rs.getString    ("MemberName"),
                    rs.getString    ("StudentID"),
                    rs.getString    ("BookTitle"),
                    rs.getTimestamp ("BorrowDate"),
                    rs.getTimestamp ("DueDate"),
                    rs.getString    ("Status"),
                    rs.getBigDecimal("FineAmount")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getOverdueReport() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT br.BorrowID, "
                   + "m.FirstName+' '+m.LastName AS MemberName, m.StudentID, "
                   + "b.Title AS BookTitle, "
                   + "br.BorrowDate, br.DueDate, "
                   + "DATEDIFF(DAY, br.DueDate, GETDATE()) AS DaysOverdue, "
                   + "br.FineAmount "
                   + "FROM BorrowedRecord br "
                   + "JOIN Member m ON br.MemberID = m.MemberID "
                   + "JOIN Book b   ON br.BookID   = b.BookID "
                   + "WHERE br.Status = 'Overdue' "
                   + "ORDER BY br.DueDate ASC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt       ("BorrowID"),
                    rs.getString    ("MemberName"),
                    rs.getString    ("StudentID"),
                    rs.getString    ("BookTitle"),
                    rs.getTimestamp ("BorrowDate"),
                    rs.getTimestamp ("DueDate"),
                    rs.getInt       ("DaysOverdue"),
                    rs.getBigDecimal("FineAmount")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────

    private BorrowedRecord mapFromView(ResultSet rs) throws SQLException {
        BorrowedRecord r = new BorrowedRecord();
        r.setBorrowID    (rs.getInt        ("BorrowID"));
        r.setMemberID    (rs.getInt        ("MemberID"));
        r.setBookID      (rs.getInt        ("BookID"));
        r.setStatus      (rs.getString     ("Status"));
        r.setFineAmount  (rs.getBigDecimal ("FineAmount"));
        r.setMemberName  (rs.getString     ("MemberName"));
        r.setStudentID   (rs.getString     ("StudentID"));
        r.setBookTitle   (rs.getString     ("BookTitle"));
        r.setLibrarianName(rs.getString    ("LibrarianName"));
        Timestamp borrow = rs.getTimestamp("BorrowDate");
        Timestamp due    = rs.getTimestamp("DueDate");
        if (borrow != null) r.setBorrowDate(borrow.toLocalDateTime());
        if (due    != null) r.setDueDate   (due.toLocalDateTime());
        return r;
    }

    private BorrowedRecord mapFull(ResultSet rs) throws SQLException {
        BorrowedRecord r = new BorrowedRecord();
        r.setBorrowID    (rs.getInt        ("BorrowID"));
        r.setMemberID    (rs.getInt        ("MemberID"));
        r.setBookID      (rs.getInt        ("BookID"));
        r.setLibrarianID (rs.getInt        ("LibrarianID"));
        r.setStatus      (rs.getString     ("Status"));
        r.setFineAmount  (rs.getBigDecimal ("FineAmount"));
        r.setFinePaid    (rs.getBoolean    ("FinePaid"));
        r.setMemberName  (rs.getString     ("MemberName"));
        r.setStudentID   (rs.getString     ("StudentID"));
        r.setBookTitle   (rs.getString     ("BookTitle"));
        r.setIsbn        (rs.getString     ("ISBN"));
        r.setLibrarianName(rs.getString    ("LibrarianName"));
        Timestamp borrow = rs.getTimestamp("BorrowDate");
        Timestamp due    = rs.getTimestamp("DueDate");
        Timestamp ret    = rs.getTimestamp("ReturnDate");
        if (borrow != null) r.setBorrowDate (borrow.toLocalDateTime());
        if (due    != null) r.setDueDate    (due.toLocalDateTime());
        if (ret    != null) r.setReturnDate (ret.toLocalDateTime());
        return r;
    }
}