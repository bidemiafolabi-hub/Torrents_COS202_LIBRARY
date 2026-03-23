package controller;

import model.*;
import utils.IDGenerator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryManager {

    private final LibraryDatabase db;

    // Stack for undo (admin operations)
    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    // Array cache for Most Frequently Accessed Items (fixed size)
    private final LibraryItem[] hotCache = new LibraryItem[5];

    public LibraryManager(LibraryDatabase db) {
        this.db = db;
    }

    public LibraryDatabase getDb() { return db; }
    public Deque<Runnable> getUndoStack() { return undoStack; }
    public LibraryItem[] getHotCache() { return hotCache; }

    public UserAccount ensureUser(String userId, String fullName) {
        UserAccount u = db.getUserById(userId);
        if (u == null) {
            u = new UserAccount(userId, fullName);
            db.addUser(u);
        }
        return u;
    }

    public LibraryItem addItem(String type, String title, String author, int year, String category) {
        String id = IDGenerator.newId(type.toUpperCase());
        LibraryItem item = switch (type) {
            case "Book" -> new Book(id, title, author, year, category);
            case "Magazine" -> new Magazine(id, title, author, year, category);
            case "Journal" -> new Journal(id, title, author, year, category);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };

        db.addItem(item);

        // Undo: remove item
        undoStack.push(() -> db.removeItemById(item.getId()));
        return item;
    }

    public boolean deleteItem(String itemId) {
        LibraryItem item = getItemById(itemId);
        if (item == null) return false;

        db.removeItemById(itemId);

        // Undo: add back same item
        undoStack.push(() -> db.addItem(item));
        return true;
    }

    public boolean undoLastAdminAction() {
        Runnable undo = undoStack.poll();
        if (undo == null) return false;
        undo.run();
        return true;
    }

    public LibraryItem getItemById(String id) {
        for (LibraryItem it : db.getItems()) if (it.getId().equals(id)) return it;
        return null;
    }

    // Access tracking for cache
    public void markAccess(LibraryItem item) {
        item.incrementAccess();
        // very simple cache update: keep 5 highest accessed
        List<LibraryItem> candidates = new ArrayList<>();
        for (LibraryItem c : hotCache) if (c != null) candidates.add(c);
        candidates.add(item);

        candidates.sort((a, b) -> Integer.compare(b.getTimesAccessed(), a.getTimesAccessed()));

        for (int i = 0; i < hotCache.length; i++) {
            hotCache[i] = i < candidates.size() ? candidates.get(i) : null;
        }
    }

    // Borrow/Return workflows
    public String borrowItem(String userId, String fullName, String itemId) {
        UserAccount user = ensureUser(userId, fullName);
        LibraryItem item = getItemById(itemId);
        if (item == null) return "Item not found.";

        markAccess(item);

        if (item instanceof Borrowable b) {
            boolean success = b.borrow(userId);
            if (success) {
                user.addBorrow(item.getId(), item.getTitle());
                return "Borrow successful. Due in 14 days.";
            }
            // For books: added to waitlist
            if (item instanceof Book book) {
                return "Not available. Added to waitlist. Position: " + book.getReservationQueue().size();
            }
            return "Not available.";
        }
        return "This item type is not borrowable.";
    }

    public String returnItem(String userId, String itemId) {
        UserAccount user = db.getUserById(userId);
        if (user == null) return "User not found.";

        LibraryItem item = getItemById(itemId);
        if (item == null) return "Item not found.";

        if (item instanceof Borrowable b) {
            boolean ok = b.returnItem();
            if (ok) {
                user.markReturn(itemId);
                return "Return successful.";
            }
            return "Return failed (already available?).";
        }
        return "This item type is not borrowable.";
    }

    // Recursion requirement: recursively compute total count by category
    public int countByCategoryRecursive(String category) {
        return countByCategoryRecursive(category, 0);
    }
    private int countByCategoryRecursive(String category, int idx) {
        if (idx >= db.getItems().size()) return 0;
        int add = db.getItems().get(idx).getCategory().equalsIgnoreCase(category) ? 1 : 0;
        return add + countByCategoryRecursive(category, idx + 1);
    }

    // Recursion requirement: overdue charge computation
    // ₦50/day overdue (example)
    public long computeOverdueChargeRecursive(LocalDate dueDate, LocalDate today) {
        if (dueDate == null || !today.isAfter(dueDate)) return 0;
        return 50 + computeOverdueChargeRecursive(dueDate.plusDays(1), today);
    }

    // Reports
    public List<LibraryItem> mostBorrowed(int n) {
        List<LibraryItem> copy = new ArrayList<>(db.getItems());
        copy.sort((a,b) -> Integer.compare(b.getTimesBorrowed(), a.getTimesBorrowed()));
        return copy.subList(0, Math.min(n, copy.size()));
    }

    public List<String> usersWithOverdueItems() {
        List<String> out = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (UserAccount u : db.getUsers()) {
            boolean hasOverdue = false;
            for (BorrowRecord r : u.getHistory()) {
                if (!r.isOpen()) continue;
                LibraryItem it = getItemById(r.getItemId());
                if (it instanceof Book book) {
                    if (book.getDueDate() != null && today.isAfter(book.getDueDate())) {
                        hasOverdue = true;
                        break;
                    }
                }
            }
            if (hasOverdue) out.add(u.getUserId() + " - " + u.getFullName());
        }
        return out;
    }

    public Map<String,Integer> categoryDistribution() {
        Map<String,Integer> map = new LinkedHashMap<>();
        for (LibraryItem it : db.getItems()) {
            map.put(it.getCategory(), map.getOrDefault(it.getCategory(), 0) + 1);
        }
        return map;
    }

    // Timer helper: how many overdue books exist
    public int overdueCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (LibraryItem it : db.getItems()) {
            if (it instanceof Book b && !b.isAvailable() && b.getDueDate() != null && today.isAfter(b.getDueDate())) {
                count++;
            }
        }
        return count;
    }

    public long overdueDays(Book book) {
        if (book.getDueDate() == null) return 0;
        long days = ChronoUnit.DAYS.between(book.getDueDate(), LocalDate.now());
        return Math.max(0, days);
    }
}