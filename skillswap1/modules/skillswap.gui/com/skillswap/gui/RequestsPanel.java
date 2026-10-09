package com.skillswap.gui;

import com.skillswap.exception.InvalidRequestException;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.Student;
import com.skillswap.service.RequestService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab showing incoming and outgoing exchange requests with Accept, Reject, and Cancel actions.
 */
public class RequestsPanel extends JPanel {

    private final Student student;
    private final RequestService requestService = new RequestService();

    private final DefaultTableModel incomingModel = new DefaultTableModel(
            new String[]{"ID", "From", "Wants to learn", "Offers to teach", "Message", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final DefaultTableModel outgoingModel = new DefaultTableModel(
            new String[]{"ID", "To", "Wants to learn", "Offers to teach", "Message", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    private final JTable incomingTable = new JTable(incomingModel);
    private final JTable outgoingTable = new JTable(outgoingModel);
    private List<ExchangeRequest> incoming = new ArrayList<>();
    private List<ExchangeRequest> outgoing = new ArrayList<>();

    public RequestsPanel(Student student) {
        this.student = student;
        setLayout(new GridLayout(2, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UITheme.BG);

        UITheme.styleTable(incomingTable);
        UITheme.styleTable(outgoingTable);

        add(buildIncomingPanel());
        add(buildOutgoingPanel());

        refresh();
    }

    private JPanel buildIncomingPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder("Incoming requests (people asking YOU to teach them) - double-click a row to respond"));
        panel.add(new JScrollPane(incomingTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton acceptButton = UITheme.primaryButton("Accept selected");
        JButton rejectButton = UITheme.dangerButton("Reject selected");
        buttons.add(acceptButton);
        buttons.add(rejectButton);
        panel.add(buttons, BorderLayout.SOUTH);

        acceptButton.addActionListener(e -> respond(true));
        rejectButton.addActionListener(e -> respond(false));

        incomingTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) quickRespond();
            }
        });
        return panel;
    }

    private JPanel buildOutgoingPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder("Outgoing requests (requests YOU sent)"));
        panel.add(new JScrollPane(outgoingTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton cancelButton = UITheme.dangerButton("Cancel selected");
        JButton refreshButton = UITheme.secondaryButton("Refresh");
        buttons.add(cancelButton);
        buttons.add(refreshButton);
        panel.add(buttons, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> cancelSelected());
        refreshButton.addActionListener(e -> refresh());
        return panel;
    }

    public void refresh() {
        try {
            incoming = requestService.incomingFor(student);
            outgoing = requestService.outgoingFor(student);
        } catch (SkillSwapException e) {
            JOptionPane.showMessageDialog(this, "Could not load requests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            incoming = new ArrayList<>();
            outgoing = new ArrayList<>();
        }

        incomingModel.setRowCount(0);
        for (ExchangeRequest r : incoming) {
            incomingModel.addRow(new Object[]{
                    r.getRequestId(), r.getSender().getName(), r.getSkillRequested(),
                    r.getSkillOffered() != null ? r.getSkillOffered() : "-", r.getMessage(), r.getStatus()});
        }

        outgoingModel.setRowCount(0);
        for (ExchangeRequest r : outgoing) {
            outgoingModel.addRow(new Object[]{
                    r.getRequestId(), r.getReceiver().getName(), r.getSkillRequested(),
                    r.getSkillOffered() != null ? r.getSkillOffered() : "-", r.getMessage(), r.getStatus()});
        }
    }

    private int selectedIncomingModelRow() {
        int viewRow = incomingTable.getSelectedRow();
        return viewRow < 0 ? -1 : incomingTable.convertRowIndexToModel(viewRow);
    }

    private int selectedOutgoingModelRow() {
        int viewRow = outgoingTable.getSelectedRow();
        return viewRow < 0 ? -1 : outgoingTable.convertRowIndexToModel(viewRow);
    }

    private void respond(boolean accept) {
        int row = selectedIncomingModelRow();
        if (row < 0 || row >= incoming.size()) {
            JOptionPane.showMessageDialog(this, "Select an incoming request first.");
            return;
        }
        ExchangeRequest req = incoming.get(row);
        try {
            requestService.respond(req, student, accept);
            JOptionPane.showMessageDialog(this, "Request " + req.getStatus() + ".");
            refresh();
        } catch (InvalidRequestException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quickRespond() {
        int row = selectedIncomingModelRow();
        if (row < 0 || row >= incoming.size()) return;
        ExchangeRequest req = incoming.get(row);
        if (req.getStatus() != RequestStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "This request is already " + req.getStatus() + ".");
            return;
        }
        Object[] options = {"Accept", "Reject", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                req.getSender().getName() + " wants help with \"" + req.getSkillRequested() + "\".\n"
                        + "Message: " + req.getMessage(),
                "Respond to request",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) respond(true);
        else if (choice == 1) respond(false);
    }

    private void cancelSelected() {
        int row = selectedOutgoingModelRow();
        if (row < 0 || row >= outgoing.size()) {
            JOptionPane.showMessageDialog(this, "Select an outgoing request first.");
            return;
        }
        ExchangeRequest req = outgoing.get(row);
        try {
            requestService.cancel(req, student);
            refresh();
        } catch (InvalidRequestException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
