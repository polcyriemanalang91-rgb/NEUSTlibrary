package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Librarian {

    private int           librarianID;
    private String        employeeID;
    private String        firstName;
    private String        lastName;
    private String        email;
    private String        passwordHash;
    private String        contactNumber;
    private boolean       isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Librarian() {}

    public int           getLibrarianID()                           { return librarianID; }
    public void          setLibrarianID(int librarianID)            { this.librarianID = librarianID; }
    public String        getEmployeeID()                            { return employeeID; }
    public void          setEmployeeID(String employeeID)           { this.employeeID = employeeID; }
    public String        getFirstName()                             { return firstName; }
    public void          setFirstName(String firstName)             { this.firstName = firstName; }
    public String        getLastName()                              { return lastName; }
    public void          setLastName(String lastName)               { this.lastName = lastName; }
    public String        getFullName()                              { return firstName + " " + lastName; }
    public String        getEmail()                                 { return email; }
    public void          setEmail(String email)                     { this.email = email; }
    public String        getPasswordHash()                          { return passwordHash; }
    public void          setPasswordHash(String passwordHash)       { this.passwordHash = passwordHash; }
    public String        getContactNumber()                         { return contactNumber; }
    public void          setContactNumber(String contactNumber)     { this.contactNumber = contactNumber; }
    public boolean       isActive()                                 { return isActive; }
    public void          setActive(boolean isActive)                { this.isActive = isActive; }
    public LocalDateTime getCreatedAt()                             { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt()                             { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return getFullName() + " [" + employeeID + "]"; }
}