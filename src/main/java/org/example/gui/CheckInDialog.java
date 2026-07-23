package org.example.gui;

import org.example.booking.Reservation;
import org.example.booking.ReservationRoomType;
import org.example.facility.Room;
import org.example.facility.RoomType;
import org.example.hr.Receptionist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.example.gui.LoginWindow.*;

public class CheckInDialog extends JDialog {

    private final Reservation reservation;
    private final Receptionist receptionist;
    private final Runnable onCheckedInCallback;
    private final Map<ReservationRoomType, JComboBox<Room>> roomCombos = new HashMap<>();
    private final JTextField notesField = new JTextField();

    public CheckInDialog(JFrame parent, Reservation reservation, Receptionist receptionist, Runnable onCheckedInCallback) {
        super(parent, "Check In Customer", true);
        this.reservation = reservation;
        this.receptionist = receptionist;
        this.onCheckedInCallback = onCheckedInCallback;

        setSize(650, 600);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        content.add(makeTitleLabel("Check In Customer"));
        content.add(Box.createVerticalStrut(6));
        content.add(makeFieldLabel("Reservation: " + reservation.getReference()));
        content.add(Box.createVerticalStrut(4));
        content.add(makeFieldLabel("Customer: " + reservation.getCustomer().getFirstName()
                + " " + reservation.getCustomer().getLastName()));
        content.add(Box.createVerticalStrut(4));
        content.add(makeFieldLabel("Dates: " + reservation.getStartDate() + "  →  " + reservation.getEndDate()));
        content.add(Box.createVerticalStrut(16));
        content.add(makeFieldLabel("Assign Rooms:"));
        content.add(Box.createVerticalStrut(8));
        content.add(buildRoomAssignmentPanel());
        content.add(Box.createVerticalStrut(16));
        content.add(makeFieldLabel("Notes (optional):"));
        content.add(Box.createVerticalStrut(4));

        styleTextField(notesField);
        content.add(notesField);
        content.add(Box.createVerticalStrut(20));
        content.add(buildButtonRow());

        setContentPane(content);
    }

    // Business Methods
    private void performCheckIn() {
        Map<Long, Room> assignments = new HashMap<>();
        Set<Long> usedRoomIds = new HashSet<>();
        for (Map.Entry<ReservationRoomType, JComboBox<Room>> entry : roomCombos.entrySet()) {
            Room selected = (Room) entry.getValue().getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Please assign a room for every room type.", "Missing Assignment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!usedRoomIds.add(selected.getId())) {
                JOptionPane.showMessageDialog(this,
                        "Room " + selected.getNumber() + " has been assigned more than once. Please select a different room.",
                        "Duplicate Room Assignment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            assignments.put(entry.getKey().getId(), selected);
        }
        try {
            reservation.performCheckIn(assignments, notesField.getText().trim(), receptionist);
            if (onCheckedInCallback != null) onCheckedInCallback.run();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Check-in failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Listeners
    private void onConfirmClicked() {
        performCheckIn();
    }
    private void onCancelClicked() {
        dispose();
    }

    // UI Helpers
    private JPanel buildRoomAssignmentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);

        for (ReservationRoomType rrt : reservation.getReservationRoomTypes()) {
            RoomType roomType = rrt.getRoomType();
            List<Room> available = RoomType.SearchAvailableRoomsForCheckIn(roomType.getId());

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(BG_COLOR);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

            JLabel label = new JLabel(rrt.toString());
            label.setForeground(TEXT_COLOR);
            label.setFont(new Font("Arial", Font.PLAIN, 13));

            JComboBox<Room> combo = new JComboBox<>();
            combo.setBackground(FIELD_BG);
            combo.setForeground(TEXT_COLOR);
            combo.setFont(new Font("Arial", Font.PLAIN, 13));

            if (available.isEmpty()) {
                combo.addItem(null);
                combo.setEnabled(false);
                label.setText(rrt + "  ⚠ No rooms available");
                label.setForeground(Color.ORANGE);
            } else {
                for (Room r : available) combo.addItem(r);
            }

            roomCombos.put(rrt, combo);
            row.add(label, BorderLayout.CENTER);
            row.add(combo, BorderLayout.EAST);
            panel.add(row);
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }

    private JPanel buildButtonRow() {
        JButton confirmBtn = buildAccentButton("Confirm Check In", ACCENT_BLUE);
        confirmBtn.addActionListener(e -> onConfirmClicked());

        JButton cancelBtn = buildSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> onCancelClicked());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setBackground(BG_COLOR);
        row.add(cancelBtn);
        row.add(confirmBtn);
        return row;
    }

    private JLabel makeTitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.BOLD, 22));
        l.setForeground(TEXT_COLOR);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(SUBTLE_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JButton buildAccentButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(color);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(SUBTLE_TEXT);
        btn.setBackground(BG_COLOR);
        btn.setBorder(BorderFactory.createLineBorder(SUBTLE_TEXT, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
