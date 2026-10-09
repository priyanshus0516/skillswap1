package com.skillswap.gui;

import com.skillswap.exception.DuplicateUserException;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.service.AuthService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Dialog for student account registration with live input validation.
 */
public class RegisterDialog extends JDialog {

    private final JTextField regNoField = new JTextField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
    private final JTextField deptField = new JTextField(15);
    private final AuthService authService = new AuthService();

    private final JLabel statusLabel = new JLabel(" ");
    private final JButton submit = UITheme.primaryButton("Register");

    public RegisterDialog(JFrame owner) {
        super(owner, "Create SkillSwap Account", true);
        setSize(400, 440);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BG);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UITheme.sectionLabel("Create your account");
        outer.add(title, BorderLayout.NORTH);

        JPanel form = UITheme.card();
        form.setLayout(new GridLayout(0, 2, 8, 10));
        addLabeledField(form, "Registration No:", regNoField);
        addLabeledField(form, "Full Name:", nameField);
        addLabeledField(form, "College Email:", emailField);
        addLabeledField(form, "Password:", passwordField);
        addLabeledField(form, "Year of Study:", yearSpinner);
        addLabeledField(form, "Department:", deptField);

        statusLabel.setFont(UITheme.FONT_SUBTITLE);

        submit.setPreferredSize(new Dimension(0, 40));
        submit.addActionListener(e -> doRegister());

        JPanel formWrapper = new JPanel(new BorderLayout(0, 8));
        formWrapper.setOpaque(false);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        formWrapper.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(submit, BorderLayout.SOUTH);
        formWrapper.add(bottom, BorderLayout.SOUTH);

        outer.add(formWrapper, BorderLayout.CENTER);
        add(outer);

        DocumentListener liveValidator = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateForm(); }
            @Override public void removeUpdate(DocumentEvent e) { validateForm(); }
            @Override public void changedUpdate(DocumentEvent e) { validateForm(); }
        };
        regNoField.getDocument().addDocumentListener(liveValidator);
        nameField.getDocument().addDocumentListener(liveValidator);
        emailField.getDocument().addDocumentListener(liveValidator);
        passwordField.getDocument().addDocumentListener(liveValidator);
        deptField.getDocument().addDocumentListener(liveValidator);

        getRootPane().setDefaultButton(submit);
        validateForm();
    }

    private void addLabeledField(JPanel form, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BODY);
        field.setFont(UITheme.FONT_BODY);
        form.add(label);
        form.add(field);
    }

    private void validateForm() {
        String email = emailField.getText().trim();
        int passwordLen = passwordField.getPassword().length;

        String problem = null;
        if (regNoField.getText().trim().isEmpty()) problem = "Registration number is required.";
        else if (nameField.getText().trim().isEmpty()) problem = "Full name is required.";
        else if (!email.contains("@") || !email.contains(".")) problem = "Enter a valid email address.";
        else if (passwordLen < 6) problem = "Password must be at least 6 characters.";
        else if (deptField.getText().trim().isEmpty()) problem = "Department is required.";

        if (problem == null) {
            statusLabel.setText("\u2713 Looks good - ready to create your account.");
            statusLabel.setForeground(UITheme.ACCENT);
            submit.setEnabled(true);
        } else {
            statusLabel.setText("\u26A0 " + problem);
            statusLabel.setForeground(UITheme.DANGER);
            submit.setEnabled(false);
        }
    }

    private void doRegister() {
        try {
            authService.register(
                    regNoField.getText().trim(),
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    new String(passwordField.getPassword()),
                    (Integer) yearSpinner.getValue(),
                    deptField.getText().trim());
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
            dispose();
        } catch (DuplicateUserException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration failed", JOptionPane.ERROR_MESSAGE);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Error creating account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
