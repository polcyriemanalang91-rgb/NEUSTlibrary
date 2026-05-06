package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Author {

    private int           authorID;
    private String        firstName;
    private String        lastName;
    private String        biography;
    private boolean       isActive;
    private LocalDateTime createdAt;

    public Author() {}

    public Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.isActive  = true;
    }

    public Author(String firstName, String lastName, String biography) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.biography = biography;
        this.isActive  = true;
    }

    public Author(int authorID, String firstName, String lastName,
                  String biography, boolean isActive, LocalDateTime createdAt) {
        this.authorID  = authorID;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.biography = biography;
        this.isActive  = isActive;
        this.createdAt = createdAt;
    }

    public int           getAuthorID()                          { return authorID; }
    public void          setAuthorID(int authorID)              { this.authorID = authorID; }
    public String        getFirstName()                         { return firstName; }
    public void          setFirstName(String firstName)         { this.firstName = firstName; }
    public String        getLastName()                          { return lastName; }
    public void          setLastName(String lastName)           { this.lastName = lastName; }
    public String        getFullName()                          { return firstName + " " + lastName; }
    public String        getBiography()                         { return biography; }
    public void          setBiography(String biography)         { this.biography = biography; }
    public boolean       isActive()                             { return isActive; }
    public void          setActive(boolean active)              { this.isActive = active; }
    public LocalDateTime getCreatedAt()                         { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }

    @Override
    public String toString() { return getFullName(); }
}