package com.skillswap.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Central styling constants, colors, fonts, and UI component builders.
 */
public final class UITheme {

    private UITheme() { }

    public static final Color PRIMARY      = new Color(0x2D6CDF);
    public static final Color PRIMARY_DARK = new Color(0x1E4FA3);
    public static final Color ACCENT       = new Color(0x27AE60);
    public static final Color DANGER       = new Color(0xE74C3C);
    public static final Color BG           = new Color(0xF4F6FA);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color BORDER       = new Color(0xDDE3EC);
    public static final Color TEXT_MUTED   = new Color(0x6B7280);

    public static final Font FONT_TITLE    = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_HEADING  = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_BODY     = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BUTTON   = new Font("SansSerif", Font.BOLD, 13);

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, new Color(0xE7ECF5), PRIMARY_DARK);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hoverColor = bg.darker();
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
            @Override public void mousePressed(MouseEvent e) { b.setBackground(hoverColor.darker()); }
            @Override public void mouseReleased(MouseEvent e) { b.setBackground(b.contains(e.getPoint()) ? hoverColor : bg); }
        });
    }

    public static JTextField liveSearchField(String placeholder, Consumer<String> onChange) {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.accept(field.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.accept(field.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.accept(field.getText()); }
        });
        return field;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(26);
        table.setSelectionBackground(new Color(0xDCE7FB));
        table.setSelectionForeground(PRIMARY_DARK);
        table.setGridColor(BORDER);
        table.setAutoCreateRowSorter(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BUTTON);
        header.setBackground(new Color(0xEEF2FA));
        header.setForeground(PRIMARY_DARK);
    }

    public static JPanel statCard(String value, String label, Color accent, Runnable onClick) {
        JPanel p = card();
        p.setLayout(new BorderLayout(4, 4));
        p.setPreferredSize(new Dimension(180, 100));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel valueLabel = new JLabel(value, SwingConstants.LEFT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLabel.setForeground(accent);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(FONT_SUBTITLE);
        textLabel.setForeground(TEXT_MUTED);

        p.add(valueLabel, BorderLayout.CENTER);
        p.add(textLabel, BorderLayout.SOUTH);

        Color normalBorder = BORDER;
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 2, true),
                        new EmptyBorder(15, 15, 15, 15)));
            }
            @Override public void mouseExited(MouseEvent e) {
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(normalBorder, 1, true),
                        new EmptyBorder(16, 16, 16, 16)));
            }
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
        return p;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(PRIMARY_DARK);
        return l;
    }

    public static void styleWindowBackground(Container root) {
        root.setBackground(BG);
    }

    public static JComponent avatar(String name, int diameter) {
        String initial = (name == null || name.isEmpty()) ? "?" : name.trim().substring(0, 1).toUpperCase();
        JLabel label = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, diameter / 2));
        label.setPreferredSize(new Dimension(diameter, diameter));
        label.setOpaque(false);
        return label;
    }
}
