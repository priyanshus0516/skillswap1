package com.skillswap.gui;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.*;
import com.skillswap.service.MatchingService;
import com.skillswap.service.RequestService;
import com.skillswap.service.SessionService;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

/**
 * Landing tab shown right after login with interactive stat cards.
 */
public class HomePanel extends JPanel {

    private final Student student;
    private final IntConsumer goToTab;
    private final RequestService requestService = new RequestService();
    private final SessionService sessionService = new SessionService();
    private final MatchingService matchingService = new MatchingService();

    private final JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));

    public HomePanel(Student student, IntConsumer goToTab) {
        this.student = student;
        this.goToTab = goToTab;
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel greeting = new JLabel("Welcome back, " + firstName(student.getName()) + " \uD83D\uDC4B");
        greeting.setFont(UITheme.FONT_TITLE.deriveFont(22f));
        greeting.setForeground(UITheme.PRIMARY_DARK);

        JLabel hint = new JLabel("Click a card below to jump straight to that section.");
        hint.setFont(UITheme.FONT_SUBTITLE);
        hint.setForeground(UITheme.TEXT_MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(greeting);
        header.add(hint);

        cardsRow.setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(cardsRow, BorderLayout.CENTER);

        refresh();
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        return fullName.trim().split("\\s+")[0];
    }

    public void refresh() {
        cardsRow.removeAll();

        int matchCount = 0;
        int pendingIncoming = 0;
        int upcomingSessions = 0;

        try {
            matchCount = matchingService.findMatchesFor(student).size();
            for (ExchangeRequest r : requestService.incomingFor(student)) {
                if (r.getStatus() == RequestStatus.PENDING) pendingIncoming++;
            }
            for (Session s : sessionService.sessionsFor(student)) {
                if (s.getStatus() == SessionStatus.SCHEDULED) upcomingSessions++;
            }
        } catch (SkillSwapException e) {
            System.err.println("Error refreshing home dashboard stats: " + e.getMessage());
        }

        cardsRow.add(UITheme.statCard(String.valueOf(student.getTeachSkills().size()), "Skills you own",
                new Color(0x8E44AD), () -> goToTab.accept(1)));
        cardsRow.add(UITheme.statCard(String.valueOf(matchCount), "Skill matches found",
                UITheme.PRIMARY, () -> goToTab.accept(3)));
        cardsRow.add(UITheme.statCard(String.valueOf(pendingIncoming), "Pending requests for you",
                UITheme.ACCENT, () -> goToTab.accept(4)));
        cardsRow.add(UITheme.statCard(String.valueOf(upcomingSessions), "Upcoming sessions",
                UITheme.PRIMARY_DARK, () -> goToTab.accept(5)));
        cardsRow.add(UITheme.statCard(String.format("%.1f", student.getAverageRating()), "Your average rating",
                UITheme.DANGER, () -> goToTab.accept(6)));

        cardsRow.revalidate();
        cardsRow.repaint();
    }
}
