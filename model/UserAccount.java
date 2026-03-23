package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserAccount implements Serializable {
    private final String userId;
    private String fullName;
    private final List<BorrowRecord> history = new ArrayList<>();

    public UserAccount(String userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<BorrowRecord> getHistory() { return history; }

    public void addBorrow(String itemId, String itemTitle) {
        history.add(new BorrowRecord(itemId, itemTitle, LocalDate.now()));
    }

    public void markReturn(String itemId) {
        for (int i = history.size() - 1; i >= 0; i--) {
            BorrowRecord r = history.get(i);
            if (r.getItemId().equals(itemId) && r.isOpen()) {
                r.markReturned(LocalDate.now());
                return;
            }
        }
    }
}