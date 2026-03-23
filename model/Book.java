package model;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Queue;

public class Book extends LibraryItem implements Borrowable {
  
    public void setAvailable(boolean available) { this.available = available; }
    public void setBorrowedByUserId(String id) { this.borrowedByUserId = id; }
    public void setDueDate(java.time.LocalDate due) { this.dueDate = due; }
    
    private boolean available = true;
    private String borrowedByUserId = null;
    private LocalDate dueDate = null;

    // Queue: reservation/waitlist
    private final Queue<String> reservationQueue = new ArrayDeque<>();

    public Book(String id, String title, String author, int year, String category) {
        super(id, title, author, year, category);
    }

    @Override public String getType() { return "Book"; }

    @Override public boolean isAvailable() { return available; }

    public String getBorrowedByUserId() { return borrowedByUserId; }
    public LocalDate getDueDate() { return dueDate; }

    public Queue<String> getReservationQueue() { return reservationQueue; }

    public void addToWaitlist(String userId) {
        if (!reservationQueue.contains(userId)) reservationQueue.offer(userId);
    }

    @Override
    public boolean borrow(String userId) {
        if (available) {
            available = false;
            borrowedByUserId = userId;
            dueDate = LocalDate.now().plusDays(14);  
            incrementBorrowed();
            return true;
        }
        // Not available -> add to queue
        addToWaitlist(userId);
        return false;
    }

    @Override
    public boolean returnItem() {
        if (available) return false;

        available = true;
        borrowedByUserId = null;
        dueDate = null;

        // Auto-assign to next in waitlist (if any) - optional: controller can handle UI notifications
        String next = reservationQueue.poll();
        if (next != null) {
            // Immediately borrow for next user
            borrow(next);
        }
        return true;
    }
}