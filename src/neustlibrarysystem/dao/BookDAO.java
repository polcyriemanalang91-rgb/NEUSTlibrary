package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Author;
import neustlibrarysystem.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookDAO {

    private static final Logger LOGGER = Logger.getLogger(BookDAO.class.getName());

    public boolean addBook(Book book) {
        String sql = "INSERT INTO Book (ISBN, Title, CategoryID, PublisherID, PublicationYear, " +
                     "Edition, TotalCopies, AvailableCopies, ShelfLocation, Description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setInt   (3, book.getCategoryID());
            ps.setInt   (4, book.getPublisherID());
            ps.setInt   (5, book.getPublicationYear());
            ps.setString(6, book.getEdition());
            ps.setInt   (7, book.getTotalCopies());
            ps.setInt   (8, book.getTotalCopies());
            ps.setString(9, book.getShelfLocation());
            ps.setString(10, book.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) book.setBookID(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding book.", e);
        }
        return false;
    }

    public boolean linkAuthor(int bookID, int authorID) {
        String sql = "INSERT INTO BookAuthor (BookID, AuthorID) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookID); ps.setInt(2, authorID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error linking author.", e);
        }
        return false;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM vw_BookInventory WHERE IsActive = 1 ORDER BY Title";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) books.add(mapBookFromViewRS(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching books.", e);
        }
        return books;
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM vw_BookInventory WHERE IsActive = 1 AND " +
                     "(Title LIKE ? OR ISBN LIKE ? OR Authors LIKE ? OR CategoryName LIKE ?) ORDER BY Title";
        String kw = "%" + keyword + "%";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw);
            ps.setString(3, kw); ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(mapBookFromViewRS(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching books.", e);
        }
        return books;
    }

    public Book getBookByID(int bookID) {
        String sql = "SELECT * FROM vw_BookInventory WHERE BookID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBookFromViewRS(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching book.", e);
        }
        return null;
    }

    public boolean isISBNExists(String isbn) {
        String sql = "SELECT COUNT(*) FROM Book WHERE ISBN = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking ISBN.", e);
        }
        return false;
    }

    public boolean updateBook(Book book) {
        String sql = "UPDATE Book SET Title=?, CategoryID=?, PublisherID=?, " +
                     "PublicationYear=?, Edition=?, TotalCopies=?, " +
                     "ShelfLocation=?, Description=?, UpdatedAt=GETDATE() WHERE BookID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setInt   (2, book.getCategoryID());
            ps.setInt   (3, book.getPublisherID());
            ps.setInt   (4, book.getPublicationYear());
            ps.setString(5, book.getEdition());
            ps.setInt   (6, book.getTotalCopies());
            ps.setString(7, book.getShelfLocation());
            ps.setString(8, book.getDescription());
            ps.setInt   (9, book.getBookID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book.", e);
        }
        return false;
    }

    public boolean deactivateBook(int bookID) {
        String sql = "UPDATE Book SET IsActive=0, UpdatedAt=GETDATE() WHERE BookID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating book.", e);
        }
        return false;
    }

    public List<Object[]> getInventoryReport() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT BookID, ISBN, Title, CategoryName, PublisherName, " +
                     "TotalCopies, AvailableCopies, IsActive FROM vw_BookInventory ORDER BY Title";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt    ("BookID"),
                    rs.getString ("ISBN"),
                    rs.getString ("Title"),
                    rs.getString ("CategoryName"),
                    rs.getString ("PublisherName"),
                    rs.getInt    ("TotalCopies"),
                    rs.getInt    ("AvailableCopies"),
                    rs.getBoolean("IsActive") ? "Active" : "Inactive"
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching inventory report.", e);
        }
        return list;
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────

    private Book mapBookFromViewRS(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookID         (rs.getInt    ("BookID"));
        book.setIsbn           (rs.getString ("ISBN"));
        book.setTitle          (rs.getString ("Title"));
        book.setEdition        (rs.getString ("Edition"));
        book.setPublicationYear(rs.getInt    ("PublicationYear"));
        book.setTotalCopies    (rs.getInt    ("TotalCopies"));
        book.setAvailableCopies(rs.getInt    ("AvailableCopies"));
        book.setShelfLocation  (rs.getString ("ShelfLocation"));
        book.setActive         (rs.getBoolean("IsActive"));
        book.setCategoryName   (rs.getString ("CategoryName"));
        book.setPublisherName  (rs.getString ("PublisherName"));

        // ← FIXED: removed setAuthorName(), properly add authors to list
        String authorsStr = rs.getString("Authors");
        if (authorsStr != null && !authorsStr.isEmpty()) {
            List<Author> authors = new ArrayList<>();
            for (String name : authorsStr.split(",")) {
                String[] parts = name.trim().split(" ", 2);
                Author a = new Author();
                a.setFirstName(parts.length > 0 ? parts[0] : "");
                a.setLastName (parts.length > 1 ? parts[1] : "");
                authors.add(a); // ← FIXED: was missing add()
            }
            book.setAuthors(authors);
        }
        return book;
    }
}