package org.example.gui;

import org.example.facility.MaintenanceRequest;
import org.example.hr.Technician;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import static org.example.gui.LoginWindow.*;

public class TechnicianDashboard extends JFrame {

    public TechnicianDashboard(LoginWindow loginWindow, Technician technician) {
        setTitle("InnPoint - Technician Dashboard");
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

        // --- Header ---
        JLabel title = new JLabel("Technician Dashboard");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome, " + technician.getFirstName() + " " + technician.getLastName());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(SUBTLE_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(50, 50, 80));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Role Info
        JLabel specLabel  = makeInfoLabel("Specialization:   " + technician.getSpecialization());
        JLabel empLabel   = makeInfoLabel("Employee ID:      " + technician.getEmployeeId());
        JLabel certsLabel = makeInfoLabel("Certificates:     " + String.join(", ", technician.getCertificates()));

        // Assigned Maintenance Requests (read-only)
        JLabel requestsHeader = makeInfoLabel("Assigned Maintenance Requests:");
        requestsHeader.setForeground(TEXT_COLOR);

        DefaultListModel<String> requestModel = new DefaultListModel<>();
        for (MaintenanceRequest req : technician.viewAssignedMaintenanceRequests()) {
            requestModel.addElement("Room " + req.getRoom().getNumber()
                    + "  |  " + req.getStatus()
                    + "  |  " + (req.getDescription() != null ? req.getDescription() : "No description"));
        }
        if (requestModel.isEmpty()) requestModel.addElement("No maintenance requests assigned.");

        JList<String> requestList = new JList<>(requestModel);
        requestList.setBackground(FIELD_BG);
        requestList.setForeground(TEXT_COLOR);
        requestList.setFont(new Font("Arial", Font.PLAIN, 13));
        requestList.setBorder(new EmptyBorder(5, 8, 5, 8));
        requestList.setEnabled(false);

        JScrollPane scroll = new JScrollPane(requestList);
        scroll.getViewport().setBackground(FIELD_BG);
        scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Planned notice
        JLabel plannedLabel = new JLabel("Full maintenance management functionality is planned for a future release.");
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
        panel.add(specLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(empLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(certsLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(requestsHeader);
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
