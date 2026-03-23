package utils;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

/**
 * Pure JSON text persistence (no external libraries).
 *
 * Stores:
 * - LibraryDatabase { items: [...], users: [...] }
 * - Items include type discriminator: "Book" | "Magazine" | "Journal"
 * - Book includes borrow state + reservationQueue
 * - Users include borrow history (BorrowRecord list)
 */
public final class FileHandler {
    private FileHandler() {}

    // ----------------------------
    // Public API
    // ----------------------------

    public static void saveDatabase(File file, LibraryDatabase db) throws IOException {
        String json = toJson(db);
        Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
    }

    public static LibraryDatabase loadDatabase(File file) throws IOException {
        if (!file.exists()) return new LibraryDatabase();
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) return new LibraryDatabase();

        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map)) return new LibraryDatabase();

        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) parsed;
        return fromJson(root);
    }

    // ----------------------------
    // Serialization (DB -> JSON)
    // ----------------------------

    private static String toJson(LibraryDatabase db) {
        Map<String, Object> root = new LinkedHashMap<>();

        // items
        List<Object> items = new ArrayList<>();
        for (LibraryItem it : db.getItems()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", it.getId());
            m.put("type", it.getType());
            m.put("title", it.getTitle());
            m.put("author", it.getAuthor());
            m.put("year", it.getYear());
            m.put("category", it.getCategory());
            m.put("timesAccessed", it.getTimesAccessed());
            m.put("timesBorrowed", it.getTimesBorrowed());

            if (it instanceof Book b) {
                m.put("available", b.isAvailable());
                m.put("borrowedByUserId", b.getBorrowedByUserId());
                m.put("dueDate", b.getDueDate() == null ? null : b.getDueDate().toString());

                List<Object> q = new ArrayList<>();
                for (String uid : b.getReservationQueue()) q.add(uid);
                m.put("reservationQueue", q);
            }

            items.add(m);
        }
        root.put("items", items);

        // users
        List<Object> users = new ArrayList<>();
        for (UserAccount u : db.getUsers()) {
            Map<String, Object> um = new LinkedHashMap<>();
            um.put("userId", u.getUserId());
            um.put("fullName", u.getFullName());

            List<Object> hist = new ArrayList<>();
            for (BorrowRecord r : u.getHistory()) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("itemId", r.getItemId());
                rm.put("itemTitle", r.getItemTitle());
                rm.put("borrowedOn", r.getBorrowedOn() == null ? null : r.getBorrowedOn().toString());
                rm.put("returnedOn", r.getReturnedOn() == null ? null : r.getReturnedOn().toString());
                hist.add(rm);
            }
            um.put("history", hist);

            users.add(um);
        }
        root.put("users", users);

        return Json.stringify(root);
    }

    // ----------------------------
    // Deserialization (JSON -> DB)
    // ----------------------------

    private static LibraryDatabase fromJson(Map<String, Object> root) {
        LibraryDatabase db = new LibraryDatabase();

        // items
        Object itemsObj = root.get("items");
        if (itemsObj instanceof List<?> items) {
            for (Object o : items) {
                if (!(o instanceof Map)) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) o;

                String id = asString(m.get("id"));
                String type = asString(m.get("type"));
                String title = asString(m.get("title"));
                String author = asString(m.get("author"));
                int year = asInt(m.get("year"));
                String category = asString(m.get("category"));

                LibraryItem item;
                if ("Book".equalsIgnoreCase(type)) {
                    Book b = new Book(id, title, author, year, category);

                    // restore book state
                    b.setAvailable(asBoolean(m.get("available"), true));
                    b.setBorrowedByUserId(asNullableString(m.get("borrowedByUserId")));
                    String due = asNullableString(m.get("dueDate"));
                    b.setDueDate(due == null ? null : LocalDate.parse(due));

                    // restore queue
                    Object qObj = m.get("reservationQueue");
                    if (qObj instanceof List<?> qList) {
                        for (Object qv : qList) {
                            String uid = asString(qv);
                            if (uid != null && !uid.isBlank()) b.getReservationQueue().offer(uid);
                        }
                    }

                    item = b;
                } else if ("Magazine".equalsIgnoreCase(type)) {
                    item = new Magazine(id, title, author, year, category);
                } else if ("Journal".equalsIgnoreCase(type)) {
                    item = new Journal(id, title, author, year, category);
                } else {
                    // unknown type -> skip
                    continue;
                }

                // restore counters (we can't set private fields directly; so we "replay" increments)
                int accessed = asInt(m.get("timesAccessed"));
                int borrowed = asInt(m.get("timesBorrowed"));
                for (int i = 0; i < accessed; i++) item.incrementAccess();
                for (int i = 0; i < borrowed; i++) item.incrementBorrowed();

                db.addItem(item);
            }
        }

        // users
        Object usersObj = root.get("users");
        if (usersObj instanceof List<?> users) {
            for (Object o : users) {
                if (!(o instanceof Map)) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> um = (Map<String, Object>) o;

                String userId = asString(um.get("userId"));
                String fullName = asString(um.get("fullName"));
                if (userId == null || userId.isBlank()) continue;

                UserAccount u = new UserAccount(userId, fullName == null ? "" : fullName);

                Object histObj = um.get("history");
                if (histObj instanceof List<?> hist) {
                    for (Object ro : hist) {
                        if (!(ro instanceof Map)) continue;

                        @SuppressWarnings("unchecked")
                        Map<String, Object> rm = (Map<String, Object>) ro;

                        String itemId = asString(rm.get("itemId"));
                        String itemTitle = asString(rm.get("itemTitle"));
                        String borrowedOn = asNullableString(rm.get("borrowedOn"));
                        String returnedOn = asNullableString(rm.get("returnedOn"));

                        if (itemId == null) continue;

                        // BorrowRecord constructor takes (itemId, itemTitle, borrowedOn)
                        LocalDate bo = borrowedOn == null ? LocalDate.now() : LocalDate.parse(borrowedOn);
                        BorrowRecord br = new BorrowRecord(itemId, itemTitle == null ? "" : itemTitle, bo);

                        if (returnedOn != null) br.markReturned(LocalDate.parse(returnedOn));

                        u.getHistory().add(br);
                    }
                }

                db.addUser(u);
            }
        }

        return db;
    }

    // ----------------------------
    // Helpers (type conversions)
    // ----------------------------

    private static String asString(Object o) {
        if (o == null) return null;
        if (o instanceof String s) return s;
        return String.valueOf(o);
    }

    private static String asNullableString(Object o) {
        String s = asString(o);
        if (s == null) return null;
        if ("null".equalsIgnoreCase(s)) return null;
        return s;
    }

    private static int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return 0; }
    }

    private static boolean asBoolean(Object o, boolean def) {
        if (o == null) return def;
        if (o instanceof Boolean b) return b;
        String s = String.valueOf(o).trim().toLowerCase();
        if ("true".equals(s)) return true;
        if ("false".equals(s)) return false;
        return def;
    }

    // ----------------------------
    // Minimal JSON parser + writer
    // ----------------------------

    private static final class Json {

        // ---- stringify ----
        static String stringify(Object value) {
            StringBuilder sb = new StringBuilder();
            writeValue(sb, value);
            return sb.toString();
        }

        private static void writeValue(StringBuilder sb, Object v) {
            if (v == null) { sb.append("null"); return; }
            if (v instanceof String s) { sb.append('"').append(escape(s)).append('"'); return; }
            if (v instanceof Boolean || v instanceof Number) { sb.append(v); return; }
            if (v instanceof Map<?,?> m) {
                sb.append('{');
                boolean first = true;
                for (Map.Entry<?,?> e : m.entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':');
                    writeValue(sb, e.getValue());
                }
                sb.append('}');
                return;
            }
            if (v instanceof List<?> list) {
                sb.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(',');
                    writeValue(sb, list.get(i));
                }
                sb.append(']');
                return;
            }
            // fallback
            sb.append('"').append(escape(String.valueOf(v))).append('"');
        }

        private static String escape(String s) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> out.append(c);
                }
            }
            return out.toString();
        }

        // ---- parse ----
        static Object parse(String json) {
            return new Parser(json).parseValue();
        }

        private static final class Parser {
            private final String s;
            private int i = 0;

            Parser(String s) { this.s = s; }

            Object parseValue() {
                skipWs();
                if (i >= s.length()) return null;
                char c = s.charAt(i);
                return switch (c) {
                    case '{' -> parseObject();
                    case '[' -> parseArray();
                    case '"' -> parseString();
                    case 't', 'f' -> parseBoolean();
                    case 'n' -> parseNull();
                    default -> parseNumber();
                };
            }

            private Map<String,Object> parseObject() {
                Map<String,Object> map = new LinkedHashMap<>();
                expect('{');
                skipWs();
                if (peek('}')) { i++; return map; }

                while (true) {
                    skipWs();
                    String key = parseString();
                    skipWs();
                    expect(':');
                    Object val = parseValue();
                    map.put(key, val);
                    skipWs();
                    if (peek('}')) { i++; break; }
                    expect(',');
                }
                return map;
            }

            private List<Object> parseArray() {
                List<Object> list = new ArrayList<>();
                expect('[');
                skipWs();
                if (peek(']')) { i++; return list; }

                while (true) {
                    Object val = parseValue();
                    list.add(val);
                    skipWs();
                    if (peek(']')) { i++; break; }
                    expect(',');
                }
                return list;
            }

            private String parseString() {
                expect('"');
                StringBuilder out = new StringBuilder();
                while (i < s.length()) {
                    char c = s.charAt(i++);
                    if (c == '"') break;
                    if (c == '\\') {
                        char n = s.charAt(i++);
                        switch (n) {
                            case '"' -> out.append('"');
                            case '\\' -> out.append('\\');
                            case 'n' -> out.append('\n');
                            case 'r' -> out.append('\r');
                            case 't' -> out.append('\t');
                            default -> out.append(n);
                        }
                    } else out.append(c);
                }
                return out.toString();
            }

            private Boolean parseBoolean() {
                if (s.startsWith("true", i)) { i += 4; return true; }
                if (s.startsWith("false", i)) { i += 5; return false; }
                return false;
            }

            private Object parseNull() {
                if (s.startsWith("null", i)) { i += 4; return null; }
                return null;
            }

            private Number parseNumber() {
                int start = i;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                        i++;
                    } else break;
                }
                String num = s.substring(start, i).trim();
                if (num.isEmpty()) return 0;
                // only ints needed for this project; keep safe fallback
                try {
                    if (num.contains(".") || num.contains("e") || num.contains("E")) return Double.parseDouble(num);
                    return Long.parseLong(num);
                } catch (Exception e) {
                    return 0;
                }
            }

            private void skipWs() {
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
                    else break;
                }
            }

            private boolean peek(char c) {
                return i < s.length() && s.charAt(i) == c;
            }

            private void expect(char c) {
                skipWs();
                if (i >= s.length() || s.charAt(i) != c) {
                    throw new IllegalArgumentException("Invalid JSON near index " + i + ", expected '" + c + "'");
                }
                i++;
            }
        }
    }
}