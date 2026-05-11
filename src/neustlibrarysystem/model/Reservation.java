package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Reservation {

    // FIX: status is stored as plain String — ReservationDAO calls setStatus(String)
    // Inalis ang enum ReservationStatus para consistent sa DAO at DB values
    private int           reservationID;
    private int           bookID;
    private int           memberID;
    private String        bookTitle;    // joined display field
    private String        memberName;   // joined display field
    private String        studentID;    // joined display field
    private LocalDateTime reservedDate;
    private LocalDateTime expiryDate;
    private String        status;       // "Pending","Confirmed","Cancelled","Completed","Expired"
    private String        notes;
    private Integer       processedBy;
    private LocalDateTime processedDate;

    public Reservation() {}

    public int           getReservationID()                         { return reservationID; }
    public void          setReservationID(int reservationID)        { this.reservationID = reservationID; }
    public int           getBookID()                                { return bookID; }
    public void          setBookID(int bookID)                      { this.bookID = bookID; }
    public int           getMemberID()                              { return memberID; }
    public void          setMemberID(int memberID)                  { this.memberID = memberID; }
    public String        getBookTitle()                             { return bookTitle; }
    public void          setBookTitle(String bookTitle)             { this.bookTitle = bookTitle; }
    public String        getMemberName()                            { return memberName; }
    public void          setMemberName(String memberName)           { this.memberName = memberName; }
    public String        getStudentID()                             { return studentID; }
    public void          setStudentID(String studentID)             { this.studentID = studentID; }
    public LocalDateTime getReservedDate()                          { return reservedDate; }
    public void          setReservedDate(LocalDateTime reservedDate){ this.reservedDate = reservedDate; }
    public LocalDateTime getExpiryDate()                            { return expiryDate; }
    public void          setExpiryDate(LocalDateTime expiryDate)    { this.expiryDate = expiryDate; }
    public String        getStatus()                                { return status; }
    public void          setStatus(String status)                   { this.status = status; }
    public String        getNotes()                                 { return notes; }
    public void          setNotes(String notes)                     { this.notes = notes; }
    public Integer       getProcessedBy()                           { return processedBy; }
    public void          setProcessedBy(Integer processedBy)        { this.processedBy = processedBy; }
    public LocalDateTime getProcessedDate()                         { return processedDate; }
    public void          setProcessedDate(LocalDateTime processedDate){ this.processedDate = processedDate; }
    private String       bookAuthors;
    public String        getBookAuthors()                                      { return bookAuthors; }
    public void          setBookAuthors(String bookAuthors)                      { this.bookAuthors = bookAuthors; }

    @Override
    public String toString() { return "Reservation #" + reservationID + " - " + bookTitle; }
}