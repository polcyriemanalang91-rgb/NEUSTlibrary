package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class BookConditionalLog {
    private int logID;
    private int bookID;
    private int librarianID;
    private Integer borrowID;
    private String condition; // New, Good, Fair, Damaged, Lost
    private String remarks;
    private LocalDateTime logDate;

    // Joined display fields
    private String bookTitle;
    private String librarianName;
    private String isbn;

    public BookConditionalLog() {}

    public BookConditionalLog(int bookID, int librarianID, Integer borrowID,
                               String condition, String remarks) {
        this.bookID = bookID;
        this.librarianID = librarianID;
        this.borrowID = borrowID;
        this.condition = condition;
        this.remarks = remarks;
    }

    public int getLogID() { return logID; }
    public void setLogID(int logID) { this.logID = logID; }

    public int getBookID() { return bookID; }
    public void setBookID(int bookID) { this.bookID = bookID; }

    public int getLibrarianID() { return librarianID; }
    public void setLibrarianID(int librarianID) { this.librarianID = librarianID; }

    public Integer getBorrowID() { return borrowID; }
    public void setBorrowID(Integer borrowID) { this.borrowID = borrowID; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getLibrarianName() { return librarianName; }
    public void setLibrarianName(String librarianName) { this.librarianName = librarianName; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override
    public String toString() {
        return "Log #" + logID + " - " + bookTitle + " [" + condition + "]";
    }
}