package model;

public interface Borrowable {
    boolean isAvailable();
    boolean borrow(String userId);
    boolean returnItem();
}

 