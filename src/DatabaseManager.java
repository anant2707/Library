import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Singleton Database Manager using SQLite.
 * Handles all CRUD operations for books, users, and transactions.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static DatabaseManager instance;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATABASE INITIALIZATION
    // ══════════════════════════════════════════════════════════════════
    public void initializeDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL, "
                + "full_name TEXT NOT NULL, "
                + "email TEXT, "
                + "role TEXT NOT NULL DEFAULT 'user', "
                + "created_at TEXT DEFAULT (date('now'))"
                + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS books ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "author TEXT NOT NULL, "
                + "isbn TEXT UNIQUE, "
                + "category TEXT, "
                + "total_qty INTEGER NOT NULL DEFAULT 1, "
                + "available_qty INTEGER NOT NULL DEFAULT 1, "
                + "added_date TEXT DEFAULT (date('now'))"
                + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS issued_books ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "book_id INTEGER NOT NULL, "
                + "user_id INTEGER NOT NULL, "
                + "issue_date TEXT NOT NULL, "
                + "due_date TEXT NOT NULL, "
                + "return_date TEXT, "
                + "fine REAL DEFAULT 0, "
                + "status TEXT DEFAULT 'ISSUED', "
                + "FOREIGN KEY(book_id) REFERENCES books(id), "
                + "FOREIGN KEY(user_id) REFERENCES users(id)"
                + ")");

            // Create indexes for search optimization
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_author ON books(author)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issued_status ON issued_books(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issued_user ON issued_books(user_id)");

            // Insert default admin if not exists
            PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.setString(3, "System Administrator");
            ps.setString(4, "admin@library.com");
            ps.setString(5, "admin");
            ps.executeUpdate();

            // Insert sample user
            ps.setString(1, "student1");
            ps.setString(2, "pass123");
            ps.setString(3, "Rahul Sharma");
            ps.setString(4, "rahul@college.com");
            ps.setString(5, "user");
            ps.executeUpdate();

            // Insert sample books
            String[] titles = {"Data Structures & Algorithms", "Operating System Concepts",
                "Database Management Systems", "Computer Networks", "Java: The Complete Reference",
                "Artificial Intelligence: A Modern Approach", "Design Patterns",
                "Introduction to Machine Learning", "Discrete Mathematics", "Software Engineering"};
            String[] authors = {"Thomas H. Cormen", "Abraham Silberschatz", "Raghu Ramakrishnan",
                "Andrew S. Tanenbaum", "Herbert Schildt", "Stuart Russell", "Gang of Four",
                "Ethem Alpaydin", "Kenneth H. Rosen", "Ian Sommerville"};
            String[] isbns = {"978-0262033848", "978-1118063330", "978-0072465631",
                "978-0132126953", "978-1260440232", "978-0136042594", "978-0201633610",
                "978-0262028189", "978-0073383095", "978-0133943030"};
            String[] cats = {"DSA", "OS", "DBMS", "Networking", "Programming",
                "AI", "Programming", "ML", "Mathematics", "SE"};

            PreparedStatement bookPs = conn.prepareStatement(
                "INSERT OR IGNORE INTO books (title, author, isbn, category, total_qty, available_qty) "
                + "VALUES (?, ?, ?, ?, ?, ?)");
            for (int i = 0; i < titles.length; i++) {
                bookPs.setString(1, titles[i]);
                bookPs.setString(2, authors[i]);
                bookPs.setString(3, isbns[i]);
                bookPs.setString(4, cats[i]);
                bookPs.setInt(5, 5);
                bookPs.setInt(6, 5);
                bookPs.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  AUTHENTICATION
    // ══════════════════════════════════════════════════════════════════
    public Map<String, String> authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("id", String.valueOf(rs.getInt("id")));
                user.put("username", rs.getString("username"));
                user.put("full_name", rs.getString("full_name"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                return user;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT
    // ══════════════════════════════════════════════════════════════════
    public boolean addUser(String username, String password, String fullName, String email, String role) {
        String sql = "INSERT INTO users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, email);
            ps.setString(5, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateUser(int id, String fullName, String email, String password) {
        String sql = "UPDATE users SET full_name = ?, email = ?" +
                     (password != null && !password.isEmpty() ? ", password = ?" : "") +
                     " WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            if (password != null && !password.isEmpty()) {
                ps.setString(3, password);
                ps.setInt(4, id);
            } else {
                ps.setInt(3, id);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteUser(int id) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM users WHERE id = ? AND role != 'admin'")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Object[]> getAllUsers() {
        List<Object[]> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, role, created_at FROM users ORDER BY id";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new Object[]{
                    rs.getInt("id"), rs.getString("username"), rs.getString("full_name"),
                    rs.getString("email"), rs.getString("role"), rs.getString("created_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    // ══════════════════════════════════════════════════════════════════
    //  BOOK MANAGEMENT
    // ══════════════════════════════════════════════════════════════════
    public boolean addBook(String title, String author, String isbn, String category, int qty) {
        String sql = "INSERT INTO books (title, author, isbn, category, total_qty, available_qty) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setString(4, category);
            ps.setInt(5, qty);
            ps.setInt(6, qty);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateBook(int id, String title, String author, String isbn, String category, int totalQty) {
        // Get current issued count
        int issuedCount = 0;
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM issued_books WHERE book_id = ? AND status = 'ISSUED'")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) issuedCount = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }

        int newAvailable = totalQty - issuedCount;
        if (newAvailable < 0) return false;

        String sql = "UPDATE books SET title=?, author=?, isbn=?, category=?, total_qty=?, available_qty=? WHERE id=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setString(4, category);
            ps.setInt(5, totalQty);
            ps.setInt(6, newAvailable);
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteBook(int id) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM books WHERE id = ? AND available_qty = total_qty")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Object[]> getAllBooks() {
        List<Object[]> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY id";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Object[]{
                    rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                    rs.getString("isbn"), rs.getString("category"),
                    rs.getInt("total_qty"), rs.getInt("available_qty"), rs.getString("added_date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public List<Object[]> searchBooks(String query, String filterBy) {
        List<Object[]> books = new ArrayList<>();
        String sql;
        switch (filterBy) {
            case "Title":
                sql = "SELECT * FROM books WHERE title LIKE ? ORDER BY title";
                break;
            case "Author":
                sql = "SELECT * FROM books WHERE author LIKE ? ORDER BY author";
                break;
            case "ID":
                sql = "SELECT * FROM books WHERE CAST(id AS TEXT) LIKE ? ORDER BY id";
                break;
            case "ISBN":
                sql = "SELECT * FROM books WHERE isbn LIKE ? ORDER BY isbn";
                break;
            case "Category":
                sql = "SELECT * FROM books WHERE category LIKE ? ORDER BY category";
                break;
            default:
                sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ? ORDER BY title";
                break;
        }
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String param = "%" + query + "%";
            ps.setString(1, param);
            if (filterBy.equals("All")) {
                ps.setString(2, param);
                ps.setString(3, param);
                ps.setString(4, param);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(new Object[]{
                    rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                    rs.getString("isbn"), rs.getString("category"),
                    rs.getInt("total_qty"), rs.getInt("available_qty"), rs.getString("added_date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    // ══════════════════════════════════════════════════════════════════
    //  ISSUE & RETURN OPERATIONS
    // ══════════════════════════════════════════════════════════════════
    public String issueBook(int bookId, int userId, int dueDays) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            // Check if book is available
            PreparedStatement checkBook = conn.prepareStatement(
                "SELECT available_qty, title FROM books WHERE id = ?");
            checkBook.setInt(1, bookId);
            ResultSet rs = checkBook.executeQuery();
            if (!rs.next()) { conn.rollback(); return "Book not found!"; }
            if (rs.getInt("available_qty") <= 0) { conn.rollback(); return "Book is not available!"; }
            String bookTitle = rs.getString("title");

            // Check if user exists
            PreparedStatement checkUser = conn.prepareStatement("SELECT full_name FROM users WHERE id = ?");
            checkUser.setInt(1, userId);
            ResultSet userRs = checkUser.executeQuery();
            if (!userRs.next()) { conn.rollback(); return "User not found!"; }

            // Check if already issued to this user
            PreparedStatement checkIssued = conn.prepareStatement(
                "SELECT id FROM issued_books WHERE book_id = ? AND user_id = ? AND status = 'ISSUED'");
            checkIssued.setInt(1, bookId);
            checkIssued.setInt(2, userId);
            if (checkIssued.executeQuery().next()) {
                conn.rollback(); return "This book is already issued to this user!";
            }

            // Issue the book
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(dueDays);

            PreparedStatement issue = conn.prepareStatement(
                "INSERT INTO issued_books (book_id, user_id, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ISSUED')");
            issue.setInt(1, bookId);
            issue.setInt(2, userId);
            issue.setString(3, today.toString());
            issue.setString(4, dueDate.toString());
            issue.executeUpdate();

            // Decrement available quantity
            PreparedStatement updateQty = conn.prepareStatement(
                "UPDATE books SET available_qty = available_qty - 1 WHERE id = ?");
            updateQty.setInt(1, bookId);
            updateQty.executeUpdate();

            conn.commit();
            return "SUCCESS: \"" + bookTitle + "\" issued successfully! Due date: " + dueDate;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }

    public String returnBook(int issueId) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            // Get issue details
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ib.*, b.title FROM issued_books ib JOIN books b ON ib.book_id = b.id WHERE ib.id = ? AND ib.status = 'ISSUED'");
            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { conn.rollback(); return "Issue record not found or already returned!"; }

            int bookId = rs.getInt("book_id");
            String bookTitle = rs.getString("title");
            LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
            LocalDate today = LocalDate.now();

            // Calculate fine (₹10/day for late returns)
            double fine = 0;
            long daysLate = ChronoUnit.DAYS.between(dueDate, today);
            if (daysLate > 0) {
                fine = daysLate * 10.0;
            }

            // Update issue record
            PreparedStatement updateIssue = conn.prepareStatement(
                "UPDATE issued_books SET return_date = ?, fine = ?, status = 'RETURNED' WHERE id = ?");
            updateIssue.setString(1, today.toString());
            updateIssue.setDouble(2, fine);
            updateIssue.setInt(3, issueId);
            updateIssue.executeUpdate();

            // Increment available quantity
            PreparedStatement updateQty = conn.prepareStatement(
                "UPDATE books SET available_qty = available_qty + 1 WHERE id = ?");
            updateQty.setInt(1, bookId);
            updateQty.executeUpdate();

            conn.commit();

            if (fine > 0) {
                return "SUCCESS: \"" + bookTitle + "\" returned. Late by " + daysLate + " days. Fine: ₹" + String.format("%.0f", fine);
            }
            return "SUCCESS: \"" + bookTitle + "\" returned successfully. No fine.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  ISSUED / RETURNED BOOK QUERIES
    // ══════════════════════════════════════════════════════════════════
    public List<Object[]> getIssuedBooks() {
        return getTransactionsByStatus("ISSUED");
    }

    public List<Object[]> getReturnedBooks() {
        return getTransactionsByStatus("RETURNED");
    }

    public List<Object[]> getAllTransactions() {
        List<Object[]> records = new ArrayList<>();
        String sql = "SELECT ib.id, b.title, u.full_name, u.username, ib.issue_date, ib.due_date, "
            + "ib.return_date, ib.fine, ib.status "
            + "FROM issued_books ib "
            + "JOIN books b ON ib.book_id = b.id "
            + "JOIN users u ON ib.user_id = u.id "
            + "ORDER BY ib.id DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(new Object[]{
                    rs.getInt("id"), rs.getString("title"), rs.getString("full_name"),
                    rs.getString("username"), rs.getString("issue_date"),
                    rs.getString("due_date"), rs.getString("return_date"),
                    rs.getDouble("fine"), rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    private List<Object[]> getTransactionsByStatus(String status) {
        List<Object[]> records = new ArrayList<>();
        String sql = "SELECT ib.id, b.id as book_id, b.title, u.full_name, u.username, "
            + "ib.issue_date, ib.due_date, ib.return_date, ib.fine, ib.status "
            + "FROM issued_books ib "
            + "JOIN books b ON ib.book_id = b.id "
            + "JOIN users u ON ib.user_id = u.id "
            + "WHERE ib.status = ? ORDER BY ib.id DESC";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(new Object[]{
                    rs.getInt("id"), rs.getInt("book_id"), rs.getString("title"),
                    rs.getString("full_name"), rs.getString("username"),
                    rs.getString("issue_date"), rs.getString("due_date"),
                    rs.getString("return_date"), rs.getDouble("fine"), rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    // For a specific user
    public List<Object[]> getUserIssuedBooks(int userId) {
        List<Object[]> records = new ArrayList<>();
        String sql = "SELECT ib.id, b.title, b.author, ib.issue_date, ib.due_date, ib.status "
            + "FROM issued_books ib JOIN books b ON ib.book_id = b.id "
            + "WHERE ib.user_id = ? AND ib.status = 'ISSUED' ORDER BY ib.due_date";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocalDate due = LocalDate.parse(rs.getString("due_date"));
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due);
                double pendingFine = daysLeft < 0 ? Math.abs(daysLeft) * 10.0 : 0;
                records.add(new Object[]{
                    rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                    rs.getString("issue_date"), rs.getString("due_date"),
                    daysLeft, pendingFine
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    public List<Object[]> getUserHistory(int userId) {
        List<Object[]> records = new ArrayList<>();
        String sql = "SELECT ib.id, b.title, ib.issue_date, ib.due_date, ib.return_date, ib.fine, ib.status "
            + "FROM issued_books ib JOIN books b ON ib.book_id = b.id "
            + "WHERE ib.user_id = ? ORDER BY ib.id DESC";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(new Object[]{
                    rs.getInt("id"), rs.getString("title"), rs.getString("issue_date"),
                    rs.getString("due_date"), rs.getString("return_date"),
                    rs.getDouble("fine"), rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return records;
    }

    // ══════════════════════════════════════════════════════════════════
    //  DASHBOARD STATISTICS
    // ══════════════════════════════════════════════════════════════════
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM books");
            stats.put("totalBooks", rs1.next() ? rs1.getInt(1) : 0);

            ResultSet rs2 = stmt.executeQuery("SELECT COALESCE(SUM(total_qty), 0) FROM books");
            stats.put("totalCopies", rs2.next() ? rs2.getInt(1) : 0);

            ResultSet rs3 = stmt.executeQuery("SELECT COALESCE(SUM(available_qty), 0) FROM books");
            stats.put("availableCopies", rs3.next() ? rs3.getInt(1) : 0);

            ResultSet rs4 = stmt.executeQuery("SELECT COUNT(*) FROM issued_books WHERE status = 'ISSUED'");
            stats.put("issuedBooks", rs4.next() ? rs4.getInt(1) : 0);

            ResultSet rs5 = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'user'");
            stats.put("totalUsers", rs5.next() ? rs5.getInt(1) : 0);

            ResultSet rs6 = stmt.executeQuery("SELECT COUNT(*) FROM issued_books WHERE status = 'RETURNED'");
            stats.put("returnedBooks", rs6.next() ? rs6.getInt(1) : 0);

            ResultSet rs7 = stmt.executeQuery(
                "SELECT COALESCE(SUM(fine), 0) FROM issued_books WHERE status = 'RETURNED'");
            stats.put("totalFines", rs7.next() ? rs7.getInt(1) : 0);

            // Overdue count
            ResultSet rs8 = stmt.executeQuery(
                "SELECT COUNT(*) FROM issued_books WHERE status = 'ISSUED' AND due_date < date('now')");
            stats.put("overdueBooks", rs8.next() ? rs8.getInt(1) : 0);

        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    public List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        cats.add("All");
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT category FROM books WHERE category IS NOT NULL ORDER BY category")) {
            while (rs.next()) cats.add(rs.getString("category"));
        } catch (SQLException e) { e.printStackTrace(); }
        return cats;
    }
}