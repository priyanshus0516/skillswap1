package com.skillswap.gui;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillLevel;
import com.skillswap.model.Student;
import com.skillswap.service.SkillService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Tab where a student manages their profile and adds/removes the skills they teach or learn.
 */
public class ProfilePanel extends JPanel {

    private final Student student;
    private final SkillService skillService = new SkillService();

    private final JTextArea profileArea = new JTextArea(4, 40);
    private final DefaultListModel<Skill> teachModel = new DefaultListModel<>();
    private final DefaultListModel<Skill> learnModel = new DefaultListModel<>();
    private final JList<Skill> teachList = new JList<>(teachModel);
    private final JList<Skill> learnList = new JList<>(learnModel);

    public ProfilePanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UITheme.BG);

        profileArea.setEditable(false);
        profileArea.setLineWrap(true);
        profileArea.setWrapStyleWord(true);
        add(new JScrollPane(profileArea), BorderLayout.NORTH);

        JPanel skillForm = new JPanel(new GridBagLayout());
        skillForm.setBorder(BorderFactory.createTitledBorder("Add a skill"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField skillNameField = new JTextField(15);
        JComboBox<String> categoryBox = new JComboBox<>(
                new String[]{"Technical", "Music", "Design", "Soft Skill", "Language", "Games", "Other"});
        JComboBox<SkillLevel> levelBox = new JComboBox<>(SkillLevel.values());
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"I can TEACH this", "I want to LEARN this"});

        gbc.gridx = 0; gbc.gridy = 0; skillForm.add(new JLabel("Skill name:"), gbc);
        gbc.gridx = 1; skillForm.add(skillNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; skillForm.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; skillForm.add(categoryBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; skillForm.add(new JLabel("Level:"), gbc);
        gbc.gridx = 1; skillForm.add(levelBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; skillForm.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; skillForm.add(typeBox, gbc);

        JButton addButton = UITheme.primaryButton("Add Skill");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        skillForm.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String name = skillNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a skill name.");
                return;
            }
            try {
                Skill skill = skillService.findOrCreateSkill(name, (String) categoryBox.getSelectedItem());
                SkillLevel level = (SkillLevel) levelBox.getSelectedItem();
                if (typeBox.getSelectedIndex() == 0) {
                    skillService.addTeachSkill(student, skill, level);
                } else {
                    skillService.addLearnSkill(student, skill, level);
                }
                skillNameField.setText("");
                refresh();
            } catch (SkillSwapException ex) {
                JOptionPane.showMessageDialog(this, "Could not add skill: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(skillForm, BorderLayout.CENTER);
        add(buildSkillLists(), BorderLayout.EAST);

        JButton refreshButton = UITheme.secondaryButton("Refresh profile");
        refreshButton.addActionListener(e -> refresh());
        add(refreshButton, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        profileArea.setText(student.displayProfile());
        teachModel.clear();
        for (Map.Entry<Skill, SkillLevel> e : student.getTeachSkills().entrySet()) teachModel.addElement(e.getKey());
        learnModel.clear();
        for (Map.Entry<Skill, SkillLevel> e : student.getLearnSkills().entrySet()) learnModel.addElement(e.getKey());
    }

    private JPanel buildSkillLists() {
        JPanel p = new JPanel(new GridLayout(2, 1, 8, 8));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(230, 0));
        p.add(skillListCard("I can teach (double-click to remove)", teachList, true));
        p.add(skillListCard("I want to learn (double-click to remove)", learnList, false));
        return p;
    }

    private JPanel skillListCard(String title, JList<Skill> list, boolean teach) {
        list.setFont(UITheme.FONT_BODY);
        list.setSelectionBackground(new Color(0xDCE7FB));
        list.setSelectionForeground(UITheme.PRIMARY_DARK);
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                Skill skill = list.getSelectedValue();
                if (skill == null) return;
                int ok = JOptionPane.showConfirmDialog(ProfilePanel.this,
                        "Remove \"" + skill + "\" from your list?", "Remove skill", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;

                try {
                    if (teach) {
                        skillService.removeTeachSkill(student, skill);
                    } else {
                        skillService.removeLearnSkill(student, skill);
                    }
                    refresh();
                } catch (SkillSwapException ex) {
                    JOptionPane.showMessageDialog(ProfilePanel.this, "Could not remove skill: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createTitledBorder(title));
        card.add(new JScrollPane(list), BorderLayout.CENTER);
        return card;
    }
}
