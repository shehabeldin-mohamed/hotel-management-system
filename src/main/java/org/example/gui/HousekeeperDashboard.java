package org.example.gui;

import org.example.facility.CleaningTask;
import org.example.hr.Housekeeper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import static org.example.gui.LoginWindow.*;

public class HousekeeperDashboard extends JFrame {

    public HousekeeperDashboard(LoginWindow loginWindow, Housekeeper housekeeper) {
        setTitle("InnPoint - Housekeeper Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onLogoutClicked(loginWindow);
            }
        });
        setSize(900, 550);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(40, 60, 40, 60));

        // Header
        JLabel title = new JLabel("Housekeeper Dashboard");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome, " + housekeeper.getFirstName() + " " + housekeeper.getLastName());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(SUBTLE_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(50, 50, 80));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Role Info
        JLabel sectionLabel = makeInfoLabel("Assigned Section:   " + housekeeper.getAssignedSection());
        JLabel cartLabel    = makeInfoLabel("Cart Number:        " + housekeeper.getCartNumber());
        JLabel empLabel     = makeInfoLabel("Employee ID:        " + housekeeper.getEmployeeId());

        // Assigned Cleaning Tasks (read-only)
        JLabel tasksHeader = makeInfoLabel("Assigned Cleaning Tasks:");
        tasksHeader.setForeground(TEXT_COLOR);

        DefaultListModel<String> taskModel = new DefaultListModel<>();
        for (CleaningTask task : housekeeper.viewAssignedCleaningTasks()) {
            taskModel.addElement("Room " + task.getRoom().getNumber()
                    + "  |  " + task.getStatus()
                    + "  |  " + (task.getDescription() != null ? task.getDescription() : "No description"));
        }
        if (taskModel.isEmpty()) taskModel.addElement("No cleaning tasks assigned.");

        JList<String> taskList = new JList<>(taskModel);
        taskList.setBackground(FIELD_BG);
        taskList.setForeground(TEXT_COLOR);
        taskList.setFont(new Font("Arial", Font.PLAIN, 13));
        taskList.setBorder(new EmptyBorder(5, 8, 5, 8));
        taskList.setEnabled(false);

        JScrollPane scroll = new JScrollPane(taskList);
        scroll.getViewport().setBackground(FIELD_BG);
        scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Planned notice
        JLabel plannedLabel = new JLabel("Full task management functionality is planned for a future release.");
        plannedLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        plannedLabel.setForeground(SUBTLE_TEXT);
        plannedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Logout
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 13));
        logoutBtn.setForeground(TEXT_COLOR);
        logoutBtn.setBackground(ACCENT_RED);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> onLogoutClicked(loginWindow));

        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(15));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(15));
        panel.add(sectionLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(cartLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(empLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(tasksHeader);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scroll);
        panel.add(Box.createVerticalStrut(10));
        panel.add(plannedLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(logoutBtn);

        setContentPane(panel);
    }

    private JLabel makeInfoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(SUBTLE_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void onLogoutClicked(LoginWindow loginWindow) {
        loginWindow.showRoleSelection();
        loginWindow.setVisible(true);
        dispose();
    }
}
