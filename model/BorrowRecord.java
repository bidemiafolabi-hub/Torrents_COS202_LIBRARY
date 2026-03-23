package model;

import java.io.Serializable;
import java.time.LocalDate;

public class BorrowRecord implements Serializable {
    private final String itemId;
    private final String itemTitle;
    private final LocalDate borrowedOn;
    private LocalDate returnedOn; // null if not returned

    public BorrowRecord(String itemId, String itemTitle, LocalDate borrowedOn) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.borrowedOn = borrowedOn;
    }

    public String getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public LocalDate getBorrowedOn() { return borrowedOn; }
    public LocalDate getReturnedOn() { return returnedOn; }

    public void markReturned(LocalDate date) { this.returnedOn = date; }

    public boolean isOpen() { return returnedOn == null; }
}