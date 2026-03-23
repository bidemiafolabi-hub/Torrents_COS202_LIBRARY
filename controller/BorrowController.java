package controller;

public class BorrowController {
    private final LibraryManager manager;

    public BorrowController(LibraryManager manager) {
        this.manager = manager;
    }

    public String borrow(String userId, String fullName, String itemId) {
        return manager.borrowItem(userId, fullName, itemId);
    }

    public String returns(String userId, String itemId) {
        return manager.returnItem(userId, itemId);
    }
}