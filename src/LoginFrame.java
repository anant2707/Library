import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Map;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("📚 Library Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // ── LEFT: Branding Panel ─────────────────────────────
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(26, 32, 53),
                        getWidth(), getHeight(), new Color(86, 141, 229));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(80, 50, 80, 50));

        JLabel icon = new JLabel("📚");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("Library Manager");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("<html><center>Smart Automated Library<br>Management System</center></html>");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        brandSub.setForeground(new Color(200, 215, 245));
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel features = new JLabel("<html><center><br>✓ Issue & Return Books<br>"
            + "✓ Smart Search & Filter<br>✓ Auto Fine Calculation<br>"
            + "✓ Admin & User Dashboards<br>✓ Real-time Availability</center></html>");
        features.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        features.setForeground(new Color(180, 200, 240));
        features.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(icon);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(brandTitle);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(brandSub);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(features);
        leftPanel.add(Box.createVerticalGlue());

        // ── RIGHT: Login Form ────────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(UIComponents.PRIMARY_DARK);
        rightPanel.setLayout(new GridBagLayout());

        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setPreferredSize(new Dimension(320, 380));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(UIComponents.FONT_TITLE);
        loginTitle.setForeground(UIComponents.TEXT_PRIMARY);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginSub = new JLabel("Sign in to your account");
        loginSub.setFont(UIComponents.FONT_BODY);
        loginSub.setForeground(UIComponents.TEXT_SECONDARY);
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userLabel.setForeground(UIComponents.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = UIComponents.createTextField("Enter username");
        usernameField.setMaximumSize(new Dimension(320, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passLabel.setForeground(UIComponents.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = UIComponents.createPasswordField("Enter password");
        passwordField.setMaximumSize(new Dimension(320, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UIComponents.createAccentButton("    Sign In    ");
        loginBtn.setMaximumSize(new Dimension(320, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIComponents.FONT_SMALL);
        statusLabel.setForeground(UIComponents.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel defaultCreds = new JLabel("<html><center>Default Admin → admin / admin123<br>"
            + "Default User → student1 / pass123</center></html>");
        defaultCreds.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        defaultCreds.setForeground(UIComponents.TEXT_SECONDARY);
        defaultCreds.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(loginTitle);
        formBox.add(Box.createVerticalStrut(5));
        formBox.add(loginSub);
        formBox.add(Box.createVerticalStrut(30));
        formBox.add(userLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(usernameField);
        formBox.add(Box.createVerticalStrut(18));
        formBox.add(passLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(10));
        formBox.add(statusLabel);
        formBox.add(Box.createVerticalStrut(15));
        formBox.add(loginBtn);
        formBox.add(Box.createVerticalStrut(25));
        formBox.add(defaultCreds);

        rightPanel.add(formBox);

        // ── Action Listener ──────────────────────────────────
        ActionListener loginAction = e -> performLogin();
        loginBtn.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        setContentPane(mainPanel);
        setVisible(true);

        usernameField.requestFocusInWindow();
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Please enter both username and password");
            return;
        }

        Map<String, String> user = DatabaseManager.getInstance().authenticate(username, password);

        if (user != null) {
            statusLabel.setForeground(UIComponents.SUCCESS);
            statusLabel.setText("✓ Login successful! Redirecting...");

            Timer timer = new Timer(500, e -> {
                dispose();
                if ("admin".equals(user.get("role"))) {
                    new AdminDashboard(user);
                } else {
                    new UserDashboard(user);
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            statusLabel.setForeground(UIComponents.DANGER);
            statusLabel.setText("✗ Invalid username or password!");
            passwordField.setText("");
        }
    }
}