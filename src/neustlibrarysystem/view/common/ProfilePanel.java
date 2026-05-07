package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class ProfilePanel extends JPanel {

    // ── Design tokens (matches LoginFrame / RegisterFrame green theme) ─────────
    private static final Color CLR_BG           = new Color(0x0b1a12);
    private static final Color CLR_CARD         = new Color(0x0f2318);
    private static final Color CLR_CARD_BORDER  = new Color(0x1e5030);
    private static final Color CLR_HEADER_BG    = new Color(0x091510);
    private static final Color CLR_FOOTER_BG    = new Color(0x091510);
    private static final Color CLR_ACCENT       = new Color(0x4caf6a);
    private static final Color CLR_ACCENT_DARK  = new Color(0x2e8b47);
    private static final Color CLR_ACCENT_LIGHT = new Color(0x6dd98a);
    private static final Color CLR_TEXT         = new Color(0xd4f0d4);
    private static final Color CLR_TEXT_SUB     = new Color(0x5a9a6a);
    private static final Color CLR_TEXT_DIM     = new Color(0x3d7a50);
    private static final Color CLR_INPUT_BG     = new Color(0x091510);
    private static final Color CLR_INPUT_BORDER = new Color(0x2a6035);
    private static final Color CLR_INPUT_FOCUS  = new Color(0x4caf6a);
    private static final Color CLR_ERROR        = new Color(0xFF6B6B);
    private static final Color CLR_CANCEL_BG    = new Color(0x152a1e);
    private static final Color CLR_CANCEL_FG    = new Color(0x8abf98);

    private static final Font FONT_NAME   = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_HINT   = new Font("Segoe UI", Font.ITALIC, 11);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final Member    member;
    private final MemberDAO memberDAO = new MemberDAO();

    // ── Form fields ───────────────────────────────────────────────────────────
    private JTextField     firstNameField;
    private JTextField     lastNameField;
    private JTextField     emailField;
    private JTextField     courseField;
    private JTextField     yearLevelField;
    private JTextField     contactField;
    private JPasswordField currentPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private JLabel         statusLabel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public ProfilePanel(Member member) {
        this.member = member;
        setLayout(new BorderLayout());
        setBackground(CLR_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x122518), getWidth(), getHeight(), CLR_HEADER_BG);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom border line
                g2.setColor(CLR_CARD_BORDER);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 24, 18, 24));

        // Avatar
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle bg
                g2.setColor(new Color(0x1e4d2a));
                g2.fillOval(0, 0, 56, 56);
                // Border ring
                g2.setColor(new Color(76, 175, 106, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 54, 54);
                // Person icon
                g2.setColor(CLR_ACCENT);
                g2.fillOval(18, 9, 20, 20);   // head
                g2.fillArc(10, 32, 36, 22, 0, 180); // body
                g2.dispose();
            }
            public Dimension getPreferredSize() { return new Dimension(56, 56); }
        };

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(member.getFullName());
        nameLabel.setFont(FONT_NAME);
        nameLabel.setForeground(CLR_TEXT);

        JLabel idLabel = new JLabel("Student ID: " + member.getStudentID());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idLabel.setForeground(CLR_ACCENT_LIGHT);

        // Role badge
        JLabel roleLabel = new JLabel("Student Member") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1e4d2a));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(new Color(76, 175, 106, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(CLR_ACCENT);
        roleLabel.setOpaque(false);
        roleLabel.setBorder(new EmptyBorder(3, 8, 3, 8));

        textCol.add(nameLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(idLabel);
        textCol.add(Box.createVerticalStrut(5));
        textCol.add(roleLabel);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(avatar);
        left.add(textCol);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    // ── Scrollable form ───────────────────────────────────────────────────────
    private JScrollPane buildForm() {
        JPanel form = new JPanel();
        form.setBackground(CLR_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        // ── Personal Information card ─────────────────────────────────────────
        form.add(buildSectionHeader("Personal Information"));
        form.add(Box.createVerticalStrut(8));

        JPanel personalCard = buildCard();
        personalCard.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 12);
        gc.anchor = GridBagConstraints.WEST;

        firstNameField = styledField(member.getFirstName());
        lastNameField  = styledField(member.getLastName());
        emailField     = styledField(member.getEmail());
        courseField    = styledField(member.getCourseProgram());
        yearLevelField = styledField(member.getYearLevel());
        contactField   = styledField(member.getContactNumber());

        addFormRow    (personalCard, gc, 0, "FIRST NAME *",    firstNameField, "LAST NAME *",  lastNameField);
        addFormRowFull(personalCard, gc, 1, "EMAIL ADDRESS *", emailField);
        addFormRow    (personalCard, gc, 2, "COURSE / PROGRAM", courseField,   "YEAR LEVEL",   yearLevelField);
        addFormRowFull(personalCard, gc, 3, "CONTACT NUMBER",  contactField);

        form.add(personalCard);
        form.add(Box.createVerticalStrut(16));

        // ── Change Password card ──────────────────────────────────────────────
        form.add(buildSectionHeader("Change Password"));
        form.add(Box.createVerticalStrut(8));

        JPanel passCard = buildCard();
        passCard.setLayout(new GridBagLayout());

        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.fill   = GridBagConstraints.HORIZONTAL;
        gc2.insets = new Insets(6, 0, 6, 12);
        gc2.anchor = GridBagConstraints.WEST;

        currentPassField = styledPasswordField();
        newPassField     = styledPasswordField();
        confirmPassField = styledPasswordField();

        addFormRowFull(passCard, gc2, 0, "CURRENT PASSWORD", currentPassField);
        addFormRow    (passCard, gc2, 1, "NEW PASSWORD",     newPassField, "CONFIRM PASSWORD", confirmPassField);

        // Hint label
        JLabel hint = new JLabel("Leave blank to keep your current password.");
        hint.setFont(FONT_HINT);
        hint.setForeground(CLR_TEXT_DIM);
        GridBagConstraints hgc = new GridBagConstraints();
        hgc.gridx = 0; hgc.gridy = 2; hgc.gridwidth = 4;
        hgc.fill = GridBagConstraints.HORIZONTAL;
        hgc.insets = new Insets(0, 0, 2, 0);
        passCard.add(hint, hgc);

        form.add(passCard);
        form.add(Box.createVerticalStrut(8));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ── Section header with accent line ──────────────────────────────────────
    private JPanel buildSectionHeader(String title) {
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(CLR_ACCENT);

        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(CLR_CARD_BORDER);
                g2.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        line.setOpaque(false);

        p.add(lbl,  BorderLayout.WEST);
        p.add(line, BorderLayout.CENTER);
        return p;
    }

    // ── Card panel builder ────────────────────────────────────────────────────
    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setBackground(CLR_CARD);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999)); // let height grow naturally
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_CARD_BORDER, 1),
            new EmptyBorder(16, 20, 16, 20)
        ));
        return card;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(CLR_FOOTER_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Top border
                g2.setColor(CLR_CARD_BORDER);
                g2.fillRect(0, 0, getWidth(), 1);
            }
        };
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 24, 16, 24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(CLR_ACCENT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        // Cancel button
        JButton cancelBtn = new JButton("Cancel") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(FONT_BTN);
        cancelBtn.setBackground(CLR_CANCEL_BG);
        cancelBtn.setForeground(CLR_CANCEL_FG);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setOpaque(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        cancelBtn.addActionListener(e -> closeDialog());
        cancelBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { cancelBtn.setBackground(new Color(0x1e3d2a)); }
            public void mouseExited (MouseEvent e) { cancelBtn.setBackground(CLR_CANCEL_BG); }
        });

        // Save button
        JButton saveBtn = new JButton("Save Changes") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, getBackground(), 0, getHeight(), getBackground().darker());
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        saveBtn.setFont(FONT_BTN);
        saveBtn.setBackground(CLR_ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setOpaque(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        saveBtn.addActionListener(e -> saveChanges());
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { saveBtn.setBackground(CLR_ACCENT_DARK); }
            public void mouseExited (MouseEvent e) { saveBtn.setBackground(CLR_ACCENT); }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(btnPanel,    BorderLayout.EAST);
        return footer;
    }

    // ── Save logic ────────────────────────────────────────────────────────────
    private void saveChanges() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showStatus("First name, last name, and email are required.", CLR_ERROR);
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showStatus("Please enter a valid email address.", CLR_ERROR);
            return;
        }

        String currentPass = new String(currentPassField.getPassword()).trim();
        String newPass     = new String(newPassField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();

        boolean changingPass = !newPass.isEmpty() || !confirmPass.isEmpty() || !currentPass.isEmpty();
        if (changingPass) {
            if (currentPass.isEmpty()) {
                showStatus("Enter your current password to change it.", CLR_ERROR);
                return;
            }
            if (newPass.length() < 6) {
                showStatus("New password must be at least 6 characters.", CLR_ERROR);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                showStatus("New password and confirmation do not match.", CLR_ERROR);
                return;
            }
        }

        member.setFirstName    (firstName);
        member.setLastName     (lastName);
        member.setEmail        (email);
        member.setCourseProgram(courseField.getText().trim());
        member.setYearLevel    (yearLevelField.getText().trim());
        member.setContactNumber(contactField.getText().trim());

        final boolean doPasswordChange = changingPass;
        final String  finalNewPass     = newPass;

        showStatus("Saving...", CLR_TEXT_SUB);

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                try {
                    boolean ok = memberDAO.updateProfile(member);
                    if (!ok) return "PROFILE_FAIL";
                    if (doPasswordChange) {
                        boolean passOk = memberDAO.changePassword(member.getMemberID(), finalNewPass);
                        if (!passOk) return "PASS_FAIL";
                    }
                    return "OK";
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "ERROR:" + ex.getMessage();
                }
            }
            @Override protected void done() {
                try {
                    String result = get();
                    switch (result) {
                        case "OK" -> {
                            showStatus("Profile updated successfully!", CLR_ACCENT);
                            clearPasswordFields();
                        }
                        case "PROFILE_FAIL" -> showStatus("Failed to update profile. Please try again.", CLR_ERROR);
                        case "PASS_FAIL"    -> showStatus("Incorrect current password.", CLR_ERROR);
                        default             -> showStatus("Error: " + result.replace("ERROR:", ""), CLR_ERROR);
                    }
                } catch (Exception ex) {
                    showStatus("Unexpected error occurred.", CLR_ERROR);
                }
            }
        }.execute();
    }

    private void clearPasswordFields() {
        currentPassField.setText("");
        newPassField.setText("");
        confirmPassField.setText("");
    }

    private void closeDialog() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    // ── Layout helpers ────────────────────────────────────────────────────────
    private void addFormRow(JPanel grid, GridBagConstraints gc, int row,
                            String lbl1, JComponent field1,
                            String lbl2, JComponent field2) {
        gc.gridy = row; gc.weightx = 0;
        gc.gridx = 0; gc.fill = GridBagConstraints.NONE;
        grid.add(makeLabel(lbl1), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 0.45;
        grid.add(field1, gc);
        gc.gridx = 2; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        grid.add(makeLabel(lbl2), gc);
        gc.gridx = 3; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 0.45;
        grid.add(field2, gc);
    }

    private void addFormRowFull(JPanel grid, GridBagConstraints gc, int row,
                                String label, JComponent field) {
        gc.gridy = row; gc.weightx = 0;
        gc.gridx = 0; gc.fill = GridBagConstraints.NONE;
        grid.add(makeLabel(label), gc);
        gc.gridx = 1; gc.gridwidth = 3;
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        grid.add(field, gc);
        gc.gridwidth = 1;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_TEXT_SUB);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 8));
        return lbl;
    }

    private JTextField styledField(String value) {
        JTextField f = new JTextField(value != null ? value : "");
        f.setFont(FONT_BODY);
        f.setBackground(CLR_INPUT_BG);
        f.setForeground(CLR_TEXT);
        f.setCaretColor(CLR_ACCENT);
        f.setPreferredSize(new Dimension(160, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        addFocusBorder(f);
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBackground(CLR_INPUT_BG);
        f.setForeground(CLR_TEXT);
        f.setCaretColor(CLR_ACCENT);
        f.setPreferredSize(new Dimension(160, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        addFocusBorder(f);
        return f;
    }

    private void addFocusBorder(JComponent field) {
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_INPUT_FOCUS, 2),
                    new EmptyBorder(4, 9, 4, 9)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_INPUT_BORDER, 1),
                    new EmptyBorder(5, 10, 5, 10)
                ));
            }
        });
    }
}