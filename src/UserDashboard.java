import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class UserDashboard extends JFrame {

    private final Map<String, String> currentUser;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private String activePage = "dashboard";

    private DefaultTableModel myBooksModel, historyModel, catalogModel;

    public UserDashboard(Map<String, String> user) {
        this.currentUser = user;
        setTitle("📚 User Dashboard — Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIComponents.PRIMARY_DARK);

        sidebarPanel = buildSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIComponents.PRIMARY_DARK);

        contentPanel.add(buildDashboardPage(), "dashboard");
        contentPanel.add(buildCatalogPage(), "catalog");
        contentPanel.add(buildMyBooksPage(), "mybooks");
        contentPanel.add(buildHistoryPage(), "history");
        contentPanel.add(buildProfilePage(), "profile");

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

        JLabel brand = new JLabel("  📚 LibManager");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(UIComponents.ACCENT);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(10, 12, 5, 0));

        JLabel role = new JLabel("  Student Portal");
        role.setFont(UIComponents.FONT_SMALL);
        role.setForeground(UIComponents.TEXT_SECONDARY);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        role.setBorder(BorderFactory.createEmptyBorder(0, 12, 15, 0));

        sidebar.add(brand);
        sidebar.add(role);
        sidebar.add(UIComponents.createSeparator());
        sidebar.add(Box.createVerticalStrut(10));

        String[] pages = {"dashboard", "catalog", "mybooks", "history", "profile"};
        String[] labels = {"📊  Dashboard", "📖  Book Catalog", "📚  My Books",
                           "📋  History", "👤  Profile"};

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

        JLabel userName = new JLabel("  👤 " + currentUser.get("full_name"));
        userName.setFont(UIComponents.FONT_SMALL);
        userName.setForeground(UIComponents.TEXT_SECONDARY);
        userName.setAlignmentX(Component.LEFT_ALIGNMENT);
        userName.setBorder(BorderFactory.createEmptyBorder(0, 12, 5, 0));
        sidebar.add(userName);

        JLabel userId = new JLabel("  ID: " + currentUser.get("id"));
        userId.setFont(UIComponents.FONT_SMALL);
        userId.setForeground(UIComponents.TEXT_SECONDARY);
        userId.setAlignmentX(Component.LEFT_ALIGNMENT);
        userId.setBorder(BorderFactory.createEmptyBorder(0, 12, 5, 0));
        sidebar.add(userId);

        JButton logoutBtn = UIComponents.createDangerButton("  Logout");
        logoutBtn.setMaximumSize(new Dimension(200, 36));
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void switchPage(String page) {
        activePage = page;
        cardLayout.show(contentPanel, page);
        refreshData();

        Container parent = sidebarPanel.getParent();
        parent.remove(sidebarPanel);
        sidebarPanel = buildSidebar();
        parent.add(sidebarPanel, BorderLayout.WEST);
        parent.revalidate();
        parent.repaint();
    }

    private void refreshData() {
        refreshMyBooks();
        refreshHistory();
        refreshCatalog(null, "All");
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: DASHBOARD
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("👋 Welcome, " + currentUser.get("full_name") + "!");
        title.setFont(UIComponents.FONT_TITLE);
        title.setForeground(UIComponents.TEXT_PRIMARY);

        int userId = Integer.parseInt(currentUser.get("id"));
        List<Object[]> myBooks = db.getUserIssuedBooks(userId);

        // Stats
        int booksIssued = myBooks.size();
        double totalFine = 0;
        int overdue = 0;
        for (Object[] b : myBooks) {
            double fine = (double) b[6];
            totalFine += fine;
            long daysLeft = (long) b[5];
            if (daysLeft < 0) overdue++;
        }

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);
        statsRow.add(UIComponents.createStatCard("BOOKS WITH ME", String.valueOf(booksIssued), UIComponents.ACCENT));
        statsRow.add(UIComponents.createStatCard("OVERDUE", String.valueOf(overdue), UIComponents.DANGER));
        statsRow.add(UIComponents.createStatCard("PENDING FINE (₹)", String.format("%.0f", totalFine), UIComponents.WARNING));
        statsRow.add(UIComponents.createStatCard("MY USER ID", currentUser.get("id"), UIComponents.SUCCESS));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);
        top.add(Box.createVerticalStrut(20));
        top.add(statsRow);

        page.add(top, BorderLayout.NORTH);

        // My current books
        myBooksModel = new DefaultTableModel(
            new String[]{"Issue ID", "Title", "Author", "Issue Date", "Due Date", "Days Left", "Fine (₹)"}, 0);
        JTable table = UIComponents.createStyledTable(myBooksModel);
        refreshMyBooks();

        // Color code overdue rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                if (!isSelected) {
                    Object daysObj = table.getModel().getValueAt(row, 5);
                    long days = daysObj instanceof Long ? (Long) daysObj : 0;
                    if (days < 0) {
                        setBackground(new Color(255, 71, 87, 25));
                        if (column == 5 || column == 6) setForeground(UIComponents.DANGER);
                        else setForeground(UIComponents.TEXT_PRIMARY);
                    } else if (days <= 2) {
                        setBackground(new Color(255, 165, 2, 20));
                        setForeground(UIComponents.TEXT_PRIMARY);
                    } else {
                        setBackground(row % 2 == 0 ? UIComponents.PRIMARY : UIComponents.TABLE_ROW_ALT);
                        setForeground(UIComponents.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(UIComponents.createSectionLabel("📚 My Currently Issued Books"), BorderLayout.NORTH);
        tableCard.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        page.add(tableCard, BorderLayout.CENTER);
        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: BOOK CATALOG (Search & Browse)
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildCatalogPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Top: search + filter
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setOpaque(false);

        JLabel title = UIComponents.createSectionLabel("📖 Book Catalog");
        topBar.add(title, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);

        JComboBox<String> filterCombo = UIComponents.createComboBox(
            new String[]{"All", "Title", "Author", "ID", "ISBN", "Category"});
        JTextField searchField = UIComponents.createTextField("Search books...");
        searchField.setPreferredSize(new Dimension(230, 38));
        JButton searchBtn = UIComponents.createAccentButton("🔍 Search");
        JButton resetBtn = UIComponents.createButton("Reset", UIComponents.INPUT_BG, UIComponents.TEXT_PRIMARY);

        searchBar.add(new JLabel("Filter:") {{ setForeground(UIComponents.TEXT_SECONDARY); setFont(UIComponents.FONT_BODY); }});
        searchBar.add(filterCombo);
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(resetBtn);
        topBar.add(searchBar, BorderLayout.EAST);

        page.add(topBar, BorderLayout.NORTH);

        // Table
        catalogModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "ISBN", "Category", "Total", "Available", "Added"}, 0);
        JTable catalogTable = UIComponents.createStyledTable(catalogModel);
        refreshCatalog(null, "All");

        // Custom renderer: color-code availability
        catalogTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                if (!isSelected) {
                    int avail = value != null ? Integer.parseInt(value.toString()) : 0;
                    if (avail == 0) setForeground(UIComponents.DANGER);
                    else if (avail <= 2) setForeground(UIComponents.WARNING);
                    else setForeground(UIComponents.SUCCESS);
                    setBackground(row % 2 == 0 ? UIComponents.PRIMARY : UIComponents.TABLE_ROW_ALT);
                }
                return this;
            }
        });

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIComponents.createTableScrollPane(catalogTable), BorderLayout.CENTER);

        // Results count
        JLabel countLabel = new JLabel("Showing all books");
        countLabel.setFont(UIComponents.FONT_SMALL);
        countLabel.setForeground(UIComponents.TEXT_SECONDARY);
        countLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 0));
        tableCard.add(countLabel, BorderLayout.SOUTH);

        page.add(tableCard, BorderLayout.CENTER);

        // Actions
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            String filter = filterCombo.getSelectedItem().toString();
            catalogModel.setRowCount(0);
            List<Object[]> results = query.isEmpty() ? db.getAllBooks() : db.searchBooks(query, filter);
            for (Object[] r : results) catalogModel.addRow(r);
            countLabel.setText("Found " + results.size() + " book(s)");
        });
        searchField.addActionListener(e -> searchBtn.doClick());

        resetBtn.addActionListener(e -> {
            searchField.setText("");
            filterCombo.setSelectedIndex(0);
            refreshCatalog(null, "All");
            countLabel.setText("Showing all books");
        });

        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: MY BOOKS (Currently Issued)
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildMyBooksPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = UIComponents.createSectionLabel("📚 My Issued Books");
        page.add(title, BorderLayout.NORTH);

        if (myBooksModel == null) {
            myBooksModel = new DefaultTableModel(
                new String[]{"Issue ID", "Title", "Author", "Issue Date", "Due Date", "Days Left", "Fine (₹)"}, 0);
        }
        JTable table = UIComponents.createStyledTable(myBooksModel);
        refreshMyBooks();

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel info = new JLabel("<html>📌 <b>Fine Policy:</b> ₹10 per day for late returns  |  "
            + "⚠ Red rows = Overdue  |  🟡 Yellow rows = Due within 2 days</html>");
        info.setFont(UIComponents.FONT_SMALL);
        info.setForeground(UIComponents.TEXT_SECONDARY);

        tableCard.add(info, BorderLayout.NORTH);
        tableCard.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = UIComponents.createAccentButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refreshMyBooks());
        tableCard.add(refreshBtn, BorderLayout.SOUTH);

        page.add(tableCard, BorderLayout.CENTER);
        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: HISTORY
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildHistoryPage() {
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setBackground(UIComponents.PRIMARY_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = UIComponents.createSectionLabel("📋 Borrowing History");
        page.add(title, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
            new String[]{"ID", "Book Title", "Issue Date", "Due Date", "Return Date", "Fine (₹)", "Status"}, 0);
        JTable table = UIComponents.createStyledTable(historyModel);
        refreshHistory();

        // Status renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UIComponents.PRIMARY : UIComponents.TABLE_ROW_ALT);
                    if ("ISSUED".equals(value)) setForeground(UIComponents.WARNING);
                    else if ("RETURNED".equals(value)) setForeground(UIComponents.SUCCESS);
                }
                return this;
            }
        });

        JPanel tableCard = UIComponents.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        page.add(tableCard, BorderLayout.CENTER);
        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAGE: PROFILE
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildProfilePage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIComponents.PRIMARY_DARK);

        JPanel card = UIComponents.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(450, 420));

        JLabel title = UIComponents.createSectionLabel("👤 My Profile");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = UIComponents.createTextField(currentUser.get("full_name"));
        nameField.setText(currentUser.get("full_name"));

        JTextField emailField = UIComponents.createTextField(currentUser.getOrDefault("email", ""));
        emailField.setText(currentUser.getOrDefault("email", ""));

        JPasswordField passField = UIComponents.createPasswordField("New Password (leave blank to keep)");

        JLabel usernameLabel = UIComponents.createBodyLabel("Username: " + currentUser.get("username"));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel idLabel = UIComponents.createBodyLabel("User ID: " + currentUser.get("id"));
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = UIComponents.createBodyLabel("Role: " + currentUser.get("role").toUpperCase());
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(15));
        card.add(idLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(UIComponents.createFormRow("Full Name:", nameField));
        card.add(Box.createVerticalStrut(8));
        card.add(UIComponents.createFormRow("Email:", emailField));
        card.add(Box.createVerticalStrut(8));
        card.add(UIComponents.createFormRow("Password:", passField));
        card.add(Box.createVerticalStrut(20));

        JButton saveBtn = UIComponents.createSuccessButton("💾 Save Changes");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(saveBtn);

        JLabel result = new JLabel(" ");
        result.setFont(UIComponents.FONT_BODY);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(10));
        card.add(result);

        saveBtn.addActionListener(e -> {
            int id = Integer.parseInt(currentUser.get("id"));
            String pass = new String(passField.getPassword()).trim();
            if (db.updateUser(id, nameField.getText().trim(), emailField.getText().trim(), pass)) {
                currentUser.put("full_name", nameField.getText().trim());
                currentUser.put("email", emailField.getText().trim());
                result.setForeground(UIComponents.SUCCESS);
                result.setText("✓ Profile updated successfully!");
                passField.setText("");
            } else {
                result.setForeground(UIComponents.DANGER);
                result.setText("✗ Failed to update profile.");
            }
        });

        page.add(card);
        return page;
    }

    // ══════════════════════════════════════════════════════════════════
    //  REFRESH METHODS
    // ══════════════════════════════════════════════════════════════════
    private void refreshMyBooks() {
        if (myBooksModel == null) return;
        myBooksModel.setRowCount(0);
        int userId = Integer.parseInt(currentUser.get("id"));
        for (Object[] r : db.getUserIssuedBooks(userId)) myBooksModel.addRow(r);
    }

    private void refreshHistory() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        int userId = Integer.parseInt(currentUser.get("id"));
        for (Object[] r : db.getUserHistory(userId)) historyModel.addRow(r);
    }

    private void refreshCatalog(String query, String filter) {
        if (catalogModel == null) return;
        catalogModel.setRowCount(0);
        List<Object[]> books = (query == null || query.isEmpty()) ? db.getAllBooks() : db.searchBooks(query, filter);
        for (Object[] r : books) catalogModel.addRow(r);
    }
}