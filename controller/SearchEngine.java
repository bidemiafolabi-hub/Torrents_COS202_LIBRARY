package controller;

import model.LibraryItem;

import java.util.List;

public final class SearchEngine {
    private SearchEngine() {}

    // 1) Linear search
    public static int linearSearchByTitle(List<LibraryItem> items, String title) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getTitle().equalsIgnoreCase(title)) return i;
        }
        return -1;
    }
 
    // 2) Binary search (requires sorted by title ascending)
    public static int binarySearchByTitle(List<LibraryItem> items, String title) {
        int lo = 0, hi = items.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int cmp = items.get(mid).getTitle().compareToIgnoreCase(title);
            if (cmp == 0) return mid;
            if (cmp < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    // 3) Recursive search by author (returns index)
    public static int recursiveSearchByAuthor(List<LibraryItem> items, String author) {
        return recursiveSearchByAuthor(items, author, 0);
    }
 
    private static int recursiveSearchByAuthor(List<LibraryItem> items, String author, int idx) {
        if (idx >= items.size()) return -1;
        if (items.get(idx).getAuthor().equalsIgnoreCase(author)) return idx;
        return recursiveSearchByAuthor(items, author, idx + 1);
    }
}