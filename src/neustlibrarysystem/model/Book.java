package neustlibrarysystem.model;

import java.time.LocalDateTime;
import java.util.List;

public class Book {

    private int           bookID;
    private String        isbn;
    private String        title;
    private int           categoryID;
    private String        categoryName;
    private int           publisherID;
    private String        publisherName;
    private int           publicationYear;
    private String        edition;
    private int           totalCopies;
    private int           availableCopies;
    private String        shelfLocation;
    private String        coverImagePath;
    private String        description;
    private boolean       isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String        authorName;
    private List<Author>  authors;      // ← ADDED

    public Book() {}

    public Book(String isbn, String title, int categoryID, int publisherID,
                int publicationYear, String edition, int totalCopies, String shelfLocation) {
        this.isbn            = isbn;
        this.title           = title;
        this.categoryID      = categoryID;
        this.publisherID     = publisherID;
        this.publicationYear = publicationYear;
        this.edition         = edition;
        this.totalCopies     = totalCopies;
        this.availableCopies = totalCopies;
        this.shelfLocation   = shelfLocation;
        this.isActive        = true;
    }

    public int           getBookID()                             { return bookID; }
    public void          setBookID(int bookID)                   { this.bookID = bookID; }
    public String        getIsbn()                               { return isbn; }
    public void          setIsbn(String isbn)                    { this.isbn = isbn; }
    public String        getTitle()                              { return title; }
    public void          setTitle(String title)                  { this.title = title; }
    public int           getCategoryID()                         { return categoryID; }
    public void          setCategoryID(int categoryID)           { this.categoryID = categoryID; }
    public String        getCategoryName()                       { return categoryName; }
    public void          setCategoryName(String categoryName)    { this.categoryName = categoryName; }
    public int           getPublisherID()                        { return publisherID; }
    public void          setPublisherID(int publisherID)         { this.publisherID = publisherID; }
    public String        getPublisherName()                      { return publisherName; }
    public void          setPublisherName(String publisherName)  { this.publisherName = publisherName; }
    public int           getPublicationYear()                    { return publicationYear; }
    public void          setPublicationYear(int year)            { this.publicationYear = year; }
    public String        getEdition()                            { return edition; }
    public void          setEdition(String edition)              { this.edition = edition; }
    public int           getTotalCopies()                        { return totalCopies; }
    public void          setTotalCopies(int totalCopies)         { this.totalCopies = totalCopies; }
    public int           getAvailableCopies()                    { return availableCopies; }
    public void          setAvailableCopies(int copies)          { this.availableCopies = copies; }
    public String        getShelfLocation()                      { return shelfLocation; }
    public void          setShelfLocation(String shelfLocation)  { this.shelfLocation = shelfLocation; }
    public String        getCoverImagePath()                     { return coverImagePath; }
    public void          setCoverImagePath(String path)          { this.coverImagePath = path; }
    public String        getDescription()                        { return description; }
    public void          setDescription(String description)      { this.description = description; }
    public boolean       isActive()                              { return isActive; }
    public void          setActive(boolean isActive)             { this.isActive = isActive; }
    public LocalDateTime getCreatedAt()                          { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt()                          { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }
    public String        getAuthorName()                         { return authorName; }
    public void          setAuthorName(String authorName)        { this.authorName = authorName; }
    public List<Author>  getAuthors()                            { return authors; }
    public void          setAuthors(List<Author> authors)        { this.authors = authors; }

    public boolean isAvailable() { return availableCopies > 0 && isActive; }

    @Override
    public String toString() { return title + " (" + isbn + ")"; }
}