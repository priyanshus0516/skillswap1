package com.skillswap.gui;

import com.skillswap.exception.InvalidRequestException;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.*;
import com.skillswap.service.FeedbackService;
import com.skillswap.service.SessionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab for leaving feedback after a completed session, and viewing feedback received.
 */
public class FeedbackPanel extends JPanel {

    private final Student student;
    private final SessionService sessionService = new SessionService();
    private final FeedbackService feedbackService = new FeedbackService();

    private final DefaultTableModel receivedModel = new DefaultTableModel(
            new String[]{"From", "Rating", "Comments"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JLabel ratingLabel = new JLabel();

    public FeedbackPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UITheme.BG);

        add(ratingLabel, BorderLayout.NORTH);

        JTable receivedTable = new JTable(receivedModel);
        UITheme.styleTable(receivedTable);
        add(new JScrollPane(receivedTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton rateButton = UITheme.primaryButton("Leave feedback for a completed session");
        JButton refreshButton = UITheme.secondaryButton("Refresh");
        buttons.add(rateButton);
        buttons.add(refreshButton);
        add(buttons, BorderLayout.SOUTH);

        rateButton.addActionListener(e -> leaveFeedback());
        refreshButton.addActionListener(e -> refresh());

        refresh();
    }

    public void refresh() {
        ratingLabel.setText(String.format("Your average rating: %.1f / 5 (%d review(s))",
                student.getAverageRating(), student.getTotalRatings()));

        receivedModel.setRowCount(0);
        try {
            List<Feedback> feedbackList = feedbackService.feedbackAbout(student);
            for (Feedback f : feedbackList) {
                receivedModel.addRow(new Object[]{f.getRatedBy().getName(), f.getRating() + "/5", f.getComments()});
            }
        } catch (SkillSwapException e) {
            JOptionPane.showMessageDialog(this, "Could not load feedback: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void leaveFeedback() {
        List<Session> mySessions = new ArrayList<>();
        try {
            mySessions = sessionService.sessionsFor(student);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Could not load sessions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        mySessions.removeIf(s -> s.getStatus() != SessionStatus.COMPLETED);
        if (mySessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no completed sessions to review yet.");
            return;
        }

        Session chosen = (Session) JOptionPane.showInputDialog(this,
                "Choose a completed session:", "Leave Feedback",
                JOptionPane.PLAIN_MESSAGE, null, mySessions.toArray(), mySessions.get(0));
        if (chosen == null) return;

        ExchangeRequest r = chosen.getRequest();
        Student partner = (r.getSender().getUserId() == student.getUserId()) ? r.getReceiver() : r.getSender();

        String ratingStr = JOptionPane.showInputDialog(this, "Rate " + partner.getName() + " (1-5):", "5");
        if (ratingStr == null) return;
        String comments = JOptionPane.showInputDialog(this, "Comments:", "Great session, learned a lot!");
        if (comments == null) comments = "";

        try {
            int rating = Integer.parseInt(ratingStr.trim());
            feedbackService.submit(chosen, student, partner, rating, comments);
            JOptionPane.showMessageDialog(this, "Thanks for your feedback!");
            refresh();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Rating must be a number from 1 to 5.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidRequestException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
