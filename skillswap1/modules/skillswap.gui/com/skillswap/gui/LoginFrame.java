package com.skillswap.gui;

import com.skillswap.exception.AuthenticationException;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Student;
import com.skillswap.service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Login screen shown when the application starts.
 */
public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("SkillSwap - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG);
        setContentPane(outer);

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(360, 340));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logo = new JLabel("\uD83C\uDF93 SkillSwap", SwingConstants.CENTER);
        logo.setFont(UITheme.FONT_TITLE);
        logo.setForeground(UITheme.PRIMARY_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(logo, gbc);

        JLabel subtitle = new JLabel("Peer-to-peer skill exchange for students", SwingConstants.CENTER);
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 1;
        card.add(subtitle, gbc);
        gbc.gridwidth = 1;

        emailField.setFont(UITheme.FONT_BODY);
        passwordField.setFont(UITheme.FONT_BODY);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(spacer(10), gbc);

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(UITheme.FONT_BODY);
        gbc.gridy = 3; gbc.gridwidth = 2;
        card.add(emailLabel, gbc);
        gbc.gridy = 4;
        card.add(emailField, gbc);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BODY);
        gbc.gridy = 5;
        card.add(passLabel, gbc);
        gbc.gridy = 6;
        card.add(passwordField, gbc);

        JButton loginButton = UITheme.primaryButton("Login");
        JButton registerButton = UITheme.secondaryButton("Create account");
        loginButton.setPreferredSize(new Dimension(0, 38));
        registerButton.setPreferredSize(new Dimension(0, 38));

        gbc.gridy = 7;
        card.add(loginButton, gbc);
        gbc.gridy = 8;
        card.add(registerButton, gbc);

        JLabel hint = new JLabel("<html><center><i>Demo: ps0612@srmist.edu.in / pass123<br>"
                + "(also try ananya@srmist.edu.in / pass123)</i></center></html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 9;
        card.add(hint, gbc);

        outer.add(card);

        getRootPane().setDefaultButton(loginButton);
        loginButton.setToolTipText("Log in (or press Enter)");
        registerButton.setToolTipText("Create a new student account");

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> new RegisterDialog(this).setVisible(true));
    }

    private JComponent spacer(int height) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(1, height));
        return p;
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            Student student = authService.login(email, password);
            JOptionPane.showMessageDialog(this, "Welcome, " + student.getName() + "!");
            new DashboardFrame(student).setVisible(true);
            dispose();
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
