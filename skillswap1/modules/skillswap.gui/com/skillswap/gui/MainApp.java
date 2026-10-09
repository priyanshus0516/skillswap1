package com.skillswap.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * SkillSwap Application entry point.
 */
public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
