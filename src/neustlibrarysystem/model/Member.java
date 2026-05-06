package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Member {

    private int           memberID;
    private String        studentID;
    private String        firstName;
    private String        lastName;
    private String        email;
    private String        passwordHash;
    private String        courseProgram;
    private String        yearLevel;
    private String        contactNumber;
    private String        address;
    private String        profileImagePath;
    private boolean       isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Member() {}

    public int           getMemberID()                              { return memberID; }
    public void          setMemberID(int memberID)                  { this.memberID = memberID; }
    public String        getStudentID()                             { return studentID; }
    public void          setStudentID(String studentID)             { this.studentID = studentID; }
    public String        getFirstName()                             { return firstName; }
    public void          setFirstName(String firstName)             { this.firstName = firstName; }
    public String        getLastName()                              { return lastName; }
    public void          setLastName(String lastName)               { this.lastName = lastName; }
    public String        getFullName()                              { return firstName + " " + lastName; }
    public String        getEmail()                                 { return email; }
    public void          setEmail(String email)                     { this.email = email; }
    public String        getPasswordHash()                          { return passwordHash; }
    public void          setPasswordHash(String passwordHash)       { this.passwordHash = passwordHash; }
    public String        getCourseProgram()                         { return courseProgram; }
    public void          setCourseProgram(String courseProgram)     { this.courseProgram = courseProgram; }
    public String        getYearLevel()                             { return yearLevel; }
    public void          setYearLevel(String yearLevel)             { this.yearLevel = yearLevel; }
    public String        getContactNumber()                         { return contactNumber; }
    public void          setContactNumber(String contactNumber)     { this.contactNumber = contactNumber; }
    public String        getAddress()                               { return address; }
    public void          setAddress(String address)                 { this.address = address; }
    public String        getProfileImagePath()                      { return profileImagePath; }
    public void          setProfileImagePath(String path)           { this.profileImagePath = path; }
    public boolean       isActive()                                 { return isActive; }
    public void          setActive(boolean isActive)                { this.isActive = isActive; }
    public LocalDateTime getCreatedAt()                             { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt()                             { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return getFullName() + " [" + studentID + "]"; }
}