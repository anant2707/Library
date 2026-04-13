import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {

    private final Map<String, String> currentUser;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private String activePage = "dashboard";

    // Table models
    private DefaultTableModel bookTableModel, userTableModel, issuedTableModel,
                              returnedTableModel, allTransTableModel;

    public AdminDashboard(Map<String, String> user) {
        this.currentUser = user;
        setTitle("📚 Admin Dashboard — Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 650));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIComponents.PRIMARY_DARK);

        // ── Sidebar ──────────────────────────────────────────
        sidebarPanel = buildSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // ── Content Area ─────────────────────────────────────
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIComponents.PRIMARY_DARK);

        contentPanel.add(buildDashboardPage(), "dashboard");
        contentPanel.add(buildBooksPage(), "books");
        contentPanel.add(buildUsersPage(), "users");
        contentPanel.add(buildIssuePage(), "issue");
        contentPanel.add(buildReturnPage(), "return");
        contentPanel.add(buildTransactionsPage(), "transactions");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIComponents.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 8, 15, 8));

        // Brand
        JLabel brand = new JLabel("  📚 LibManager");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(UIComponents.ACCENT);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(10, 12, 5, 0));

        JLabel role = new JLabel("  Admin Panel");
        role.setFont(UIComponents.FONT_SMALL);
        role.setForeground(UIComponents.TEXT_SECONDARY);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        role.setBorder(BorderFactory.createEmptyBorder(0, 12, 15, 0));

        sidebar.add(brand);
        sidebar.add(role);
        sidebar.add(UIComponents.createSeparator());
        sidebar.add(Box.createVerticalStrut(10));

        String[] pages = {"dashboard", "books", "users", "issue", "return", "transactions"};
        String[] labels = {"📊  Dashboard", "📖  Manage Books", "👥  Manage Users",
                           "📤  Issue Book", "📥  Return Book", "📋  Transactions"};

        for (int i = 0; i < pages.length; i++) {
            final String page = pages[i];
            JButton btn = UIComponents.createSidebarButton(labels[i], page.equals(activePage));
            btn.addActionListener(e -> switchPage(page));
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(3));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(UIComponents.createSeparator());
        sidebar.add(Box.createVerticalStrut(8));

        // User info
        JLabel userName = new JLabel("  👤 " + currentUser.get("full_name"));
        userName.setFont(UIComponents.FONT_SMALL);
        userName.setForeground(UIComponents.TEXT_SECONDARY);
        userName.setAlignmentX(Component.LEFT_ALIGNMENT);
        userName.setBorder(BorderFactory.createEmptyBorder(0, 12, 5, 0));
        sidebar.add(userName);

        JButton logoutBtn = UIComponents.createDangerButton("  Logout");
        logoutBtn.setMaximumSize(new Dimension(200, 36));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void switchPage(String page) {
        activePage = page;
        cardLayout.show(contentPanel, page);
        refreshData();

        // Rebuild sidebar to update active state
        Container parent = sidebarPanel.getParent();
        parent.remove(sidebarPanel);
        sidebarPanel = buildSidebar();
        parent.add(sidebarPanel, BorderLayout.WEST);
        parent.revalidate();
        parent.repaint();
    }

    private void refreshData() {
        refreshBookTable();
        refreshUserTable();
        refreshIssuedTable();
        refreshReturnedTable();
        refreshAllTransTable();
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: DASHBOARD
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Title
        JLabel title = new JLabel("📊 Dashboard Overview");
        title.setFont(UIComponents.FONT_TITLE);
        title.setForeground(UIComponents.TEXT_PRIMARY);

        Map<String, Integer> stats = db.getStats();

        // Stats cards in a row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);
        statsRow.add(UIComponents.createStatCard("TOTAL BOOKS", String.valueOf(stats.getOrDefault("totalBooks", 0)), UIComponents.ACCENT));
        statsRow.add(UIComponents.createStatCard("BOOKS ISSUED", String.valueOf(stats.getOrDefault("issuedBooks", 0)), UIComponents.WARNING));
        statsRow.add(UIComponents.createStatCard("REGISTERED USERS", String.valueOf(stats.getOrDefault("totalUsers", 0)), UIComponents.SUCCESS));
        statsRow.add(UIComponents.createStatCard("OVERDUE BOOKS", String.valueOf(stats.getOrDefault("overdueBooks", 0)), UIComponents.DANGER));

        JPanel statsRow2 = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow2.setOpaque(false);
        statsRow2.add(UIComponents.createStatCard("TOTAL COPIES", String.valueOf(stats.getOrDefault("totalCopies", 0)), UIComponents.ACCENT));
        statsRow2.add(UIComponents.createStatCard("AVAILABLE COPIES", String.valueOf(stats.getOrDefault("availableCopies", 0)), UIComponents.SUCCESS));
        statsRow2.add(UIComponents.createStatCard("RETURNED BOOKS", String.valueOf(stats.getOrDefault("returnedBooks", 0)), new Color(155, 89, 182)));
        statsRow2.add(UIComponents.createStatCard("TOTAL FINES (₹)", String.valueOf(stats.getOrDefault("totalFines", 0)), UIComponents.DANGER));

        // Refresh button
        JButton refreshBtn = UIComponents.createAccentButton("↻ Refresh Stats");
        refreshBtn.addActionListener(e -> switchPage("dashboard"));

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(title);
        topSection.add(Box.createVerticalStrut(20));
        topSection.add(statsRow);
        topSection.add(Box.createVerticalStrut(15));
        topSection.add(statsRow2);
        topSection.add(Box.createVerticalStrut(15));
        topSection.add(refreshBtn);

        page.add(topSection, BorderLayout.NORTH);

        // Recent transactions table
        JPanel tableSection = UIComponents.createCard();
        tableSection.setLayout(new BorderLayout(0, 10));
        JLabel recentLabel = UIComponents.createSectionLabel("📋 Recent Transactions");
        allTransTableModel = new DefaultTableModel(
            new String[]{"ID", "Book", "User", "Username", "Issue Date", "Due Date", "Return Date", "Fine (₹)", "Status"}, 0);
        JTable table = UIComponents.createStyledTable(allTransTableModel);
        refreshAllTransTable();
        tableSection.add(recentLabel, BorderLayout.NORTH);
        tableSection.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        page.add(tableSection, BorderLayout.CENTER);
        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: MANAGE BOOKS
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildBooksPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Title + Search Bar
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setOpaque(false);

        JLabel title = UIComponents.createSectionLabel("📖 Manage Books");
        topBar.add(title, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);

        JComboBox<String> filterCombo = UIComponents.createComboBox(
            new String[]{"All", "Title", "Author", "ID", "ISBN", "Category"});
        JTextField searchField = UIComponents.createTextField("Search books...");
        searchField.setPreferredSize(new Dimension(220, 38));
        JButton searchBtn = UIComponents.createAccentButton("🔍 Search");

        searchBar.add(new JLabel("Filter:") {{ setForeground(UIComponents.TEXT_SECONDARY); setFont(UIComponents.FONT_BODY); }});
        searchBar.add(filterCombo);
        searchBar.add(searchField);
        searchBar.add(searchBtn);

        topBar.add(searchBar, BorderLayout.EAST);
        page.add(topBar, BorderLayout.NORTH);

        // Table
        bookTableModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "ISBN", "Category", "Total Qty", "Available", "Added"}, 0);
        JTable bookTable = UIComponents.createStyledTable(bookTableModel);
        refreshBookTable();

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIComponents.createTableScrollPane(bookTable), BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        // Bottom buttons + Add form
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setOpaque(false);

        // Add Book form
        JPanel formCard = UIComponents.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.add(UIComponents.createSectionLabel("➕ Add New Book"));
        formCard.add(Box.createVerticalStrut(10));

        JTextField titleField = UIComponents.createTextField("Book Title");
        JTextField authorField = UIComponents.createTextField("Author Name");
        JTextField isbnField = UIComponents.createTextField("ISBN Number");
        JTextField categoryField = UIComponents.createTextField("Category");
        JTextField qtyField = UIComponents.createTextField("Quantity");

        formCard.add(UIComponents.createFormRow("Title:", titleField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Author:", authorField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("ISBN:", isbnField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Category:", categoryField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Quantity:", qtyField));
        formCard.add(Box.createVerticalStrut(10));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        JButton addBtn = UIComponents.createSuccessButton("➕ Add Book");
        JButton editBtn = UIComponents.createWarningButton("✏ Update");
        JButton deleteBtn = UIComponents.createDangerButton("🗑 Delete");
        JButton clearBtn = UIComponents.createButton("Clear", UIComponents.INPUT_BG, UIComponents.TEXT_PRIMARY);

        btnRow.add(addBtn);
        btnRow.add(editBtn);
        btnRow.add(deleteBtn);
        btnRow.add(clearBtn);
        formCard.add(btnRow);

        bottomPanel.add(formCard, BorderLayout.CENTER);
        page.add(bottomPanel, BorderLayout.SOUTH);

        // ── Table row selection → fills form ──
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row >= 0) {
                titleField.setText(bookTableModel.getValueAt(row, 1).toString());
                authorField.setText(bookTableModel.getValueAt(row, 2).toString());
                isbnField.setText(bookTableModel.getValueAt(row, 3) != null ? bookTableModel.getValueAt(row, 3).toString() : "");
                categoryField.setText(bookTableModel.getValueAt(row, 4) != null ? bookTableModel.getValueAt(row, 4).toString() : "");
                qtyField.setText(bookTableModel.getValueAt(row, 5).toString());
            }
        });

        // ── Actions ──
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            String filter = filterCombo.getSelectedItem().toString();
            bookTableModel.setRowCount(0);
            List<Object[]> results = query.isEmpty() ? db.getAllBooks() : db.searchBooks(query, filter);
            for (Object[] r : results) bookTableModel.addRow(r);
        });
        searchField.addActionListener(e -> searchBtn.doClick());

        addBtn.addActionListener(e -> {
            try {
                if (titleField.getText().trim().isEmpty() || authorField.getText().trim().isEmpty()) {
                    UIComponents.showError(this, "Title and Author are required!"); return;
                }
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (db.addBook(titleField.getText().trim(), authorField.getText().trim(),
                        isbnField.getText().trim(), categoryField.getText().trim(), qty)) {
                    UIComponents.showSuccess(this, "Book added successfully!");
                    refreshBookTable();
                    clearBtn.doClick();
                } else {
                    UIComponents.showError(this, "Failed to add book. ISBN might be duplicate.");
                }
            } catch (NumberFormatException ex) {
                UIComponents.showError(this, "Please enter a valid quantity.");
            }
        });

        editBtn.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row < 0) { UIComponents.showError(this, "Select a book to update!"); return; }
            try {
                int id = (int) bookTableModel.getValueAt(row, 0);
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (db.updateBook(id, titleField.getText().trim(), authorField.getText().trim(),
                        isbnField.getText().trim(), categoryField.getText().trim(), qty)) {
                    UIComponents.showSuccess(this, "Book updated successfully!");
                    refreshBookTable();
                } else {
                    UIComponents.showError(this, "Update failed. Ensure quantity ≥ issued copies.");
                }
            } catch (NumberFormatException ex) {
                UIComponents.showError(this, "Please enter a valid quantity.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row < 0) { UIComponents.showError(this, "Select a book to delete!"); return; }
            int id = (int) bookTableModel.getValueAt(row, 0);
            if (UIComponents.showConfirm(this, "Delete this book? (Only works if all copies returned)")) {
                if (db.deleteBook(id)) {
                    UIComponents.showSuccess(this, "Book deleted.");
                    refreshBookTable(); clearBtn.doClick();
                } else {
                    UIComponents.showError(this, "Cannot delete — some copies are still issued.");
                }
            }
        });

        clearBtn.addActionListener(e -> {
            titleField.setText(""); authorField.setText(""); isbnField.setText("");
            categoryField.setText(""); qtyField.setText("");
            bookTable.clearSelection();
        });

        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: MANAGE USERS
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildUsersPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = UIComponents.createSectionLabel("👥 Manage Users");
        page.add(title, BorderLayout.NORTH);

        // Table
        userTableModel = new DefaultTableModel(
            new String[]{"ID", "Username", "Full Name", "Email", "Role", "Created"}, 0);
        JTable userTable = UIComponents.createStyledTable(userTableModel);
        refreshUserTable();

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIComponents.createTableScrollPane(userTable), BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        // Add User form
        JPanel formCard = UIComponents.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.add(UIComponents.createSectionLabel("➕ Add / Edit User"));
        formCard.add(Box.createVerticalStrut(10));

        JTextField unameField = UIComponents.createTextField("Username");
        JPasswordField passField = UIComponents.createPasswordField("Password");
        JTextField fnameField = UIComponents.createTextField("Full Name");
        JTextField emailField = UIComponents.createTextField("Email");
        JComboBox<String> roleCombo = UIComponents.createComboBox(new String[]{"user", "admin"});

        formCard.add(UIComponents.createFormRow("Username:", unameField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Password:", passField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Full Name:", fnameField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Email:", emailField));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(UIComponents.createFormRow("Role:", roleCombo));
        formCard.add(Box.createVerticalStrut(10));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        JButton addBtn = UIComponents.createSuccessButton("➕ Add User");
        JButton editBtn = UIComponents.createWarningButton("✏ Update");
        JButton deleteBtn = UIComponents.createDangerButton("🗑 Delete");
        JButton clearBtn = UIComponents.createButton("Clear", UIComponents.INPUT_BG, UIComponents.TEXT_PRIMARY);
        btnRow.add(addBtn); btnRow.add(editBtn); btnRow.add(deleteBtn); btnRow.add(clearBtn);
        formCard.add(btnRow);

        page.add(formCard, BorderLayout.SOUTH);

        // Fill form on table select
        userTable.getSelectionModel().addListSelectionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                unameField.setText(userTableModel.getValueAt(row, 1).toString());
                fnameField.setText(userTableModel.getValueAt(row, 2).toString());
                emailField.setText(userTableModel.getValueAt(row, 3) != null ? userTableModel.getValueAt(row, 3).toString() : "");
                roleCombo.setSelectedItem(userTableModel.getValueAt(row, 4).toString());
                passField.setText("");
            }
        });

        addBtn.addActionListener(e -> {
            String uname = unameField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String fname = fnameField.getText().trim();
            if (uname.isEmpty() || pass.isEmpty() || fname.isEmpty()) {
                UIComponents.showError(this, "Username, Password, and Full Name are required!"); return;
            }
            if (db.addUser(uname, pass, fname, emailField.getText().trim(), roleCombo.getSelectedItem().toString())) {
                UIComponents.showSuccess(this, "User added successfully!");
                refreshUserTable(); clearBtn.doClick();
            } else {
                UIComponents.showError(this, "Failed. Username might be taken.");
            }
        });

        editBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) { UIComponents.showError(this, "Select a user!"); return; }
            int id = (int) userTableModel.getValueAt(row, 0);
            String pass = new String(passField.getPassword()).trim();
            if (db.updateUser(id, fnameField.getText().trim(), emailField.getText().trim(), pass)) {
                UIComponents.showSuccess(this, "User updated!"); refreshUserTable();
            } else {
                UIComponents.showError(this, "Update failed.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) { UIComponents.showError(this, "Select a user!"); return; }
            int id = (int) userTableModel.getValueAt(row, 0);
            if (UIComponents.showConfirm(this, "Delete this user?")) {
                if (db.deleteUser(id)) {
                    UIComponents.showSuccess(this, "User deleted."); refreshUserTable(); clearBtn.doClick();
                } else {
                    UIComponents.showError(this, "Cannot delete admin accounts.");
                }
            }
        });

        clearBtn.addActionListener(e -> {
            unameField.setText(""); passField.setText(""); fnameField.setText(""); emailField.setText("");
            userTable.clearSelection();
        });

        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: ISSUE BOOK
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildIssuePage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = UIComponents.createSectionLabel("📤 Issue Book");
        page.add(title, BorderLayout.NORTH);

        // Issue form
        JPanel formCard = UIComponents.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JTextField bookIdField = UIComponents.createTextField("Enter Book ID");
        JTextField userIdField = UIComponents.createTextField("Enter User ID");
        JTextField daysField = UIComponents.createTextField("No. of Days (default: 14)");
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(UIComponents.FONT_BODY);
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(UIComponents.createFormRow("Book ID:", bookIdField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(UIComponents.createFormRow("User ID:", userIdField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(UIComponents.createFormRow("Due Days:", daysField));
        formCard.add(Box.createVerticalStrut(15));

        JButton issueBtn = UIComponents.createSuccessButton("📤 Issue Book");
        issueBtn.setPreferredSize(new Dimension(180, 44));
        formCard.add(issueBtn);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(resultLabel);

        page.add(formCard, BorderLayout.WEST);

        // Currently issued table
        issuedTableModel = new DefaultTableModel(
            new String[]{"Issue ID", "Book ID", "Book Title", "User", "Username", "Issue Date", "Due Date", "Return", "Fine (₹)", "Status"}, 0);
        JTable issuedTable = UIComponents.createStyledTable(issuedTableModel);
        refreshIssuedTable();

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UIComponents.createSectionLabel("📋 Currently Issued Books"), BorderLayout.NORTH);
        tableCard.add(UIComponents.createTableScrollPane(issuedTable), BorderLayout.CENTER);

        page.add(tableCard, BorderLayout.CENTER);

        // Action
        issueBtn.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                int userId = Integer.parseInt(userIdField.getText().trim());
                int days = daysField.getText().trim().isEmpty() ? 14 : Integer.parseInt(daysField.getText().trim());

                String result = db.issueBook(bookId, userId, days);
                if (result.startsWith("SUCCESS")) {
                    resultLabel.setForeground(UIComponents.SUCCESS);
                    resultLabel.setText("✓ " + result.substring(9));
                    refreshIssuedTable();
                    bookIdField.setText(""); userIdField.setText(""); daysField.setText("");
                } else {
                    resultLabel.setForeground(UIComponents.DANGER);
                    resultLabel.setText("✗ " + result);
                }
            } catch (NumberFormatException ex) {
                resultLabel.setForeground(UIComponents.DANGER);
                resultLabel.setText("✗ Please enter valid numeric IDs.");
            }
        });

        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: RETURN BOOK
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildReturnPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = UIComponents.createSectionLabel("📥 Return Book");
        page.add(title, BorderLayout.NORTH);

        // Return form
        JPanel formCard = UIComponents.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(350, 0));

        JTextField issueIdField = UIComponents.createTextField("Enter Issue ID");
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(UIComponents.FONT_BODY);
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("<html><i>Select a row from the table or enter Issue ID manually.<br>"
            + "Fine: ₹10/day for late returns.</i></html>");
        hint.setFont(UIComponents.FONT_SMALL);
        hint.setForeground(UIComponents.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(UIComponents.createFormRow("Issue ID:", issueIdField));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(hint);
        formCard.add(Box.createVerticalStrut(15));

        JButton returnBtn = UIComponents.createSuccessButton("📥 Return Book");
        returnBtn.setPreferredSize(new Dimension(180, 44));
        formCard.add(returnBtn);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(resultLabel);

        page.add(formCard, BorderLayout.WEST);

        // Issued books table (click to select issue ID)
        DefaultTableModel returnTableModel = new DefaultTableModel(
            new String[]{"Issue ID", "Book ID", "Book Title", "User", "Username", "Issue Date", "Due Date", "Return", "Fine (₹)", "Status"}, 0);
        JTable returnTable = UIComponents.createStyledTable(returnTableModel);

        // Populate with issued books
        List<Object[]> issued = db.getIssuedBooks();
        for (Object[] r : issued) returnTableModel.addRow(r);

        returnTable.getSelectionModel().addListSelectionListener(e -> {
            int row = returnTable.getSelectedRow();
            if (row >= 0) {
                issueIdField.setText(returnTableModel.getValueAt(row, 0).toString());
            }
        });

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UIComponents.createSectionLabel("📋 Books Available for Return (Currently Issued)"), BorderLayout.NORTH);
        tableCard.add(UIComponents.createTableScrollPane(returnTable), BorderLayout.CENTER);

        page.add(tableCard, BorderLayout.CENTER);

        // Action
        returnBtn.addActionListener(e -> {
            try {
                int issueId = Integer.parseInt(issueIdField.getText().trim());
                String result = db.returnBook(issueId);
                if (result.startsWith("SUCCESS")) {
                    resultLabel.setForeground(UIComponents.SUCCESS);
                    resultLabel.setText("✓ " + result.substring(9));
                    // Refresh table
                    returnTableModel.setRowCount(0);
                    for (Object[] r : db.getIssuedBooks()) returnTableModel.addRow(r);
                    issueIdField.setText("");
                } else {
                    resultLabel.setForeground(UIComponents.DANGER);
                    resultLabel.setText("✗ " + result);
                }
            } catch (NumberFormatException ex) {
                resultLabel.setForeground(UIComponents.DANGER);
                resultLabel.setText("✗ Enter a valid Issue ID.");
            }
        });

        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: ALL TRANSACTIONS
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildTransactionsPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(UIComponents.createSectionLabel("📋 All Transactions"), BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);
        JComboBox<String> statusFilter = UIComponents.createComboBox(new String[]{"All", "ISSUED", "RETURNED"});
        JButton filterBtn = UIComponents.createAccentButton("Filter");
        filterPanel.add(new JLabel("Status:") {{ setForeground(UIComponents.TEXT_SECONDARY); setFont(UIComponents.FONT_BODY); }});
        filterPanel.add(statusFilter);
        filterPanel.add(filterBtn);
        topBar.add(filterPanel, BorderLayout.EAST);

        page.add(topBar, BorderLayout.NORTH);

        // Table
        DefaultTableModel transModel = new DefaultTableModel(
            new String[]{"ID", "Book", "User", "Username", "Issue Date", "Due Date", "Return Date", "Fine (₹)", "Status"}, 0);
        JTable transTable = UIComponents.createStyledTable(transModel);

        List<Object[]> all = db.getAllTransactions();
        for (Object[] r : all) transModel.addRow(r);

        // Custom cell renderer for status column
        transTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                if (!isSelected) {
                    if ("ISSUED".equals(value)) {
                        setForeground(UIComponents.WARNING);
                    } else if ("RETURNED".equals(value)) {
                        setForeground(UIComponents.SUCCESS);
                    }
                }
                return this;
            }
        });

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIComponents.createTableScrollPane(transTable), BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        filterBtn.addActionListener(e -> {
            transModel.setRowCount(0);
            String filter = statusFilter.getSelectedItem().toString();
            List<Object[]> data;
            if ("ISSUED".equals(filter)) data = db.getIssuedBooks();
            else if ("RETURNED".equals(filter)) data = db.getReturnedBooks();
            else data = db.getAllTransactions();

            if ("All".equals(filter)) {
                for (Object[] r : data) transModel.addRow(r);
            } else {
                for (Object[] r : data) {
                    // Issued/Returned queries return 10 columns, transaction query returns 9
                    if (r.length == 10) {
                        transModel.addRow(new Object[]{r[0], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9]});
                    } else {
                        transModel.addRow(r);
                    }
                }
            }
        });

        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  TABLE REFRESH METHODS
    // ══════════════════════════════════════════════════════════════════
    private void refreshBookTable() {
        if (bookTableModel == null) return;
        bookTableModel.setRowCount(0);
        for (Object[] r : db.getAllBooks()) bookTableModel.addRow(r);
    }

    private void refreshUserTable() {
        if (userTableModel == null) return;
        userTableModel.setRowCount(0);
        for (Object[] r : db.getAllUsers()) userTableModel.addRow(r);
    }

    private void refreshIssuedTable() {
        if (issuedTableModel == null) return;
        issuedTableModel.setRowCount(0);
        for (Object[] r : db.getIssuedBooks()) issuedTableModel.addRow(r);
    }

    private void refreshReturnedTable() {
        if (returnedTableModel == null) return;
        returnedTableModel.setRowCount(0);
        for (Object[] r : db.getReturnedBooks()) returnedTableModel.addRow(r);
    }

    private void refreshAllTransTable() {
        if (allTransTableModel == null) return;
        allTransTableModel.setRowCount(0);
        for (Object[] r : db.getAllTransactions()) allTransTableModel.addRow(r);
    }
}