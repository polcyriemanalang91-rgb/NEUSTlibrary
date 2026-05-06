package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Publisher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublisherDAO {

    public List<Publisher> getAllPublishers() throws SQLException {
        List<Publisher> list = new ArrayList<>();
        String sql = "SELECT * FROM Publisher ORDER BY PublisherName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Publisher> getActivePublishers() throws SQLException {
        List<Publisher> list = new ArrayList<>();
        String sql = "SELECT * FROM Publisher WHERE IsActive = 1 ORDER BY PublisherName";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Publisher getPublisherByID(int publisherID) throws SQLException {
        String sql = "SELECT * FROM Publisher WHERE PublisherID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, publisherID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean addPublisher(Publisher publisher) throws SQLException {
        String sql = "INSERT INTO Publisher (PublisherName, Address, ContactEmail, ContactPhone, IsActive) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, publisher.getPublisherName());
            ps.setString(2, publisher.getAddress());
            ps.setString(3, publisher.getContactEmail());
            ps.setString(4, publisher.getContactPhone());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePublisher(Publisher publisher) throws SQLException {
        String sql = "UPDATE Publisher SET PublisherName = ?, Address = ?, ContactEmail = ?, ContactPhone = ?, IsActive = ? WHERE PublisherID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, publisher.getPublisherName());
            ps.setString(2, publisher.getAddress());
            ps.setString(3, publisher.getContactEmail());
            ps.setString(4, publisher.getContactPhone());
            ps.setBoolean(5, publisher.isActive());
            ps.setInt(6, publisher.getPublisherID());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePublisher(int publisherID) throws SQLException {
        String sql = "UPDATE Publisher SET IsActive = 0 WHERE PublisherID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, publisherID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean publisherNameExists(String name, int excludeID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Publisher WHERE PublisherName = ? AND PublisherID <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private Publisher mapRow(ResultSet rs) throws SQLException {
        Publisher p = new Publisher();
        p.setPublisherID(rs.getInt("PublisherID"));
        p.setPublisherName(rs.getString("PublisherName"));
        p.setAddress(rs.getString("Address"));
        p.setContactEmail(rs.getString("ContactEmail"));
        p.setContactPhone(rs.getString("ContactPhone"));
        p.setActive(rs.getBoolean("IsActive"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}