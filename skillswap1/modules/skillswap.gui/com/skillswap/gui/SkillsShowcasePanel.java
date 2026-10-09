package com.skillswap.gui;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillLevel;
import com.skillswap.model.Student;
import com.skillswap.service.AuthService;
import com.skillswap.service.SkillService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * "My Skills" tab: visual card showcase of all skills the student teaches or learns.
 * Cards provide lift-on-hover animation, skill leveling up, and removal.
 */
public class SkillsShowcasePanel extends JPanel {

    private static class Entry {
        final Skill skill; final SkillLevel level; final boolean teach;
        Entry(Skill skill, SkillLevel level, boolean teach) {
            this.skill = skill; this.level = level; this.teach = teach;
        }
    }

    private final Student student;
    private final SkillService skillService = new SkillService();
    private final AuthService authService = new AuthService();

    private final JPanel grid = new JPanel(new GridLayout(0, 3, 14, 14));
    private final JLabel summaryLabel = new JLabel();
    private final JLabel detailTitle = new JLabel("Click a skill card to see details");
    private final JLabel detailText = new JLabel(" ");
    private final JButton levelUpButton = UITheme.primaryButton("\u2B06 Level up");
    private final JButton removeButton = UITheme.dangerButton("Remove");
    private final List<SkillCard> cards = new ArrayList<>();

    private String filter = "ALL";
    private Entry selectedEntry;

