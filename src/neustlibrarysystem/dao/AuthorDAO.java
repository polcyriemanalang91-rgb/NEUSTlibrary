package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAO {

    public List<Author> getAllAuthors() throws SQLException {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT * FROM Author ORDER BY LastName, FirstName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Author> getActiveAuthors() throws SQLException {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT * FROM Author WHERE IsActive = 1 ORDER BY LastName, FirstName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Author getAuthorByID(int authorID) throws SQLException {
        String sql = "SELECT * FROM Author WHERE AuthorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Author> getAuthorsByBookID(int bookID) throws SQLException {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT a.* FROM Author a " +
                     "JOIN BookAuthor ba ON a.AuthorID = ba.AuthorID " +
                     "WHERE ba.BookID = ? ORDER BY a.LastName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean addAuthor(Author author) throws SQLException {
        String sql = "INSERT INTO Author (FirstName, LastName, Biography, IsActive) VALUES (?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, author.getFirstName());
            ps.setString(2, author.getLastName());
            ps.setString(3, author.getBiography());
            return ps.executeUpdate() > 0;
        }
    }

    public int addAuthorReturnID(Author author) throws SQLException {
        String sql = "INSERT INTO Author (FirstName, LastName, Biography, IsActive) VALUES (?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, author.getFirstName());
            ps.setString(2, author.getLastName());
            ps.setString(3, author.getBiography());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateAuthor(Author author) throws SQLException {
        String sql = "UPDATE Author SET FirstName = ?, LastName = ?, Biography = ?, IsActive = ? WHERE AuthorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, author.getFirstName());
            ps.setString(2, author.getLastName());
            ps.setString(3, author.getBiography());
            ps.setBoolean(4, author.isActive());
            ps.setInt(5, author.getAuthorID());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAuthor(int authorID) throws SQLException {
        String sql = "UPDATE Author SET IsActive = 0 WHERE AuthorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean authorExists(String firstName, String lastName, int excludeID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Author WHERE FirstName = ? AND LastName = ? AND AuthorID <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setInt(3, excludeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Add/remove book-author relationships
    public boolean linkAuthorToBook(int bookID, int authorID) throws SQLException {
        String sql = "IF NOT EXISTS (SELECT 1 FROM BookAuthor WHERE BookID=? AND AuthorID=?) " +
                     "INSERT INTO BookAuthor (BookID, AuthorID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookID); ps.setInt(2, authorID);
            ps.setInt(3, bookID); ps.setInt(4, authorID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean unlinkAuthorFromBook(int bookID, int authorID) throws SQLException {
        String sql = "DELETE FROM BookAuthor WHERE BookID = ? AND AuthorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookID);
            ps.setInt(2, authorID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeAllAuthorsFromBook(int bookID) throws SQLException {
        String sql = "DELETE FROM BookAuthor WHERE BookID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookID);
            ps.executeUpdate();
            return true;
        }
    }

    private Author mapRow(ResultSet rs) throws SQLException {
        Author a = new Author();
        a.setAuthorID(rs.getInt("AuthorID"));
        a.setFirstName(rs.getString("FirstName"));
        a.setLastName(rs.getString("LastName"));
        a.setBiography(rs.getString("Biography"));
        a.setActive(rs.getBoolean("IsActive"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }
}