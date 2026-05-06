package neustlibrarysystem.model;

import java.time.LocalDateTime;

public class Category {

    private int           categoryID;
    private String        categoryName;
    private String        description;
    private boolean       isActive;
    private LocalDateTime createdAt;  // FIX: added — CategoryDAO calls setCreatedAt()

    public Category() {}

    public Category(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description  = description;
        this.isActive     = true;
    }

    public int           getCategoryID()                        { return categoryID; }
    public void          setCategoryID(int categoryID)          { this.categoryID = categoryID; }
    public String        getCategoryName()                      { return categoryName; }
    public void          setCategoryName(String categoryName)   { this.categoryName = categoryName; }
    public String        getDescription()                       { return description; }
    public void          setDescription(String description)     { this.description = description; }
    public boolean       isActive()                             { return isActive; }
    public void          setActive(boolean isActive)            { this.isActive = isActive; }
    public LocalDateTime getCreatedAt()                         { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }

    @Override
    public String toString() { return categoryName; }
}