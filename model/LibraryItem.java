package model;

import java.io.Serializable;

public abstract class LibraryItem implements Serializable {
    private final String id;
    private String title;
    private String author;
    private int year;
    private String category;
    private int timesAccessed;
    private int timesBorrowed;

    protected LibraryItem(String id, String title, String author, int year, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.timesAccessed = 0;
        this.timesBorrowed = 0;
    }

    // getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public String getCategory() { return category; }
    
    // setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setYear(int year) { this.year = year; }
    public void setCategory(String category) { this.category = category; }

    public int getTimesAccessed() { return timesAccessed; }
    public void incrementAccess() { this.timesAccessed++; }

    public int getTimesBorrowed() { return timesBorrowed; }
    public void incrementBorrowed() { this.timesBorrowed++; }

    public abstract String getType();

    // Polymorphism: one function can process any LibraryItem
    public String toDisplayString() {
        return String.format("[%s] %s | %s | %s | %d", getType(), title, author, category, year);
    }
}