    public SkillsShowcasePanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top summary and filter chips
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        summaryLabel.setFont(UITheme.FONT_HEADING);
        summaryLabel.setForeground(UITheme.PRIMARY_DARK);
        top.add(summaryLabel, BorderLayout.NORTH);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        chips.add(chip("All", "ALL", group, true));
        chips.add(chip("\uD83C\uDF93 I can teach", "TEACH", group, false));
        chips.add(chip("\uD83D\uDCDA I want to learn", "LEARN", group, false));
        top.add(chips, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Center card grid
        grid.setOpaque(false);
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.add(grid, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(gridWrap);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        // Bottom detail bar
        JPanel detail = UITheme.card();
        detail.setLayout(new BorderLayout(10, 4));
        detailTitle.setFont(UITheme.FONT_HEADING);
        detailText.setFont(UITheme.FONT_BODY);
        detailText.setForeground(UITheme.TEXT_MUTED);
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        texts.add(detailTitle);
        texts.add(detailText);
        detail.add(texts, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        levelUpButton.setEnabled(false);
        removeButton.setEnabled(false);
        actions.add(levelUpButton);
        actions.add(removeButton);
        detail.add(actions, BorderLayout.EAST);
        add(detail, BorderLayout.SOUTH);

        levelUpButton.addActionListener(e -> levelUp());
        removeButton.addActionListener(e -> removeSelected());

        refresh();
    }

    private JToggleButton chip(String text, String value, ButtonGroup group, boolean selected) {
        JToggleButton b = new JToggleButton(text, selected);
        b.setFont(UITheme.FONT_BUTTON);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.setOpaque(true);
        Runnable paint = () -> {
            b.setBackground(b.isSelected() ? UITheme.PRIMARY : new Color(0xE7ECF5));
            b.setForeground(b.isSelected() ? Color.WHITE : UITheme.PRIMARY_DARK);
        };
        paint.run();
        b.addItemListener(e -> paint.run());
        b.addActionListener(e -> { filter = value; refresh(); });
        group.add(b);
        return b;
    }

    public void refresh() {
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Skill, SkillLevel> e : student.getTeachSkills().entrySet())
            entries.add(new Entry(e.getKey(), e.getValue(), true));
        for (Map.Entry<Skill, SkillLevel> e : student.getLearnSkills().entrySet())
            entries.add(new Entry(e.getKey(), e.getValue(), false));

        summaryLabel.setText("You own " + student.getTeachSkills().size() + " skill(s) to teach and are learning "
                + student.getLearnSkills().size() + ".");

        grid.removeAll();
        cards.clear();
        Entry stillSelected = null;
        for (Entry en : entries) {
            if (filter.equals("TEACH") && !en.teach) continue;
            if (filter.equals("LEARN") && en.teach) continue;
            SkillCard card = new SkillCard(en);
            cards.add(card);
            grid.add(card);
            if (selectedEntry != null && selectedEntry.skill.equals(en.skill) && selectedEntry.teach == en.teach) {
                stillSelected = en;
            }
        }
        if (cards.isEmpty()) {
            JLabel empty = new JLabel("Nothing here yet - add skills in the My Profile tab.", SwingConstants.CENTER);
            empty.setForeground(UITheme.TEXT_MUTED);
            grid.add(empty);
        }
        select(stillSelected);
        grid.revalidate();
        grid.repaint();
    }

    private void select(Entry en) {
        selectedEntry = en;
        for (SkillCard c : cards) { c.repaint(); }
        if (en == null) {
            detailTitle.setText("Click a skill card to see details");
            detailText.setText(" ");
            levelUpButton.setEnabled(false);
            removeButton.setEnabled(false);
            return;
        }

        int others = 0;
        try {
            List<Student> all = authService.getAllStudents();
            for (Student s : all) {
                if (s.getUserId() == student.getUserId()) continue;
                boolean relevant = en.teach ? s.getLearnSkills().containsKey(en.skill)
                                            : s.getTeachSkills().containsKey(en.skill);
                if (relevant) others++;
            }
        } catch (SkillSwapException e) {
            System.err.println("Could not query students for skill stats: " + e.getMessage());
        }

        detailTitle.setText(en.skill + "  -  " + en.level + (en.teach ? " (teaching)" : " (learning)"));
        detailText.setText(en.teach
                ? others + " other student(s) want to learn this from you."
                : others + " other student(s) can teach you this.");
        levelUpButton.setEnabled(en.level != SkillLevel.EXPERT);
        removeButton.setEnabled(true);
    }

    private void levelUp() {
        if (selectedEntry == null || selectedEntry.level == SkillLevel.EXPERT) return;
        SkillLevel next = SkillLevel.values()[selectedEntry.level.ordinal() + 1];
        try {
            if (selectedEntry.teach) {
                skillService.updateTeachSkillLevel(student, selectedEntry.skill, next);
            } else {
                skillService.updateLearnSkillLevel(student, selectedEntry.skill, next);
            }
            Entry keep = new Entry(selectedEntry.skill, next, selectedEntry.teach);
            selectedEntry = keep;
            refresh();
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Could not update skill level: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelected() {
        if (selectedEntry == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Remove \"" + selectedEntry.skill + "\"?",
                "Remove skill", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            if (selectedEntry.teach) {
                skillService.removeTeachSkill(student, selectedEntry.skill);
            } else {
                skillService.removeLearnSkill(student, selectedEntry.skill);
            }
            selectedEntry = null;
            refresh();
        } catch (SkillSwapException ex) {
            JOptionPane.showMessageDialog(this, "Could not remove skill: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static Color colorFor(String category) {
        if (category == null) return new Color(0x7F8C8D);
        switch (category) {
            case "Technical":  return new Color(0x2D6CDF);
            case "Music":      return new Color(0x8E44AD);
            case "Design":     return new Color(0xE67E22);
            case "Soft Skill": return new Color(0x16A085);
            case "Language":   return new Color(0xD63384);
            case "Games":      return new Color(0x27AE60);
            default:           return new Color(0x7F8C8D);
        }
    }

    private class SkillCard extends JPanel {
        private final Entry entry;
        private boolean hover;

        SkillCard(Entry entry) {
            this.entry = entry;
            setOpaque(false);
            setPreferredSize(new Dimension(200, 118));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(entry.skill + " - click for details");
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { select(entry); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color base = colorFor(entry.skill.getCategory());
            int lift = hover ? 0 : 5;

            // Soft drop shadow
            g2.setColor(new Color(0, 0, 0, hover ? 45 : 25));
            g2.fill(new RoundRectangle2D.Float(3, 8, w - 6, h - 8, 18, 18));

            // Gradient card body
            g2.setPaint(new GradientPaint(0, lift, base.brighter(), w, h, base.darker()));
            g2.fill(new RoundRectangle2D.Float(0, lift, w, h - 8, 18, 18));

            boolean isSelected = selectedEntry != null && selectedEntry.skill.equals(entry.skill)
                    && selectedEntry.teach == entry.teach;
            if (isSelected) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Float(2, lift + 2, w - 4, h - 12, 16, 16));
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(shorten(entry.skill.getName(), 18), 14, lift + 28);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(entry.skill.getCategory() == null ? "" : entry.skill.getCategory(), 14, lift + 45);

            String badge = entry.teach ? "TEACH" : "LEARN";
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            int bw = g2.getFontMetrics().stringWidth(badge) + 14;
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fill(new RoundRectangle2D.Float(w - bw - 10, lift + 10, bw, 18, 18, 18));
            g2.setColor(Color.WHITE);
            g2.drawString(badge, w - bw - 3, lift + 23);

            int filled = entry.level.ordinal() + 1;
            int segW = (w - 28 - 3 * 6) / 4;
            for (int i = 0; i < 4; i++) {
                g2.setColor(i < filled ? Color.WHITE : new Color(255, 255, 255, 70));
                g2.fill(new RoundRectangle2D.Float(14 + i * (segW + 6), lift + 62, segW, 8, 8, 8));
            }
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(entry.level.name(), 14, lift + 88);

            g2.dispose();
        }

        private String shorten(String s, int max) {
            return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
        }
    }
}
