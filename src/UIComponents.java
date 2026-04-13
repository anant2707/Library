import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Central UI styling factory — provides consistent, polished components
 * with a cool-yet-classic dark/navy theme for the entire application.
 */
public class UIComponents {

    // ── Color Palette ──────────────────────────────────────────────
    public static final Color PRIMARY_DARK    = new Color(26, 32, 53);    // #1A2035
    public static final Color PRIMARY         = new Color(33, 42, 68);    // #212A44
    public static final Color SIDEBAR         = new Color(22, 28, 48);    // #161C30
    public static final Color ACCENT          = new Color(86, 141, 229);  // #568DE5
    public static final Color ACCENT_HOVER    = new Color(110, 160, 240); // #6EA0F0
    public static final Color SUCCESS         = new Color(46, 213, 115);  // #2ED573
    public static final Color WARNING         = new Color(255, 165, 2);   // #FFA502
    public static final Color DANGER          = new Color(255, 71, 87);   // #FF4757
    public static final Color CARD_BG         = new Color(38, 48, 78);    // #26304E
    public static final Color INPUT_BG        = new Color(44, 55, 88);    // #2C3758
    public static final Color TEXT_PRIMARY    = new Color(236, 240, 250); // #ECF0FA
    public static final Color TEXT_SECONDARY  = new Color(160, 170, 200); // #A0AAC8
    public static final Color TABLE_ROW_ALT   = new Color(30, 38, 62);   // #1E263E
    public static final Color TABLE_HEADER    = new Color(50, 62, 100);   // #323E64
    public static final Color BORDER_COLOR    = new Color(55, 68, 108);   // #37446C
    public static final Color SEARCH_HIGHLIGHT= new Color(255, 215, 0, 40);

    // ── Fonts ──────────────────────────────────────────────────────
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING     = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HEAD  = new Font("Segoe UI", Font.BOLD, 13);

    // ── Rounded Button ──────────────────────────────────────────────
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));
        return btn;
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, ACCENT, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    public static JButton createWarningButton(String text) {
        return createButton(text, WARNING, Color.WHITE);
    }

    // ── Sidebar Button ──────────────────────────────────────────────
    public static JButton createSidebarButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active || getModel().isRollover()) {
                    g2.setColor(ACCENT);
                    g2.fill(new RoundRectangle2D.Float(4, 2, getWidth() - 8, getHeight() - 4, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(active ? Color.WHITE : TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return btn;
    }

    // ── Styled Text Field ────────────────────────────────────────────
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(TEXT_SECONDARY);
                    g3.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g3.drawString(placeholder, ins.left + 4, getHeight() / 2 + 5);
                    g3.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(250, 38));
        return field;
    }

    // ── Styled Password Field ────────────────────────────────────────
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(TEXT_SECONDARY);
                    g3.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g3.drawString(placeholder, ins.left + 4, getHeight() / 2 + 5);
                    g3.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(250, 38));
        return field;
    }

    // ── Styled Combo Box ─────────────────────────────────────────────
    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(INPUT_BG);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        combo.setPreferredSize(new Dimension(200, 38));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT : INPUT_BG);
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        return combo;
    }

    // ── Styled Table ─────────────────────────────────────────────────
    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? PRIMARY : TABLE_ROW_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                }
                return c;
            }
        };
        table.setFont(FONT_TABLE);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(FONT_TABLE_HEAD);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);

        // Center-align header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setBackground(TABLE_HEADER);
        headerRenderer.setForeground(TEXT_PRIMARY);
        headerRenderer.setFont(FONT_TABLE_HEAD);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        return table;
    }

    // ── Scroll Pane for Tables ───────────────────────────────────────
    public static JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(PRIMARY);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        // Style scrollbar
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT;
                this.trackColor = PRIMARY_DARK;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });
        return scroll;
    }

    // ── Card Panel ───────────────────────────────────────────────────
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return card;
    }

    // ── Stat Card (Dashboard) ────────────────────────────────────────
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // Left accent bar
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight(), 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_TITLE);
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    // ── Section Label ────────────────────────────────────────────────
    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    // ── Form Panel Helper ────────────────────────────────────────────
    public static JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(500, 50));
        JLabel label = new JLabel(labelText);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        label.setPreferredSize(new Dimension(130, 38));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ── Separator ────────────────────────────────────────────────────
    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        return sep;
    }

    // ── Dialog helpers ───────────────────────────────────────────────
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}