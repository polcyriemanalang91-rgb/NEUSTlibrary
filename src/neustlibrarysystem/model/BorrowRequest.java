package neustlibrarysystem.model;

import java.time.LocalDate;

/**
 * Represents a borrow request submitted by a member.
 * Used by AcceptBorrowRequestPanel for librarian approval workflow.
 */
public class BorrowRequest {

    private int       requestId;
    private int       memberId;
    private String    memberName;
    private int       bookId;
    private String    bookTitle;
    private String    isbn;
    private LocalDate requestDate;
    private LocalDate preferredPickup;
    private String    status;               // "PENDING", "ACCEPTED", "REJECTED"
    private String    notes;
    private Integer   processedByLibrarianId;
    private LocalDate processedDate;
    private LocalDate dueDate;              // set when ACCEPTED

    // ── Constructors ──────────────────────────────────────────────────────────
    public BorrowRequest() {}

    public BorrowRequest(int requestId, int memberId, String memberName,
                         int bookId, String bookTitle, String isbn,
                         LocalDate requestDate, LocalDate preferredPickup,
                         String status, String notes) {
        this.requestId       = requestId;
        this.memberId        = memberId;
        this.memberName      = memberName;
        this.bookId          = bookId;
        this.bookTitle       = bookTitle;
        this.isbn            = isbn;
        this.requestDate     = requestDate;
        this.preferredPickup = preferredPickup;
        this.status          = status;
        this.notes           = notes;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int       getRequestId()              { return requestId; }
    public int       getMemberId()               { return memberId; }
    public String    getMemberName()             { return memberName; }
    public int       getBookId()                 { return bookId; }
    public String    getBookTitle()              { return bookTitle; }
    public String    getIsbn()                   { return isbn; }
    public LocalDate getRequestDate()            { return requestDate; }
    public LocalDate getPreferredPickup()        { return preferredPickup; }
    public String    getStatus()                 { return status; }
    public String    getNotes()                  { return notes; }
    public Integer   getProcessedByLibrarianId() { return processedByLibrarianId; }
    public LocalDate getProcessedDate()          { return processedDate; }
    public LocalDate getDueDate()                { return dueDate; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setRequestId(int requestId)                           { this.requestId = requestId; }
    public void setMemberId(int memberId)                             { this.memberId = memberId; }
    public void setMemberName(String memberName)                      { this.memberName = memberName; }
    public void setBookId(int bookId)                                 { this.bookId = bookId; }
    public void setBookTitle(String bookTitle)                        { this.bookTitle = bookTitle; }
    public void setIsbn(String isbn)                                  { this.isbn = isbn; }
    public void setRequestDate(LocalDate requestDate)                 { this.requestDate = requestDate; }
    public void setPreferredPickup(LocalDate preferredPickup)         { this.preferredPickup = preferredPickup; }
    public void setStatus(String status)                              { this.status = status; }
    public void setNotes(String notes)                                { this.notes = notes; }
    public void setProcessedByLibrarianId(Integer id)                 { this.processedByLibrarianId = id; }
    public void setProcessedDate(LocalDate processedDate)             { this.processedDate = processedDate; }
    public void setDueDate(LocalDate dueDate)                         { this.dueDate = dueDate; }

    // ── Utility ───────────────────────────────────────────────────────────────
    public boolean isPending()  { return "PENDING".equalsIgnoreCase(status); }
    public boolean isAccepted() { return "ACCEPTED".equalsIgnoreCase(status); }
    public boolean isRejected() { return "REJECTED".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return "BorrowRequest{" +
            "requestId=" + requestId +
            ", memberId=" + memberId +
            ", memberName='" + memberName + '\'' +
            ", bookTitle='" + bookTitle + '\'' +
            ", status='" + status + '\'' +
            '}';
    }
}