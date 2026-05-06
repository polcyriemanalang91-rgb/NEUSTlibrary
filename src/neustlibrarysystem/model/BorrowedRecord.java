package neustlibrarysystem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BorrowedRecord {

    private int           borrowID;
    private int           bookID;
    private int           memberID;
    private int           librarianID;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String        status;       // Borrowed, Returned, Overdue, Lost
    private BigDecimal    fineAmount;
    private boolean       finePaid;
    private Integer       reservationID;

    // Joined fields for display
    private String bookTitle;
    private String memberName;
    private String studentID;
    private String librarianName;
    private String isbn;
    private String memberEmail;

    public BorrowedRecord() {
        this.fineAmount = BigDecimal.ZERO;
        this.finePaid   = false;
    }

    public int           getBorrowID()                          { return borrowID; }
    public void          setBorrowID(int borrowID)              { this.borrowID = borrowID; }
    public int           getBookID()                            { return bookID; }
    public void          setBookID(int bookID)                  { this.bookID = bookID; }
    public int           getMemberID()                          { return memberID; }
    public void          setMemberID(int memberID)              { this.memberID = memberID; }
    public int           getLibrarianID()                       { return librarianID; }
    public void          setLibrarianID(int librarianID)        { this.librarianID = librarianID; }
    public LocalDateTime getBorrowDate()                        { return borrowDate; }
    public void          setBorrowDate(LocalDateTime borrowDate){ this.borrowDate = borrowDate; }
    public LocalDateTime getDueDate()                           { return dueDate; }
    public void          setDueDate(LocalDateTime dueDate)      { this.dueDate = dueDate; }
    public LocalDateTime getReturnDate()                        { return returnDate; }
    public void          setReturnDate(LocalDateTime returnDate){ this.returnDate = returnDate; }
    public String        getStatus()                            { return status; }
    public void          setStatus(String status)               { this.status = status; }
    public BigDecimal    getFineAmount()                        { return fineAmount; }
    public void          setFineAmount(BigDecimal fineAmount)   { this.fineAmount = fineAmount; }
    public boolean       isFinePaid()                           { return finePaid; }
    public void          setFinePaid(boolean finePaid)          { this.finePaid = finePaid; }
    public Integer       getReservationID()                     { return reservationID; }
    public void          setReservationID(Integer reservationID){ this.reservationID = reservationID; }
    public String        getBookTitle()                         { return bookTitle; }
    public void          setBookTitle(String bookTitle)         { this.bookTitle = bookTitle; }
    public String        getMemberName()                        { return memberName; }
    public void          setMemberName(String memberName)       { this.memberName = memberName; }
    public String        getStudentID()                         { return studentID; }
    public void          setStudentID(String studentID)         { this.studentID = studentID; }
    public String        getLibrarianName()                     { return librarianName; }
    public void          setLibrarianName(String librarianName) { this.librarianName = librarianName; }
    public String        getIsbn()                              { return isbn; }
    public void          setIsbn(String isbn)                   { this.isbn = isbn; }
    public String        getMemberEmail()                       { return memberEmail; }
    public void          setMemberEmail(String memberEmail)     { this.memberEmail = memberEmail; }

    public boolean isOverdue() {
        return returnDate == null
            && dueDate != null
            && LocalDateTime.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return "Borrow #" + borrowID + " - " + bookTitle + " [" + status + "]";
    }
}