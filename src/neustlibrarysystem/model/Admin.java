package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Admin {
    private int adminID;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Admin() {}

    public Admin(int adminID, String username, String fullName, String email) {
        this.adminID   = adminID;
        this.username  = username;
        this.fullName  = fullName;
        this.email     = email;
    }

    public int getAdminID()                          { return adminID; }
    public void setAdminID(int adminID)              { this.adminID = adminID; }
    public int getAdminId()                          { return adminID; }       // ← backward compat
    public void setAdminId(int adminId)              { this.adminID = adminId; } // ← backward compat
    public String getUsername()                      { return username; }
    public void setUsername(String username)         { this.username = username; }
    public String getPasswordHash()                  { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName()                      { return fullName; }
    public void setFullName(String fullName)         { this.fullName = fullName; }
    public String getEmail()                         { return email; }
    public void setEmail(String email)               { this.email = email; }
    public boolean isActive()                        { return isActive; }
    public void setActive(boolean isActive)          { this.isActive = isActive; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}