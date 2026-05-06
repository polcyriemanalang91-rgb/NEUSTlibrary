package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.BookConditionalLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookConditionLogDAO {

    public boolean addLog(BookConditionalLog log) throws SQLException {
        String sql = "INSERT INTO BookConditionalLog (BookID, LibrarianID, BorrowID, Condition, Remarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getBookID());
            ps.setInt(2, log.getLibrarianID());
            if (log.getBorrowID() != null) ps.setInt(3, log.getBorrowID());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, log.getCondition());
            ps.setString(5, log.getRemarks());
            return ps.executeUpdate() > 0;
        }
    }

    public List<BookConditionalLog> getLogsByBookID(int bookID) throws SQLException {
        List<BookConditionalLog> list = new ArrayList<>();
        String sql = "SELECT bcl.*, b.Title AS BookTitle, b.ISBN, " +
                     "l.FirstName + ' ' + l.LastName AS LibrarianName " +
                     "FROM BookConditionalLog bcl " +
                     "JOIN Book b ON bcl.BookID = b.BookID " +
                     "JOIN Librarian l ON bcl.LibrarianID = l.LibrarianID " +
                     "WHERE bcl.BookID = ? ORDER BY bcl.LogDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<BookConditionalLog> getAllLogs() throws SQLException {
        List<BookConditionalLog> list = new ArrayList<>();
        String sql = "SELECT bcl.*, b.Title AS BookTitle, b.ISBN, " +
                     "l.FirstName + ' ' + l.LastName AS LibrarianName " +
                     "FROM BookConditionalLog bcl " +
                     "JOIN Book b ON bcl.BookID = b.BookID " +
                     "JOIN Librarian l ON bcl.LibrarianID = l.LibrarianID " +
                     "ORDER BY bcl.LogDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<BookConditionalLog> getLogsByBorrowID(int borrowID) throws SQLException {
        List<BookConditionalLog> list = new ArrayList<>();
        String sql = "SELECT bcl.*, b.Title AS BookTitle, b.ISBN, " +
                     "l.FirstName + ' ' + l.LastName AS LibrarianName " +
                     "FROM BookConditionalLog bcl " +
                     "JOIN Book b ON bcl.BookID = b.BookID " +
                     "JOIN Librarian l ON bcl.LibrarianID = l.LibrarianID " +
                     "WHERE bcl.BorrowID = ? ORDER BY bcl.LogDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private BookConditionalLog mapRow(ResultSet rs) throws SQLException {
        BookConditionalLog log = new BookConditionalLog();
        log.setLogID(rs.getInt("LogID"));
        log.setBookID(rs.getInt("BookID"));
        log.setLibrarianID(rs.getInt("LibrarianID"));
        int borrowID = rs.getInt("BorrowID");
        if (!rs.wasNull()) log.setBorrowID(borrowID);
        log.setCondition(rs.getString("Condition"));
        log.setRemarks(rs.getString("Remarks"));
        Timestamp ts = rs.getTimestamp("LogDate");
        if (ts != null) log.setLogDate(ts.toLocalDateTime());
        try {
            log.setBookTitle(rs.getString("BookTitle"));
            log.setIsbn(rs.getString("ISBN"));
            log.setLibrarianName(rs.getString("LibrarianName"));
        } catch (Exception ignored) {}
        return log;
    }
}