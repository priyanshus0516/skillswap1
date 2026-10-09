package com.skillswap.gui;

import com.skillswap.exception.InvalidRequestException;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.*;
import com.skillswap.service.RequestService;
import com.skillswap.service.SessionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab for scheduling and managing learning sessions.
 */
public class SessionsPanel extends JPanel {

    private final Student student;
    private final SessionService sessionService = new SessionService();
    private final RequestService requestService = new RequestService();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Skill", "With", "Date/Time", "Mode", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Session> sessions = new ArrayList<>();

    public SessionsPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UITheme.BG);

        UITheme.styleTable(table);
        add(new JLabel("Your scheduled learning sessions (double-click a row for quick actions):"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) quickActions();
            }
        });

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton scheduleButton = UITheme.primaryButton("Schedule from an accepted request");
        JButton completeButton = UITheme.primaryButton("Mark selected as completed");
        JButton cancelButton = UITheme.dangerButton("Cancel selected");
        JButton refreshButton = UITheme.secondaryButton("Refresh");
        buttons.add(scheduleButton);
        buttons.add(completeButton);
        buttons.add(cancelButton);
        buttons.add(refreshButton);
        add(buttons, BorderLayout.SOUTH);

        scheduleButton.addActionListener(e -> scheduleFromAcceptedRequest());
        completeButton.addActionListener(e -> markCompleted());
        cancelButton.addActionListener(e -> cancelSelected());
        refreshButton.addActionListener(e -> refresh());

        refresh();
    }

    public void refresh() {
        try {
            sessions = sessionService.sessionsFor(student);
        } catch (SkillSwapException e) {
            JOptionPane.showMessageDialog(this, "Could not load sessions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            sessions = new ArrayList<>();
        }

        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        for (Session s : sessions) {
            model.addRow(new Object[]{
                    s.getSessionId(), s.getRequest().getSkillRequested(),
                    otherParty(s), s.getDateTime().format(fmt), s.getMode(), s.getStatus()});
        }
    }

    private String otherParty(Session s) {
        ExchangeRequest r = s.getRequest();
        Student other = (r.getSender().getUserId() == student.getUserId()) ? r.getReceiver() : r.getSender();
        return other.getName();
    }

    private void scheduleFromAcceptedRequest() {
        List<ExchangeRequest> accepted = new ArrayList<>();
        try {
            for (ExchangeRequest r : requestService.incomingFor(student)) {
                if (r.getStatus() == RequestStatus.ACCEPTED) accepted.add(r);
            }
            for (ExchangeRequest r : requestService.outgoingFor(student)) {
                if (r.getStatus() == RequestStatus.ACCEPTED) accepted.add(r);
            }
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Could not fetch requests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (accepted.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No ACCEPTED requests available to schedule yet.");
            return;
        }

        ExchangeRequest chosen = (ExchangeRequest) JOptionPane.showInputDialog(
                this, "Choose an accepted request:", "Schedule Session",
                JOptionPane.PLAIN_MESSAGE, null, accepted.toArray(), accepted.get(0));
        if (chosen == null) return;

        String dateStr = JOptionPane.showInputDialog(this,
                "Enter date & time (yyyy-MM-dd HH:mm):",
                LocalDateTime.now().plusDays(1).withMinute(0).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        if (dateStr == null) return;

        SessionMode mode = (SessionMode) JOptionPane.showInputDialog(this,
                "Mode:", "Session Mode", JOptionPane.PLAIN_MESSAGE, null,
                SessionMode.values(), SessionMode.ONLINE);
        if (mode == null) return;

        String place = JOptionPane.showInputDialog(this,
                mode == SessionMode.ONLINE ? "Meeting link:" : "Location on campus:");
        if (place == null) place = "";

        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr.trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            sessionService.schedule(chosen, dateTime, 60, mode, place);
            JOptionPane.showMessageDialog(this, "Session scheduled!");
            refresh();
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please use the format yyyy-MM-dd HH:mm",
                    "Invalid date format", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidRequestException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? -1 : table.convertRowIndexToModel(viewRow);
    }

    private void markCompleted() {
        int row = selectedModelRow();
        if (row < 0 || row >= sessions.size()) {
            JOptionPane.showMessageDialog(this, "Select a session first.");
            return;
        }
        try {
            sessionService.markCompleted(sessions.get(row));
            refresh();
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelected() {
        int row = selectedModelRow();
        if (row < 0 || row >= sessions.size()) {
            JOptionPane.showMessageDialog(this, "Select a session first.");
            return;
        }
        try {
            sessionService.cancel(sessions.get(row));
            refresh();
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quickActions() {
        int row = selectedModelRow();
        if (row < 0 || row >= sessions.size()) return;
        Session s = sessions.get(row);
        if (s.getStatus() != SessionStatus.SCHEDULED) {
            JOptionPane.showMessageDialog(this, "This session is already " + s.getStatus() + ".");
            return;
        }
        Object[] options = {"Mark completed", "Cancel session", "Close"};
        int choice = JOptionPane.showOptionDialog(this,
                s.getRequest().getSkillRequested() + " session with " + otherParty(s),
                "Session actions",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);
        if (choice == 0) markCompleted();
        else if (choice == 1) cancelSelected();
    }
}
