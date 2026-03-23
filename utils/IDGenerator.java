package utils;

import java.util.UUID;

public final class IDGenerator {
    private IDGenerator() {}
    public static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}