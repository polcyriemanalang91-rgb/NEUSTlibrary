package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class RegisterFrame extends JFrame {

    // ── Design tokens (matches LoginFrame green theme) ────────────────────────
    private static final Color CLR_BG           = new Color(0x0b1a12);
    private static final Color CLR_CARD         = new Color(0x0f2318);
    private static final Color CLR_CARD_BORDER  = new Color(0x1e5030);
    private static final Color CLR_ACCENT       = new Color(0x4caf6a);
    private static final Color CLR_ACCENT_DARK  = new Color(0x2e8b47);
    private static final Color CLR_ACCENT_LIGHT = new Color(0x6dd98a);
    private static final Color CLR_TEXT         = new Color(0xd4f0d4);
    private static final Color CLR_TEXT_SUB     = new Color(0x5a9a6a);
    private static final Color CLR_INPUT_BG     = new Color(0x091510);
    private static final Color CLR_INPUT_BORDER = new Color(0x2a6035);
    private static final Color CLR_INPUT_FOCUS  = new Color(0x4caf6a);
    private static final Color CLR_ERROR        = new Color(0xFF6B6B);
    private static final Color CLR_SUCCESS      = new Color(0x4caf6a);

    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD,  13);

    // ── Fields ────────────────────────────────────────────────────────────────
    private JTextField     studentIDField;
    private JTextField     firstNameField;
    private JTextField     lastNameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField     courseProgramField;
    private JComboBox<String> yearLevelCombo;
    private JTextField     contactField;
    private JTextField     addressField;
    private JLabel         statusLabel;
    private JButton        registerBtn;

    private final MemberDAO memberDAO = new MemberDAO();

    // ── Constructor ───────────────────────────────────────────────────────────
    public RegisterFrame() {
        setTitle("NEUST Library — Student Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        // Root panel with dark green gradient
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    getWidth() / 2f, 0, new Color(0x0f2318),
                    getWidth() / 2f, getHeight(), new Color(0x060e09));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative glows
                g2.setColor(new Color(76, 175, 106, 25));
                g2.fillOval(getWidth() - 160, -60, 250, 250);
                g2.setColor(new Color(46, 139, 71, 18));
                g2.fillOval(-80, getHeight() - 140, 240, 240);
            }
        };
        root.setBackground(CLR_BG);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1a4a28), getWidth(), getHeight(), new Color(0x0f2318));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom border
                g2.setColor(new Color(0x1e5030));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(24, 30, 20, 30));
        header.setOpaque(false);

        // Pencil icon badge
        JLabel iconBadge = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Badge bg
                GradientPaint bg = new GradientPaint(0, 0, new Color(0x1e4d2a), 50, 50, new Color(0x2d7a3a));
                g2.setPaint(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, 50, 50, 12, 12));
                // Badge border
                g2.setColor(new Color(76, 175, 106, 65));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 49, 49, 12, 12));
                // Simple pencil/person icon drawn in green
                g2.setColor(CLR_ACCENT_LIGHT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Head
                g2.drawOval(18, 8, 14, 14);
                // Body
                g2.drawArc(11, 28, 28, 20, 0, 180);
                // Pencil
                g2.setColor(CLR_ACCENT);
                int[] px = {33, 42, 38, 29};
                int[] py = {26, 20, 16, 22};
                g2.fillPolygon(px, py, 4);
                g2.setColor(CLR_ACCENT_LIGHT);
                g2.drawLine(42, 20, 44, 15);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(50, 50); }
        };
        iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("Student Registration");
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(CLR_TEXT);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLbl = new JLabel("Create your NEUST library account");
        subtitleLbl.setFont(FONT_SUBTITLE);
        subtitleLbl.setForeground(CLR_TEXT_SUB);
        subtitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel accentBar = new JPanel() {
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
        accentBar.setOpaque(false);
        accentBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        accentBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        header.add(iconBadge);
        header.add(Box.createVerticalStrut(10));
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLbl);
        header.add(Box.createVerticalStrut(10));
        header.add(accentBar);

        // ── Form card ─────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CLR_CARD);
        form.setBorder(new EmptyBorder(24, 30, 24, 30));
        form.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(4, 0, 4, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx   = 1.0;

        // Section: Personal Info
        addSectionLabel(form, gbc, "PERSONAL INFORMATION");
        studentIDField = addField(form, gbc, "STUDENT ID",       "Enter your student ID");
        firstNameField = addField(form, gbc, "FIRST NAME",       "Enter your first name");
        lastNameField  = addField(form, gbc, "LAST NAME",        "Enter your last name");
        emailField     = addField(form, gbc, "EMAIL ADDRESS",    "Enter your email");

        // Section: Security
        addSectionLabel(form, gbc, "ACCOUNT SECURITY");
        passwordField        = addPasswordField(form, gbc, "PASSWORD",         "Min. 6 characters");
        confirmPasswordField = addPasswordField(form, gbc, "CONFIRM PASSWORD", "Re-enter password");

        // Section: Academic
        addSectionLabel(form, gbc, "ACADEMIC DETAILS");
        courseProgramField = addField(form, gbc, "COURSE / PROGRAM", "e.g. BSIT, BSED");

        addFieldLabel(form, gbc, "YEAR LEVEL");
        yearLevelCombo = new JComboBox<>(new String[]{
            "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "Graduate"
        });
        yearLevelCombo.setFont(FONT_INPUT);
        yearLevelCombo.setBackground(CLR_INPUT_BG);
        yearLevelCombo.setForeground(CLR_TEXT);
        yearLevelCombo.setPreferredSize(new Dimension(0, 42));
        yearLevelCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        UIManager.put("ComboBox.foreground",          CLR_TEXT);
        UIManager.put("ComboBox.background",          CLR_INPUT_BG);
        UIManager.put("ComboBox.selectionBackground", CLR_ACCENT);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        gbc.insets = new Insets(0, 0, 8, 0);
        form.add(yearLevelCombo, gbc);

        // Section: Contact
        addSectionLabel(form, gbc, "CONTACT DETAILS");
        contactField = addField(form, gbc, "CONTACT NUMBER", "e.g. 09XX-XXX-XXXX");
        addressField = addField(form, gbc, "ADDRESS",        "Enter your address");

        // Status label
        gbc.insets = new Insets(4, 0, 4, 0);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(CLR_ERROR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(statusLabel, gbc);

        // Register button
        gbc.insets = new Insets(4, 0, 8, 0);
        registerBtn = new JButton("Create Account") {
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
        registerBtn.setFont(FONT_BTN);
        registerBtn.setBackground(CLR_ACCENT);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setOpaque(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(0, 44));
        registerBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        registerBtn.addActionListener(e -> handleRegister());
        registerBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (registerBtn.isEnabled()) registerBtn.setBackground(CLR_ACCENT_DARK);
            }
            public void mouseExited(MouseEvent e) { registerBtn.setBackground(CLR_ACCENT); }
        });
        form.add(registerBtn, gbc);

        // Divider
        gbc.insets = new Insets(0, 0, 8, 0);
        JPanel hrPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x1e5030));
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 12); }
        };
        hrPanel.setOpaque(false);
        form.add(hrPanel, gbc);

        // Back to login link
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton backBtn = new JButton("Already have an account? Login");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setForeground(CLR_ACCENT);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { backBtn.setForeground(CLR_TEXT); }
            public void mouseExited(MouseEvent e)  { backBtn.setForeground(CLR_ACCENT); }
        });
        form.add(backBtn, gbc);

        // Scroll pane
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void addSectionLabel(JPanel panel, GridBagConstraints gbc, String text) {
        GridBagConstraints sgbc = (GridBagConstraints) gbc.clone();
        sgbc.insets = new Insets(14, 0, 6, 0);

        JPanel sectionPanel = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        sectionPanel.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(CLR_ACCENT);

        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x1e5030));
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        line.setOpaque(false);

        sectionPanel.add(lbl, BorderLayout.WEST);
        sectionPanel.add(line, BorderLayout.CENTER);

        panel.add(sectionPanel, sgbc);
    }

    private void addFieldLabel(JPanel panel, GridBagConstraints gbc, String text) {
        GridBagConstraints lgbc = (GridBagConstraints) gbc.clone();
        lgbc.insets = new Insets(4, 0, 4, 0);
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_TEXT_SUB);
        panel.add(lbl, lgbc);
    }

    private JTextField addField(JPanel panel, GridBagConstraints gbc, String label, String placeholder) {
        addFieldLabel(panel, gbc, label);
        GridBagConstraints fgbc = (GridBagConstraints) gbc.clone();
        fgbc.insets = new Insets(0, 0, 8, 0);
        JTextField field = new JTextField();
        styleInput(field, placeholder);
        panel.add(field, fgbc);
        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, GridBagConstraints gbc, String label, String placeholder) {
        addFieldLabel(panel, gbc, label);
        GridBagConstraints fgbc = (GridBagConstraints) gbc.clone();
        fgbc.insets = new Insets(0, 0, 8, 0);
        JPasswordField field = new JPasswordField();
        styleInput(field, placeholder);
        panel.add(field, fgbc);
        return field;
    }

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

    private void showStatus(String msg, boolean success) {
        statusLabel.setForeground(success ? CLR_SUCCESS : CLR_ERROR);
        statusLabel.setText(msg);
    }

    // ── Register Handler ──────────────────────────────────────────────────────
    private void handleRegister() {
        String studentID   = studentIDField.getText().trim();
        String firstName   = firstNameField.getText().trim();
        String lastName    = lastNameField.getText().trim();
        String email       = emailField.getText().trim();
        String password    = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String course      = courseProgramField.getText().trim();
        String yearLevel   = (String) yearLevelCombo.getSelectedItem();
        String contact     = contactField.getText().trim();
        String address     = addressField.getText().trim();

        if (studentID.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || password.isEmpty() || course.isEmpty()) {
            showStatus("Please fill in all required fields.", false);
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showStatus("Please enter a valid email address.", false);
            return;
        }
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters.", false);
            return;
        }
        if (!password.equals(confirmPass)) {
            showStatus("Passwords do not match.", false);
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating account...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            String errorMsg = null;

            @Override
            protected Boolean doInBackground() {
                if (memberDAO.isStudentIDExists(studentID)) {
                    errorMsg = "Student ID already registered.";
                    return false;
                }
                if (memberDAO.isEmailExists(email)) {
                    errorMsg = "Email already registered.";
                    return false;
                }
                Member m = new Member();
                m.setStudentID    (studentID);
                m.setFirstName    (firstName);
                m.setLastName     (lastName);
                m.setEmail        (email);
                m.setCourseProgram(course);
                m.setYearLevel    (yearLevel);
                m.setContactNumber(contact);
                m.setAddress      (address);
                return memberDAO.register(m, password);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                registerBtn.setText("Create Account");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Account created successfully!\nYou may now log in.",
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        showStatus(errorMsg != null ? errorMsg : "Registration failed. Please try again.", false);
                    }
                } catch (Exception ex) {
                    showStatus("Unexpected error: " + ex.getMessage(), false);
                }
            }
        };
        worker.execute();
    }
}