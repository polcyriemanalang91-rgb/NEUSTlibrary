package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ProfilePanel extends JPanel {

    // ── Colors (matching StudentDashboard theme) ───────────────────────────────
    private static final Color CLR_BG       = new Color(0x1a2e1a);
    private static final Color CLR_CARD_BG  = new Color(0x1e331e);
    private static final Color CLR_ACCENT   = new Color(0x4caf50);
    private static final Color CLR_ACCENT2  = new Color(0x00e5ff);
    private static final Color CLR_BORDER   = new Color(0x2e4d2e);
    private static final Color CLR_TEXT_DIM = new Color(0xaaaaaa);
    private static final Color CLR_RED      = new Color(0xf44336);
    private static final Color CLR_INPUT_BG = new Color(0x243d24);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final Member    member;
    private final MemberDAO memberDAO = new MemberDAO();

    // ── Form fields ───────────────────────────────────────────────────────────
    private JTextField  firstNameField;
    private JTextField  lastNameField;
    private JTextField  emailField;
    private JTextField  courseField;
    private JTextField  yearLevelField;
    private JTextField  contactField;
    private JPasswordField currentPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;

    private JLabel statusLabel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public ProfilePanel(Member member) {
        this.member = member;
        setLayout(new BorderLayout());
        setBackground(CLR_BG);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildForm(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0d1f0d));
        header.setBorder(new EmptyBorder(20, 24, 16, 24));

        // Avatar circle
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2e5c2e));
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(CLR_ACCENT);
                g2.fillOval(17, 8, 22, 22);
                g2.fillArc(10, 32, 36, 22, 0, 180);
                g2.dispose();
            }
            public Dimension getPreferredSize() { return new Dimension(56, 56); }
        };

        JPanel textCol = new JPanel();
        textCol.setBackground(new Color(0x0d1f0d));
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(member.getFullName());
        nameLabel.setFont(FONT_HEADER);
        nameLabel.setForeground(Color.WHITE);

        JLabel idLabel = new JLabel("Student ID: " + member.getStudentID());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idLabel.setForeground(CLR_ACCENT2);

        JLabel roleLabel = new JLabel("Student Member");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(CLR_ACCENT);
        roleLabel.setBackground(new Color(0x1e3d1e));
        roleLabel.setOpaque(true);
        roleLabel.setBorder(new EmptyBorder(2, 8, 2, 8));

        textCol.add(nameLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(idLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(roleLabel);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setBackground(new Color(0x0d1f0d));
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

        form.add(buildSectionCard("Personal Information", buildPersonalFields()));
        form.add(Box.createVerticalStrut(16));
        form.add(buildSectionCard("Change Password", buildPasswordFields()));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBackground(CLR_BG);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        return scroll;
    }

    // ── Section card wrapper ──────────────────────────────────────────────────
    private JPanel buildSectionCard(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CLR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(16, 20, 18, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setForeground(CLR_ACCENT);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);

        JPanel titleArea = new JPanel(new BorderLayout());
        titleArea.setBackground(CLR_CARD_BG);
        titleArea.add(sectionTitle, BorderLayout.WEST);
        titleArea.add(sep,          BorderLayout.SOUTH);

        card.add(titleArea, BorderLayout.NORTH);
        card.add(content,   BorderLayout.CENTER);
        return card;
    }

    // ── Personal info fields ──────────────────────────────────────────────────
    private JPanel buildPersonalFields() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(CLR_CARD_BG);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 12);
        gc.anchor = GridBagConstraints.WEST;

        firstNameField  = styledField(member.getFirstName());
        lastNameField   = styledField(member.getLastName());
        emailField      = styledField(member.getEmail());
        courseField     = styledField(member.getCourseProgram());
        yearLevelField  = styledField(member.getYearLevel());
        contactField    = styledField(member.getContactNumber());

        // Row 0: First Name | Last Name
        addFormRow(grid, gc, 0, "First Name *",    firstNameField,
                              "Last Name *",       lastNameField);
        // Row 1: Email
        addFormRowFull(grid, gc, 1, "Email Address *", emailField);
        // Row 2: Course | Year Level
        addFormRow(grid, gc, 2, "Course / Program", courseField,
                              "Year Level",        yearLevelField);
        // Row 3: Contact
        addFormRowFull(grid, gc, 3, "Contact Number", contactField);

        return grid;
    }

    // ── Password fields ───────────────────────────────────────────────────────
    private JPanel buildPasswordFields() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(CLR_CARD_BG);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 12);
        gc.anchor = GridBagConstraints.WEST;

        currentPassField  = styledPasswordField();
        newPassField      = styledPasswordField();
        confirmPassField  = styledPasswordField();

        addFormRowFull(grid, gc, 0, "Current Password", currentPassField);
        addFormRow(grid, gc, 1, "New Password",    newPassField,
                              "Confirm Password", confirmPassField);

        JLabel hint = new JLabel("Leave password fields blank to keep current password.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(CLR_TEXT_DIM);
        GridBagConstraints hintGc = new GridBagConstraints();
        hintGc.gridx = 0; hintGc.gridy = 2;
        hintGc.gridwidth = 4;
        hintGc.fill   = GridBagConstraints.HORIZONTAL;
        hintGc.insets = new Insets(2, 0, 0, 0);
        grid.add(hint, hintGc);

        return grid;
    }

    // ── Footer (status + buttons) ─────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(new Color(0x0d1f0d));
        footer.setBorder(new EmptyBorder(14, 24, 16, 24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(CLR_ACCENT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(new Color(0x0d1f0d));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(FONT_BODY);
        cancelBtn.setBackground(new Color(0x2a4a2a));
        cancelBtn.setForeground(new Color(0xccddcc));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        cancelBtn.addActionListener(e -> closeDialog());

        JButton saveBtn = new JButton("  Save Changes");
        saveBtn.setFont(FONT_BODY);
        saveBtn.setBackground(new Color(0x2e5c2e));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { saveBtn.setBackground(CLR_ACCENT); }
            public void mouseExited (MouseEvent e) { saveBtn.setBackground(new Color(0x2e5c2e)); }
        });
        saveBtn.addActionListener(e -> saveChanges());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(btnPanel,    BorderLayout.EAST);
        return footer;
    }

    // ── Save logic ────────────────────────────────────────────────────────────
    private void saveChanges() {
        // Validation
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showStatus("⚠ First name, last name, and email are required.", CLR_RED);
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showStatus("⚠ Please enter a valid email address.", CLR_RED);
            return;
        }

        // Password handling
        String currentPass  = new String(currentPassField.getPassword()).trim();
        String newPass      = new String(newPassField.getPassword()).trim();
        String confirmPass  = new String(confirmPassField.getPassword()).trim();

        boolean changingPass = !newPass.isEmpty() || !confirmPass.isEmpty() || !currentPass.isEmpty();
        if (changingPass) {
            if (currentPass.isEmpty()) {
                showStatus("⚠ Enter your current password to change it.", CLR_RED);
                return;
            }
            if (newPass.length() < 6) {
                showStatus("⚠ New password must be at least 6 characters.", CLR_RED);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                showStatus("⚠ New password and confirmation do not match.", CLR_RED);
                return;
            }
        }

        // Apply changes to model
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setCourseProgram(courseField.getText().trim());
        member.setYearLevel(yearLevelField.getText().trim());
        member.setContactNumber(contactField.getText().trim());

        final boolean doPasswordChange = changingPass;
        final String  finalCurrentPass = currentPass;
        final String  finalNewPass     = newPass;

        showStatus("Saving...", CLR_TEXT_DIM);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    // Update profile info
                    boolean ok = memberDAO.updateProfile(member);
                    if (!ok) return "PROFILE_FAIL";

                    // Change password if requested
                    if (doPasswordChange) {
                        boolean passOk = memberDAO.changePassword(
                            member.getMemberID(), finalNewPass);
                        if (!passOk) return "PASS_FAIL";
                    }
                    return "OK";
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "ERROR:" + ex.getMessage();
                }
            }
            @Override
            protected void done() {
                try {
                    String result = get();
                    switch (result) {
                        case "OK" -> {
                            showStatus("✔ Profile updated successfully!", CLR_ACCENT);
                            clearPasswordFields();
                        }
                        case "PROFILE_FAIL" ->
                            showStatus("⚠ Failed to update profile. Please try again.", CLR_RED);
                        case "PASS_FAIL" ->
                            showStatus("⚠ Incorrect current password.", CLR_RED);
                        default ->
                            showStatus("⚠ Error: " + result.replace("ERROR:", ""), CLR_RED);
                    }
                } catch (Exception ex) {
                    showStatus("⚠ Unexpected error occurred.", CLR_RED);
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
    /** Adds a two-column form row: [label][field][label][field] */
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

    /** Adds a full-width form row: [label][field spanning 3 cols] */
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
        lbl.setForeground(CLR_TEXT_DIM);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 8));
        return lbl;
    }

    private JTextField styledField(String value) {
        JTextField f = new JTextField(value != null ? value : "");
        f.setFont(FONT_BODY);
        f.setBackground(CLR_INPUT_BG);
        f.setForeground(Color.WHITE);
        f.setCaretColor(CLR_ACCENT);
        f.setPreferredSize(new Dimension(160, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        addFocusBorder(f);
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBackground(CLR_INPUT_BG);
        f.setForeground(Color.WHITE);
        f.setCaretColor(CLR_ACCENT);
        f.setPreferredSize(new Dimension(160, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        addFocusBorder(f);
        return f;
    }

    private void addFocusBorder(JComponent field) {
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_ACCENT, 1),
                    new EmptyBorder(4, 8, 4, 8)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CLR_BORDER, 1),
                    new EmptyBorder(4, 8, 4, 8)
                ));
            }
        });
    }
}