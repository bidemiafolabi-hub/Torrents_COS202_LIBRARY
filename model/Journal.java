package model;

public class Journal extends LibraryItem {
    public Journal(String id, String title, String author, int year, String category) {
        super(id, title, author, year, category);
    }
    @Override public String getType() { return "Journal"; }
}