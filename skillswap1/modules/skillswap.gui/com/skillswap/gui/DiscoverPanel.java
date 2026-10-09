package com.skillswap.gui;

import com.skillswap.exception.DataAccessException;
import com.skillswap.exception.InvalidRequestException;
import com.skillswap.model.Skill;
import com.skillswap.model.Student;
import com.skillswap.service.MatchingService;
import com.skillswap.service.RequestService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab that discovers compatible learning partners for the logged-in student.
 */
public class DiscoverPanel extends JPanel {

    private final Student student;
    private final MatchingService matchingService = new MatchingService();
    private final RequestService requestService = new RequestService();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Partner", "They can teach you", "You can teach them", "Mutual?"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private List<MatchingService.Match> currentMatches = new ArrayList<>();

    public DiscoverPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UITheme.BG);

        UITheme.styleTable(table);
        table.setRowSorter(sorter);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Students who can teach you something you want to learn:"), BorderLayout.NORTH);

        JTextField searchField = UITheme.liveSearchField("Search by name or skill...", text -> filter(text));
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(new JLabel("\uD83D\uDD0D  "), BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchWrap, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        JButton refreshButton = UITheme.secondaryButton("Find matches");
        JButton sendRequestButton = UITheme.primaryButton("Send exchange request to selected");
        southPanel.add(refreshButton);
        southPanel.add(sendRequestButton);
        add(southPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refresh());
        sendRequestButton.addActionListener(e -> sendRequest());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) sendRequest();
            }
        });

        refresh();
    }

    public void refresh() {
        try {
            currentMatches = matchingService.findMatchesFor(student);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "Error finding matches: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            currentMatches = new ArrayList<>();
        }

        model.setRowCount(0);
        for (MatchingService.Match m : currentMatches) {
            model.addRow(new Object[]{
                    m.partner.getName(),
                    m.theyCanTeachYou,
                    m.youCanTeachThem,
                    m.isMutual() ? "Yes" : "No"
            });
        }
    }

    private void filter(String text) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text.trim())));
        }
    }

    private void sendRequest() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || currentMatches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a partner from the table first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        MatchingService.Match match = currentMatches.get(modelRow);
        if (match.theyCanTeachYou.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No teachable skill found for this match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Skill wanted = match.theyCanTeachYou.get(0);
        Skill offered = match.youCanTeachThem.isEmpty() ? null : match.youCanTeachThem.get(0);

        String note = JOptionPane.showInputDialog(this,
                "Message to " + match.partner.getName() + " requesting help with \"" + wanted + "\":",
                "Send Exchange Request",
                JOptionPane.PLAIN_MESSAGE);
        if (note == null) return;

        try {
            requestService.sendRequest(student, match.partner, wanted, offered, note);
            JOptionPane.showMessageDialog(this, "Request sent to " + match.partner.getName() + "!");
        } catch (InvalidRequestException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not send request", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
