package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LibraryDatabase implements Serializable {
    // ArrayList: store items
    private final List<LibraryItem> items = new ArrayList<>();
    private final List<UserAccount> users = new ArrayList<>();

    public List<LibraryItem> getItems() { return items; }
    public List<UserAccount> getUsers() { return users; }

    public void addItem(LibraryItem item) { items.add(item); }
    public void removeItemById(String id) { items.removeIf(it -> it.getId().equals(id)); }

    public void addUser(UserAccount user) { users.add(user); }

    public UserAccount getUserById(String userId) {
        for (UserAccount u : users) if (u.getUserId().equals(userId)) return u;
        return null;
    }
}