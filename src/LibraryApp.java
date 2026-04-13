import javax.swing.*;

public class LibraryApp {
    public static void main(String[] args) {
        // Initialize the database (creates tables + default admin)
        DatabaseManager.getInstance().initializeDatabase();

        // Set look-and-feel and launch login
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame();
        });
    }
}