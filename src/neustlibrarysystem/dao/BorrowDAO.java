package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.BorrowedRecord;
import neustlibrarysystem.model.BorrowRequest;

import java.sql.*;
import java.time.LocalDate;
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

    // ── BORROW REQUEST METHODS ────────────────────────────────────────────────

    /**
     * Returns all borrow requests with member and book info joined.
     * Adjust table/column names to match your actual DB schema.
     */
    public List<BorrowRequest> getAllBorrowRequests() {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT br.RequestID, br.MemberID, "
                   + "m.FirstName + ' ' + m.LastName AS MemberName, "
                   + "br.BookID, b.Title AS BookTitle, b.ISBN, "
                   + "br.RequestDate, br.PreferredPickup, "
                   + "br.Status, br.Notes, "
                   + "br.ProcessedBy, br.ProcessedDate, br.DueDate "
                   + "FROM BorrowRequest br "
                   + "JOIN Member m ON br.MemberID = m.MemberID "
                   + "JOIN Book   b ON br.BookID   = b.BookID "
                   + "ORDER BY br.RequestDate DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId (rs.getInt   ("RequestID"));
                req.setMemberId  (rs.getInt   ("MemberID"));
                req.setMemberName(rs.getString("MemberName"));
                req.setBookId    (rs.getInt   ("BookID"));
                req.setBookTitle (rs.getString("BookTitle"));
                req.setIsbn      (rs.getString("ISBN"));
                req.setStatus    (rs.getString("Status"));
                req.setNotes     (rs.getString("Notes"));

                Date reqDate = rs.getDate("RequestDate");
                if (reqDate != null) req.setRequestDate(reqDate.toLocalDate());

                Date pickup = rs.getDate("PreferredPickup");
                if (pickup != null) req.setPreferredPickup(pickup.toLocalDate());

                Date procDate = rs.getDate("ProcessedDate");
                if (procDate != null) req.setProcessedDate(procDate.toLocalDate());

                Date due = rs.getDate("DueDate");
                if (due != null) req.setDueDate(due.toLocalDate());

                int procBy = rs.getInt("ProcessedBy");
                if (!rs.wasNull()) req.setProcessedByLibrarianId(procBy);

                list.add(req);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Accepts a borrow request:
     * 1. Updates BorrowRequest status to ACCEPTED
     * 2. Creates a BorrowedRecord entry
     * 3. Decrements available copies of the book
     * All inside a transaction.
     */
    public void acceptBorrowRequest(int requestId, int librarianId, LocalDate dueDate) {
        String updateRequest = "UPDATE BorrowRequest "
                + "SET Status = 'ACCEPTED', ProcessedBy = ?, "
                + "ProcessedDate = GETDATE(), DueDate = ? "
                + "WHERE RequestID = ?";

        // Insert actual borrow record — adjust column names to match BorrowedRecord table
        String insertBorrow = "INSERT INTO BorrowedRecord "
                + "(MemberID, BookID, LibrarianID, BorrowDate, DueDate, Status, FineAmount, FinePaid) "
                + "SELECT MemberID, BookID, ?, GETDATE(), ?, 'Borrowed', 0, 0 "
                + "FROM BorrowRequest WHERE RequestID = ?";

        // Decrement available copies
        String decrementBook = "UPDATE Book "
                + "SET AvailableCopies = AvailableCopies - 1 "
                + "WHERE BookID = (SELECT BookID FROM BorrowRequest WHERE RequestID = ?) "
                + "AND AvailableCopies > 0";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(updateRequest)) {
                    ps.setInt (1, librarianId);
                    ps.setDate(2, Date.valueOf(dueDate));
                    ps.setInt (3, requestId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(insertBorrow)) {
                    ps.setInt (1, librarianId);
                    ps.setDate(2, Date.valueOf(dueDate));
                    ps.setInt (3, requestId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(decrementBook)) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Rejects a borrow request — simply updates status to REJECTED.
     */
    public void rejectBorrowRequest(int requestId, int librarianId) {
        String sql = "UPDATE BorrowRequest "
                + "SET Status = 'REJECTED', ProcessedBy = ?, ProcessedDate = GETDATE() "
                + "WHERE RequestID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, librarianId);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
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