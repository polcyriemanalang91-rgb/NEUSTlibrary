package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Publisher {

    private int           publisherID;
    private String        publisherName;
    private String        address;
    private String        contactEmail;
    private String        contactPhone;
    private boolean       isActive;
    private LocalDateTime createdAt;  // FIX: added — PublisherDAO calls setCreatedAt()

    public Publisher() {}

    public Publisher(String publisherName) {
        this.publisherName = publisherName;
        this.isActive      = true;
    }

    // FIX: added full constructor — ManagePublishersPanel calls new Publisher(name, address, email, phone)
    public Publisher(String publisherName, String address, String contactEmail, String contactPhone) {
        this.publisherName = publisherName;
        this.address       = address;
        this.contactEmail  = contactEmail;
        this.contactPhone  = contactPhone;
        this.isActive      = true;
    }

    public int           getPublisherID()                           { return publisherID; }
    public void          setPublisherID(int publisherID)            { this.publisherID = publisherID; }
    public String        getPublisherName()                         { return publisherName; }
    public void          setPublisherName(String publisherName)     { this.publisherName = publisherName; }
    public String        getAddress()                               { return address; }
    public void          setAddress(String address)                 { this.address = address; }
    public String        getContactEmail()                          { return contactEmail; }
    public void          setContactEmail(String contactEmail)       { this.contactEmail = contactEmail; }
    public String        getContactPhone()                          { return contactPhone; }
    public void          setContactPhone(String contactPhone)       { this.contactPhone = contactPhone; }
    public boolean       isActive()                                 { return isActive; }
    public void          setActive(boolean isActive)                { this.isActive = isActive; }
    public LocalDateTime getCreatedAt()                             { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }

    @Override
    public String toString() { return publisherName; }
}