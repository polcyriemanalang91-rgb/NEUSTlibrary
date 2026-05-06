package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterFrame extends JFrame {

    // ── UI Constants ──────────────────────────────────────────────────────────
    private static final Color CLR_PRIMARY = new Color(0x1B4F8A);
    private static final Color CLR_WHITE   = Color.WHITE;
    private static final Color CLR_BG      = new Color(0xF4F6F9);
    private static final Font  FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font  FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font  FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_BTN    = new Font("Segoe UI", Font.BOLD,  13);

    // ── Fields ────────────────────────────────────────────────────────────────
    private JTextField  studentIDField;
    private JTextField  firstNameField;
    private JTextField  lastNameField;
    private JTextField  emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField  courseProgramField;
    private JComboBox<String> yearLevelCombo;
    private JTextField  contactField;
    private JTextField  addressField;
    private JLabel      statusLabel;
    private JButton     registerBtn;

    private final MemberDAO memberDAO = new MemberDAO();

    // ── Constructor ───────────────────────────────────────────────────────────
    public RegisterFrame() {
        setTitle("NEUST Library — Student Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);

        // Header
        JPanel header = new JPanel();
        header.setBackground(CLR_PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 30, 18, 30));

        JLabel icon = new JLabel("📝", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Student Registration", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Create your library account", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0xBDCFEB));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CLR_WHITE);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(4, 0, 4, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        studentIDField       = addField(form, gbc, "Student ID");
        firstNameField       = addField(form, gbc, "First Name");
        lastNameField        = addField(form, gbc, "Last Name");
        emailField           = addField(form, gbc, "Email Address");
        passwordField        = addPasswordField(form, gbc, "Password");
        confirmPasswordField = addPasswordField(form, gbc, "Confirm Password");
        courseProgramField   = addField(form, gbc, "Course / Program");

        // Year Level combo
        addLabel(form, gbc, "Year Level");
        yearLevelCombo = new JComboBox<>(new String[]{
            "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "Graduate"
        });
        yearLevelCombo.setFont(FONT_INPUT);
        yearLevelCombo.setPreferredSize(new Dimension(0, 34));
        form.add(yearLevelCombo, gbc);

        contactField = addField(form, gbc, "Contact Number");
        addressField = addField(form, gbc, "Address");

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0xC0392B));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(statusLabel, gbc);

        // Register button
        registerBtn = new JButton("Create Account");
        registerBtn.setFont(FONT_BTN);
        registerBtn.setBackground(CLR_PRIMARY);
        registerBtn.setForeground(CLR_WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(0, 40));
        registerBtn.addActionListener(e -> handleRegister());
        form.add(registerBtn, gbc);

        // Back to login link
        JButton backBtn = new JButton("Already have an account? Login");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setForeground(CLR_PRIMARY);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        form.add(backBtn, gbc);

        // Scroll pane for form
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void addLabel(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(new Color(0x555555));
        panel.add(lbl, gbc);
    }

    private JTextField addField(JPanel panel, GridBagConstraints gbc, String label) {
        addLabel(panel, gbc, label);
        JTextField field = new JTextField();
        styleInput(field);
        panel.add(field, gbc);
        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, GridBagConstraints gbc, String label) {
        addLabel(panel, gbc, label);
        JPasswordField field = new JPasswordField();
        styleInput(field);
        panel.add(field, gbc);
        return field;
    }

    private void styleInput(JTextField field) {
        field.setFont(FONT_INPUT);
        field.setPreferredSize(new Dimension(0, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCCCCCC), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setForeground(success ? new Color(0x27AE60) : new Color(0xC0392B));
        statusLabel.setText(msg);
    }

    // ── Register Handler ──────────────────────────────────────────────────────
    private void handleRegister() {
        String studentID    = studentIDField.getText().trim();
        String firstName    = firstNameField.getText().trim();
        String lastName     = lastNameField.getText().trim();
        String email        = emailField.getText().trim();
        String password     = new String(passwordField.getPassword());
        String confirmPass  = new String(confirmPasswordField.getPassword());
        String course       = courseProgramField.getText().trim();
        String yearLevel    = (String) yearLevelCombo.getSelectedItem();
        String contact      = contactField.getText().trim();
        String address      = addressField.getText().trim();

        // ── Validation ────────────────────────────────────────────────────────
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
        registerBtn.setText("Registering...");

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