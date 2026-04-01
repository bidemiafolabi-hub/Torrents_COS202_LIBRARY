# Smart Library Circulation & Automation System

## Overview

The **Smart Library Circulation & Automation System** is a Java-based desktop application designed to manage library resources efficiently.
It allows administrators and users to view items, borrow and return books, search and sort records, and generate reports about library activity.

The system uses **Java Swing** for the graphical user interface and implements several computer science concepts such as **object-oriented programming, search algorithms, sorting algorithms, file handling, and GUI event-driven programming**.

The application also includes a **modern dark-mode interface using FlatLaf**, making the user interface more visually appealing and easier to use.

---

## Features

### 1. Library Item Management

* View all library items in a structured table
* Display item details such as:

  * ID
  * Title
  * Author
  * Category
  * Year
  * Borrow status

### 2. Borrow and Return System

* Users can borrow library items using their **User ID and Full Name**
* Borrowed books receive a **due date (14 days)**
* Items can be returned and availability is updated automatically

### 3. Search Functionality

Multiple search algorithms are implemented:

* **Linear Search** – search items by title
* **Binary Search** – optimized search by title (requires sorted data)
* **Recursive Search** – search items by author

### 4. Sorting System

Items can be sorted by different fields using different algorithms:

* Title
* Author
* Year
* Category

Sorting algorithms implemented include:

* Bubble Sort
* Insertion Sort
* Merge Sort

### 5. Reports Generation

The system can generate analytical reports including:

* **Most borrowed items**
* **Users with overdue items**
* **Category distribution**
* **Hot cache (most accessed items)**

### 6. File Persistence

Library data can be saved and loaded using **JSON files**.

Features include:

* Save database
* Save As (choose file location)
* Load existing database

### 7. Modern Graphical Interface

The application uses **Java Swing** enhanced with **FlatLaf Dark Mode** for a modern user experience.

Interface panels include:

* View Items
* Borrow / Return
* Admin
* Search & Sort
* Reports

---

## Advanced GUI Techniques Implemented

The project includes several advanced GUI features:

* **Timers for overdue notifications**
* **Input validation with dialog popups**
* **Keyboard shortcuts and menu mnemonics**
* **File chooser dialogs for saving files**

These improve usability and demonstrate advanced event-driven GUI programming.

---

## Project Structure

```
LibraryProject
│
├── gui
│   ├── MainWindow.java
│   ├── BorrowPanel.java
│   ├── ViewItemsPanel.java
│   ├── SearchSortPanel.java
│   ├── ReportsPanel.java
│   └── AdminPanel.java
│
├── controller
│   ├── LibraryManager.java
│   ├── BorrowController.java
│   ├── SearchEngine.java
│   └── SortEngine.java
│
├── model
│   ├── LibraryItem.java
│   ├── Book.java
│   └── LibraryDatabase.java
│
├── utils
│   └── FileHandler.java
│
└── flatlaf-3.x.jar
```

---

## Technologies Used

* **Java**
* **Java Swing GUI**
* **FlatLaf (Dark Mode UI)**
* **JSON file storage**
* **Object-Oriented Programming**
* **Search Algorithms**
* **Sorting Algorithms**

---

## How to Compile and Run

### 1. Compile the Project

Windows:

```
javac -cp ".;flatlaf-3.7.jar" gui/*.java controller/*.java model/*.java utils/*.java
```

Mac/Linux:

```
javac -cp ".:flatlaf-3.7.jar" gui/*.java controller/*.java model/*.java utils/*.java
```

### 2. Run the Application

Windows:

```
java -cp ".;flatlaf-3.7.jar" gui.MainWindow
```

Mac/Linux:

```
java -cp ".:flatlaf-3.7.jar" gui.MainWindow
```

---

## System Workflow

1. Application starts and loads the library database.
2. The main window opens with multiple tabs.
3. Users can:

   * View items
   * Borrow or return books
   * Search or sort library items
   * Generate reports
4. The system periodically checks for overdue items using a timer.
5. Library data can be saved or loaded from JSON files.

---

## UML Design

The system follows a **layered architecture**:

* **Model Layer** – represents data structures (LibraryItem, Book, Database)
* **Controller Layer** – manages business logic
* **GUI Layer** – handles user interaction
* **Utility Layer** – file handling and persistence

A **UML Class Diagram** is included to illustrate class hierarchy and relationships.

---

## Future Improvements

Possible enhancements include:

* User authentication system
* Barcode scanning for library items
* Database integration (SQL)
* Advanced analytics dashboard
* Improved GUI components and animations

---

## Author

The Torrents.
