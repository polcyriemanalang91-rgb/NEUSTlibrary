package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Member;
import neustlibrarysystem.util.PasswordUtil;
import neustlibrarysystem.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {

    private Member currentMember;
    private MemberDAO memberDAO;

    private JTextField txtFirstName, txtLastName, txtEmail, txtStudentID;
    private JTextField txtCourse, txtYearLevel, txtContact, txtAddress;
    private JPasswordField txtCurrentPass, txtNewPass, txtConfirmPass;

    private JButton saveBtn, changeBtn;

    public ProfilePanel(Member currentMember) {
        this.currentMember = currentMember;
        this.memberDAO = new MemberDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Profile Info",     buildProfileTab());
        tabs.addTab("Change Password",  buildPasswordTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildProfileTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtStudentID = new JTextField(currentMember.getStudentID(), 20);
        txtStudentID.setEditable(false);
        txtStudentID.setBackground(new Color(0xECECEC));
        txtFirstName = new JTextField(currentMember.getFirstName(), 20);
        txtLastName  = new JTextField(currentMember.getLastName(), 20);
        txtEmail     = new JTextField(currentMember.getEmail(), 20);
        txtCourse    = new JTextField(currentMember.getCourseProgram()   != null ? currentMember.getCourseProgram()   : "", 20);
        txtYearLevel = new JTextField(currentMember.getYearLevel()       != null ? currentMember.getYearLevel()       : "", 20);
        txtContact   = new JTextField(currentMember.getContactNumber()   != null ? currentMember.getContactNumber()   : "", 20);
        txtAddress   = new JTextField(currentMember.getAddress()         != null ? currentMember.getAddress()         : "", 20);

        Object[][] fields = {
            {"Student ID:",      txtStudentID},
            {"First Name:",      txtFirstName},
            {"Last Name:",       txtLastName},
            {"Email:",           txtEmail},
            {"Course/Program:",  txtCourse},
            {"Year Level:",      txtYearLevel},
            {"Contact Number:",  txtContact},
            {"Address:",         txtAddress}
        };

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel((String) fields[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            JTextField tf = (JTextField) fields[i][1];
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(tf, gbc);
        }

        saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(0x1B4F72));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.addActionListener(e -> saveProfile());
        gbc.gridx = 1; gbc.gridy = fields.length; gbc.anchor = GridBagConstraints.EAST;
        panel.add(saveBtn, gbc);

        return panel;
    }

    private JPanel buildPasswordTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCurrentPass = new JPasswordField(20);
        txtNewPass     = new JPasswordField(20);
        txtConfirmPass = new JPasswordField(20);

        String[] labels = {"Current Password:", "New Password:", "Confirm New Password:"};
        JPasswordField[] fields = {txtCurrentPass, txtNewPass, txtConfirmPass};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(fields[i], gbc);
        }

        changeBtn = new JButton("Change Password");
        changeBtn.setBackground(new Color(0x117A65));
        changeBtn.setForeground(Color.WHITE);
        changeBtn.setFocusPainted(false);
        changeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        changeBtn.addActionListener(e -> changePassword());
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(changeBtn, gbc);

        return panel;
    }

    // ─── SwingWorker: saveProfile ─────────────────────────────────────────────
    private void saveProfile() {
        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName .getText().trim();
        String email     = txtEmail    .getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First name, last name, and email are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I-update ang member object sa EDT bago pumasok sa background thread
        currentMember.setFirstName    (firstName);
        currentMember.setLastName     (lastName);
        currentMember.setEmail        (email);
        currentMember.setCourseProgram(txtCourse   .getText().trim());
        currentMember.setYearLevel    (txtYearLevel.getText().trim());
        currentMember.setContactNumber(txtContact  .getText().trim());
        currentMember.setAddress      (txtAddress  .getText().trim());

        saveBtn.setEnabled(false);
        saveBtn.setText("Saving...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // Nasa background thread — ligtas mag-query ng DB dito
                return memberDAO.updateProfile(currentMember);
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang UI
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ProfilePanel.this,
                                "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                ProfilePanel.this,
                                "Update failed.", "Error", JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ProfilePanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    saveBtn.setEnabled(true);
                    saveBtn.setText("Save Changes");
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: changePassword ──────────────────────────────────────────
    private void changePassword() {
        String current = new String(txtCurrentPass.getPassword());
        String newPass = new String(txtNewPass    .getPassword());
        String confirm = new String(txtConfirmPass.getPassword());

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All password fields are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!PasswordUtil.verifyPassword(current, currentMember.getPasswordHash())) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int memberID = currentMember.getMemberID();

        changeBtn.setEnabled(false);
        changeBtn.setText("Changing...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // Nasa background thread — ligtas mag-query ng DB dito
                return memberDAO.changePassword(memberID, newPass);
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang UI
                try {
                    if (get()) {
                        // I-update ang local hash para tama ang subsequent password checks
                        currentMember.setPasswordHash(PasswordUtil.hashPassword(newPass));
                        JOptionPane.showMessageDialog(
                                ProfilePanel.this,
                                "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        txtCurrentPass.setText("");
                        txtNewPass    .setText("");
                        txtConfirmPass.setText("");
                    } else {
                        JOptionPane.showMessageDialog(
                                ProfilePanel.this,
                                "Password change failed.", "Error", JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ProfilePanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    changeBtn.setEnabled(true);
                    changeBtn.setText("Change Password");
                }
            }
        };

        worker.execute();
    }
}