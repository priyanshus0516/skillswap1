package com.skillswap.gui;

import com.skillswap.model.Student;
import com.skillswap.util.FileLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window shown after a successful login.
 */
public class DashboardFrame extends JFrame {

    private final Student student;
    private JLabel subLabel;

    public DashboardFrame(Student student) {
        super("SkillSwap - " + student.getName());
        this.student = student;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(880, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        setContentPane(root);

        root.add(buildHeader(student), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY);
        tabs.setBackground(UITheme.BG);

        HomePanel homePanel = new HomePanel(student, tabs::setSelectedIndex);
        SkillsShowcasePanel skillsPanel = new SkillsShowcasePanel(student);
        ProfilePanel profilePanel = new ProfilePanel(student);
        DiscoverPanel discoverPanel = new DiscoverPanel(student);
        RequestsPanel requestsPanel = new RequestsPanel(student);
        SessionsPanel sessionsPanel = new SessionsPanel(student);
        FeedbackPanel feedbackPanel = new FeedbackPanel(student);

        tabs.addTab("  \uD83C\uDFE0 Home  ", homePanel);
        tabs.addTab("  \uD83C\uDFA8 My Skills  ", skillsPanel);
        tabs.addTab("  My Profile  ", profilePanel);
        tabs.addTab("  Discover Matches  ", discoverPanel);
        tabs.addTab("  Exchange Requests  ", requestsPanel);
        tabs.addTab("  Sessions  ", sessionsPanel);
        tabs.addTab("  Feedback & Ratings  ", feedbackPanel);

        tabs.addChangeListener(e -> {
            updateHeaderStats();
            Component selected = tabs.getSelectedComponent();
            if (selected == homePanel) homePanel.refresh();
            else if (selected == skillsPanel) skillsPanel.refresh();
            else if (selected == profilePanel) profilePanel.refresh();
            else if (selected == discoverPanel) discoverPanel.refresh();
            else if (selected == requestsPanel) requestsPanel.refresh();
            else if (selected == sessionsPanel) sessionsPanel.refresh();
            else if (selected == feedbackPanel) feedbackPanel.refresh();
        });

        root.add(tabs, BorderLayout.CENTER);
    }

    private void updateHeaderStats() {
        if (subLabel != null) {
            subLabel.setText(student.getDepartment() + " - Year " + student.getYearOfStudy()
                    + "  |  \u2605 " + String.format("%.1f", student.getAverageRating())
                    + " (" + student.getTotalRatings() + " reviews)");
        }
    }

    private JPanel buildHeader(Student student) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(UITheme.avatar(student.getName(), 42));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(student.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(UITheme.FONT_HEADING);

        subLabel = new JLabel(student.getDepartment() + " - Year " + student.getYearOfStudy()
                + "  |  \u2605 " + String.format("%.1f", student.getAverageRating())
                + " (" + student.getTotalRatings() + " reviews)");
        subLabel.setForeground(new Color(0xDCE7FB));
        subLabel.setFont(UITheme.FONT_SUBTITLE);

        textBlock.add(nameLabel);
        textBlock.add(subLabel);
        left.add(textBlock);

        header.add(left, BorderLayout.WEST);

        JLabel appName = new JLabel("\uD83C\uDF93 SkillSwap");
        appName.setForeground(Color.WHITE);
        appName.setFont(UITheme.FONT_TITLE.deriveFont(18f));
        header.add(appName, BorderLayout.CENTER);

        JButton logoutButton = UITheme.secondaryButton("Logout");
        logoutButton.addActionListener(e -> {
            FileLogger.log("LOGOUT: " + student.getEmail());
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(logoutButton);
        header.add(right, BorderLayout.EAST);

        return header;
    }
}
