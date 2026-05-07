package neustlibrarysystem.view.common;

import neustlibrarysystem.db.DatabaseConnection;
import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.dao.LibrarianDAO;
import neustlibrarysystem.dao.AdminDAO;
import neustlibrarysystem.model.Member;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.util.ValidationUtil;
import neustlibrarysystem.view.admin.AdminDashboard;
import neustlibrarysystem.view.librarian.LibrarianDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color CLR_BG          = new Color(0x0b1a12);   // deep forest bg
    private static final Color CLR_BG_TOP      = new Color(0x0f2318);   // gradient top
    private static final Color CLR_CARD        = new Color(0x0f2318);   // card bg
    private static final Color CLR_CARD_BORDER = new Color(0x1e5030);   // card border
    private static final Color CLR_ACCENT      = new Color(0x4caf6a);   // bright green accent
    private static final Color CLR_ACCENT_DARK = new Color(0x2e8b47);   // hover green
    private static final Color CLR_ACCENT_LIGHT= new Color(0x6dd98a);   // light green
    private static final Color CLR_TEXT        = new Color(0xd4f0d4);   // primary text
    private static final Color CLR_TEXT_SUB    = new Color(0x5a9a6a);   // secondary text
    private static final Color CLR_INPUT_BG    = new Color(0x091510);   // input background
    private static final Color CLR_INPUT_BORDER= new Color(0x2a6035);   // input border
    private static final Color CLR_INPUT_FOCUS = new Color(0x4caf6a);   // focused border
    private static final Color CLR_ERROR       = new Color(0xFF6B6B);
    private static final Color CLR_SUCCESS     = new Color(0x4caf6a);

    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  24);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD,  13);

    // ── Widgets ───────────────────────────────────────────────────────────────
    private JComboBox<String> roleBox;
    private JTextField        emailField;
    private JPasswordField    passwordField;
    private JButton           loginBtn;
    private JButton           registerBtn;
    private JLabel            statusLabel;
    private JPanel            card;

    // ── Animation ─────────────────────────────────────────────────────────────
    private int   cardOffsetY = 30;
    private Timer slideTimer;

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final MemberDAO    memberDAO    = new MemberDAO();
    private final LibrarianDAO librarianDAO = new LibrarianDAO();
    private final AdminDAO     adminDAO     = new AdminDAO();

    // ── Constructor ───────────────────────────────────────────────────────────
    public LoginFrame() {
        setTitle("NEUST Library System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 590);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);
        buildUI();
        startSlideAnimation();
    }

    // ── Animation ─────────────────────────────────────────────────────────────
    private void startSlideAnimation() {
        slideTimer = new Timer(12, null);
        slideTimer.addActionListener(e -> {
            cardOffsetY = Math.max(0, cardOffsetY - 2);
            if (card != null) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_CARD_BORDER, 1),
                    new EmptyBorder(32 + cardOffsetY, 36, 32, 36)
                ));
                card.revalidate();
                card.repaint();
            }
            if (cardOffsetY <= 0) slideTimer.stop();
        });
        slideTimer.start();
    }

    private void shakeComponent(Component comp) {
        final int[] ticks = {0};
        Point origin = comp.getLocation();
        Timer t = new Timer(28, null);
        t.addActionListener(e -> {
            ticks[0]++;
            comp.setLocation(origin.x + (ticks[0] % 2 == 0 ? 6 : -6), origin.y);
            if (ticks[0] >= 8) { comp.setLocation(origin); ((Timer) e.getSource()).stop(); }
        });
        t.start();
    }

    // ── UI Construction ───────────────────────────────────────────────────────
    private void buildUI() {
        // Root: deep green gradient background
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Green gradient from top-center
                GradientPaint gp = new GradientPaint(
                    getWidth() / 2f, 0, new Color(0x0f2318),
                    getWidth() / 2f, getHeight(), new Color(0x060e09));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative glow circle top-right
                g2.setColor(new Color(76, 175, 106, 28));
                g2.fillOval(getWidth() - 180, -80, 280, 280);
                // Decorative glow circle bottom-left
                g2.setColor(new Color(46, 139, 71, 22));
                g2.fillOval(-100, getHeight() - 160, 280, 280);
            }
        };
        root.setBackground(CLR_BG);

        // ── Card ──────────────────────────────────────────────────────────────
        card = new JPanel(new GridBagLayout());
        card.setBackground(CLR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_CARD_BORDER, 1),
            new EmptyBorder(32 + cardOffsetY, 36, 32, 36)
        ));
        card.setPreferredSize(new Dimension(380, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill      = GridBagConstraints.HORIZONTAL;
        gc.insets    = new Insets(4, 0, 4, 0);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.weightx   = 1.0;

        // ── Logo area ─────────────────────────────────────────────────────────
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        // Book icon in a rounded square badge
        JLabel bookIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Badge background
                GradientPaint bg = new GradientPaint(0, 0, new Color(0x1e4d2a), 56, 56, new Color(0x2d7a3a));
                g2.setPaint(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, 56, 56, 14, 14));

                // Badge border
                g2.setColor(new Color(0x4caf6a, true) {
                    public int getAlpha() { return 70; }
                });
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 55, 55, 14, 14));

                // Stack of books (3 layers, lighter toward top)
                // Bottom book
                g2.setColor(new Color(0x6dd98a));
                g2.fillRoundRect(6,  38, 18, 9,  3, 3);
                // Middle book
                g2.setColor(new Color(0x4caf6a));
                g2.fillRoundRect(8,  27, 22, 11, 3, 3);
                // Top book
                g2.setColor(new Color(0x2e8b47));
                g2.fillRoundRect(10, 15, 26, 12, 3, 3);

                // Spine lines
                g2.setColor(new Color(0x0f, 0x23, 0x18));
                g2.fillRoundRect(11, 38, 3, 9,  2, 2);
                g2.fillRoundRect(13, 27, 3, 11, 2, 2);
                g2.fillRoundRect(15, 15, 3, 12, 2, 2);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(56, 56); }
        };
        bookIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("NEUST Library");
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(CLR_TEXT);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLbl = new JLabel("Talavera Off-Campus · Library System");
        subtitleLbl.setFont(FONT_SUBTITLE);
        subtitleLbl.setForeground(CLR_TEXT_SUB);
        subtitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Accent divider bar
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    getWidth() / 2f - 24, 0, CLR_ACCENT,
                    getWidth() / 2f + 24, 0, CLR_ACCENT_LIGHT);
                g2.setPaint(gp);
                g2.fillRoundRect(getWidth() / 2 - 24, 0, 48, 3, 3, 3);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 10); }
        };
        divider.setOpaque(false);
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        logoPanel.add(bookIcon);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(titleLbl);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitleLbl);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(divider);

        card.add(logoPanel, gc);

        // ── Role selector ──────────────────────────────────────────────────────
        gc.insets = new Insets(16, 0, 4, 0);
        card.add(buildFieldLabel("LOGIN AS"), gc);

        gc.insets = new Insets(0, 0, 8, 0);
        roleBox = new JComboBox<>(new String[]{"Student / Member", "Librarian", "Admin"});
        roleBox.setFont(FONT_INPUT);
        roleBox.setBackground(CLR_INPUT_BG);
        roleBox.setForeground(CLR_TEXT);
        roleBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        roleBox.setPreferredSize(new Dimension(0, 40));
        UIManager.put("ComboBox.foreground",        CLR_TEXT);
        UIManager.put("ComboBox.background",        CLR_INPUT_BG);
        UIManager.put("ComboBox.selectionBackground", CLR_ACCENT);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        card.add(roleBox, gc);

        // ── Email ──────────────────────────────────────────────────────────────
        gc.insets = new Insets(4, 0, 4, 0);
        card.add(buildFieldLabel("EMAIL ADDRESS"), gc);

        gc.insets = new Insets(0, 0, 8, 0);
        emailField = new JTextField();
        styleInput(emailField, "Enter your email");
        card.add(emailField, gc);

        // ── Password ───────────────────────────────────────────────────────────
        gc.insets = new Insets(4, 0, 4, 0);
        card.add(buildFieldLabel("PASSWORD"), gc);

        gc.insets = new Insets(0, 0, 8, 0);
        passwordField = new JPasswordField();
        styleInput(passwordField, "Enter your password");
        card.add(passwordField, gc);

        // ── Status label ───────────────────────────────────────────────────────
        gc.insets = new Insets(0, 0, 4, 0);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(CLR_ERROR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, gc);

        // ── Login button ───────────────────────────────────────────────────────
        gc.insets = new Insets(4, 0, 8, 0);
        loginBtn = new JButton("Login") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, getBackground(),
                    0, getHeight(), getBackground().darker());
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(FONT_BTN);
        loginBtn.setBackground(CLR_ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setOpaque(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(0, 44));
        loginBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        loginBtn.addActionListener(e -> handleLogin());
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (loginBtn.isEnabled()) loginBtn.setBackground(CLR_ACCENT_DARK);
            }
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(CLR_ACCENT); }
        });
        card.add(loginBtn, gc);

        // ── Divider ────────────────────────────────────────────────────────────
        gc.insets = new Insets(0, 0, 8, 0);
        JPanel hrPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x1e5030));
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 12); }
        };
        hrPanel.setOpaque(false);
        card.add(hrPanel, gc);

        // ── Register link ──────────────────────────────────────────────────────
        gc.insets = new Insets(0, 0, 0, 0);
        registerBtn = new JButton("No account yet? Register as Student");
        registerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setForeground(CLR_ACCENT);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> openRegister());
        registerBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { registerBtn.setForeground(CLR_TEXT); }
            public void mouseExited(MouseEvent e)  { registerBtn.setForeground(CLR_ACCENT); }
        });
        card.add(registerBtn, gc);

        // Show/hide register based on role
        roleBox.addActionListener(e ->
            registerBtn.setVisible(roleBox.getSelectedIndex() == 0));

        // Enter key to submit
        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        // ── Drop shadow wrapper ────────────────────────────────────────────────
        JPanel shadow = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 8; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 14 * i));
                    g2.fillRoundRect(i, i + 4, getWidth() - i * 2, getHeight() - i * 2, 14, 14);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        shadow.setOpaque(false);
        shadow.setBorder(new EmptyBorder(8, 8, 16, 8));
        shadow.add(card, BorderLayout.CENTER);

        GridBagConstraints rootGc = new GridBagConstraints();
        rootGc.fill = GridBagConstraints.BOTH;
        rootGc.weightx = rootGc.weighty = 1.0;
        root.add(shadow, rootGc);

        setContentPane(root);
    }

    // ── Input field styling ───────────────────────────────────────────────────
    private void styleInput(JTextField field, String placeholder) {
        field.setFont(FONT_INPUT);
        field.setBackground(CLR_INPUT_BG);
        field.setForeground(CLR_TEXT);
        field.setCaretColor(CLR_ACCENT);
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_INPUT_FOCUS, 2),
                    new EmptyBorder(5, 11, 5, 11)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });
    }

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_TEXT_SUB);
        lbl.putClientProperty("html.disable", Boolean.TRUE);
        return lbl;
    }

    // ── Login logic ───────────────────────────────────────────────────────────
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        int    role     = roleBox.getSelectedIndex();

        if (ValidationUtil.isNullOrEmpty(email) || ValidationUtil.isNullOrEmpty(password)) {
            showStatus("Please enter email and password.", false);
            shakeComponent(loginBtn);
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");
        statusLabel.setText(" ");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            boolean success = false;

            @Override protected Void doInBackground() {
                switch (role) {
                    case 0 -> {
                        Member m = memberDAO.login(email, password);
                        if (m != null) {
                            success = true;
                            SwingUtilities.invokeLater(() -> {
                                new StudentDashboard(m).setVisible(true);
                                dispose();
                            });
                        }
                    }
                    case 1 -> {
                        Librarian lib = librarianDAO.login(email, password);
                        if (lib != null) {
                            success = true;
                            SwingUtilities.invokeLater(() -> {
                                new LibrarianDashboard(lib).setVisible(true);
                                dispose();
                            });
                        }
                    }
                    case 2 -> {
                        Admin admin = adminDAO.authenticate(email, password);
                        if (admin != null) {
                            success = true;
                            SwingUtilities.invokeLater(() -> {
                                new AdminDashboard(admin).setVisible(true);
                                dispose();
                            });
                        }
                    }
                }
                return null;
            }

            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Login");
                if (!success) {
                    showStatus("Invalid email or password.", false);
                    shakeComponent(card);
                }
            }
        };
        worker.execute();
    }

    private void openRegister() {
        new RegisterFrame().setVisible(true);
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setForeground(success ? CLR_SUCCESS : CLR_ERROR);
        statusLabel.setText(message);
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to the database.\nPlease check your SQL Server configuration.",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}