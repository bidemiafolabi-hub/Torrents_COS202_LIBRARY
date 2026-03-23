package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.List;

public final class SortEngine {
    private SortEngine() {}

    public enum SortField { TITLE, AUTHOR, YEAR }
    public enum Algo { INSERTION_SORT, MERGE_SORT }

    public static List<LibraryItem> sort(List<LibraryItem> items, SortField field, Algo algo) {
        List<LibraryItem> copy = new ArrayList<>(items);
        switch (algo) {
            case INSERTION_SORT -> insertionSort(copy, field);
            case MERGE_SORT -> copy = mergeSort(copy, field);
        }
        return copy;
    }

    // Insertion sort
    private static void insertionSort(List<LibraryItem> a, SortField f) {
        for (int i = 1; i < a.size(); i++) {
            LibraryItem key = a.get(i);
            int j = i - 1;
            while (j >= 0 && compare(a.get(j), key, f) > 0) {
                a.set(j + 1, a.get(j));
                j--;
            }
            a.set(j + 1, key);
        }
    }

    // Merge sort
    private static List<LibraryItem> mergeSort(List<LibraryItem> a, SortField f) {
        if (a.size() <= 1) return a;
        int mid = a.size() / 2;
        List<LibraryItem> left = mergeSort(a.subList(0, mid), f);
        List<LibraryItem> right = mergeSort(a.subList(mid, a.size()), f);
        return merge(left, right, f);
    }

    private static List<LibraryItem> merge(List<LibraryItem> left, List<LibraryItem> right, SortField f) {
        List<LibraryItem> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), f) <= 0) out.add(left.get(i++));
            else out.add(right.get(j++));
        }
        while (i < left.size()) out.add(left.get(i++));
        while (j < right.size()) out.add(right.get(j++));
        return out;
    }

    private static int compare(LibraryItem a, LibraryItem b, SortField f) {
        return switch (f) {
            case TITLE -> a.getTitle().compareToIgnoreCase(b.getTitle());
            case AUTHOR -> a.getAuthor().compareToIgnoreCase(b.getAuthor());
            case YEAR -> Integer.compare(a.getYear(), b.getYear());
        };
    }
}