package neustlibrarysystem.dao;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.model.Member;
import neustlibrarysystem.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemberDAO {

    private static final Logger LOGGER = Logger.getLogger(MemberDAO.class.getName());

    public Member login(String email, String plainPassword) {
        String sql = "SELECT * FROM Member WHERE Email = ? AND IsActive = 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (PasswordUtil.verifyPassword(plainPassword, rs.getString("PasswordHash")))
                        return mapMember(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Member login error.", e);
        }
        return null;
    }

    public boolean register(Member member, String plainPassword) {
        String sql = "INSERT INTO Member (StudentID, FirstName, LastName, Email, PasswordHash, " +
                     "CourseProgram, YearLevel, ContactNumber, Address) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, member.getStudentID());
            ps.setString(2, member.getFirstName());
            ps.setString(3, member.getLastName());
            ps.setString(4, member.getEmail());
            ps.setString(5, PasswordUtil.hashPassword(plainPassword));
            ps.setString(6, member.getCourseProgram());
            ps.setString(7, member.getYearLevel());
            ps.setString(8, member.getContactNumber());
            ps.setString(9, member.getAddress());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering member.", e);
        }
        return false;
    }

    public List<Member> getAllMembers() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM Member ORDER BY LastName, FirstName";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMember(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching members.", e);
        }
        return list;
    }

    public List<Member> searchMembers(String keyword) {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM Member WHERE "
                   + "StudentID LIKE ? OR FirstName LIKE ? OR LastName LIKE ? OR Email LIKE ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw);
            ps.setString(3, kw); ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMember(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching members.", e);
        }
        return list;
    }

    public Member getMemberByID(int memberID) {
        String sql = "SELECT * FROM Member WHERE MemberID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMember(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching member.", e);
        }
        return null;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Member WHERE Email = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email.", e);
        }
        return false;
    }

    public boolean isStudentIDExists(String studentID) {
        String sql = "SELECT COUNT(*) FROM Member WHERE StudentID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentID.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking student ID.", e);
        }
        return false;
    }

    public boolean updateProfile(Member member) {
        String sql = "UPDATE Member SET FirstName=?, LastName=?, CourseProgram=?, " +
                     "YearLevel=?, ContactNumber=?, Address=?, UpdatedAt=GETDATE() WHERE MemberID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, member.getFirstName());
            ps.setString(2, member.getLastName());
            ps.setString(3, member.getCourseProgram());
            ps.setString(4, member.getYearLevel());
            ps.setString(5, member.getContactNumber());
            ps.setString(6, member.getAddress());
            ps.setInt   (7, member.getMemberID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating profile.", e);
        }
        return false;
    }

    public boolean changePassword(int memberID, String newPlainPassword) {
        String sql = "UPDATE Member SET PasswordHash=?, UpdatedAt=GETDATE() WHERE MemberID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPlainPassword));
            ps.setInt   (2, memberID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error changing password.", e);
        }
        return false;
    }

    public boolean deactivateMember(int memberID) {
        String sql = "UPDATE Member SET IsActive=0, UpdatedAt=GETDATE() WHERE MemberID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating member.", e);
        }
        return false;
    }

    public boolean setMemberActive(int memberID, boolean active) {
        String sql = "UPDATE Member SET IsActive=?, UpdatedAt=GETDATE() WHERE MemberID=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt    (2, memberID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling member active.", e);
        }
        return false;
    }

    public List<Object[]> getMemberActivityReport() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT m.MemberID, m.StudentID, "
                   + "m.FirstName+' '+m.LastName AS FullName, m.Email, "
                   + "COUNT(br.BorrowID) AS TotalBorrows, "
                   + "SUM(CASE WHEN br.Status IN ('Borrowed','Overdue') THEN 1 ELSE 0 END) AS ActiveBorrows, "
                   + "SUM(CASE WHEN br.Status = 'Returned' THEN 1 ELSE 0 END) AS Returned, "
                   + "SUM(CASE WHEN br.Status = 'Overdue'  THEN 1 ELSE 0 END) AS Overdue "
                   + "FROM Member m "
                   + "LEFT JOIN BorrowedRecord br ON m.MemberID = br.MemberID "
                   + "GROUP BY m.MemberID, m.StudentID, m.FirstName, m.LastName, m.Email "
                   + "ORDER BY TotalBorrows DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt   ("MemberID"),
                    rs.getString("StudentID"),
                    rs.getString("FullName"),
                    rs.getString("Email"),
                    rs.getInt   ("TotalBorrows"),
                    rs.getInt   ("ActiveBorrows"),
                    rs.getInt   ("Returned"),
                    rs.getInt   ("Overdue")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching member activity report.", e);
        }
        return list;
    }

    // ── ADDED: Count all active members for dashboard ─────────────────────────
    public int getActiveMemberCount() {
        String sql = "SELECT COUNT(*) FROM Member WHERE IsActive = 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting members.", e);
        }
        return 0;
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────
    private Member mapMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberID     (rs.getInt    ("MemberID"));
        m.setStudentID    (rs.getString ("StudentID"));
        m.setFirstName    (rs.getString ("FirstName"));
        m.setLastName     (rs.getString ("LastName"));
        m.setEmail        (rs.getString ("Email"));
        m.setPasswordHash (rs.getString ("PasswordHash"));
        m.setCourseProgram(rs.getString ("CourseProgram"));
        m.setYearLevel    (rs.getString ("YearLevel"));
        m.setContactNumber(rs.getString ("ContactNumber"));
        m.setAddress      (rs.getString ("Address"));
        m.setActive       (rs.getBoolean("IsActive"));
        Timestamp ca = rs.getTimestamp("CreatedAt");
        if (ca != null) m.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("UpdatedAt");
        if (ua != null) m.setUpdatedAt(ua.toLocalDateTime());
        return m;
    }
}