package model;

public class Magazine extends LibraryItem {
    public Magazine(String id, String title, String author, int year, String category) {
        super(id, title, author, year, category);
    }
    @Override public String getType() { return "Magazine"; }
}