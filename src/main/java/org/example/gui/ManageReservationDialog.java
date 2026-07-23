package org.example.gui;

import org.example.booking.Reservation;
import org.example.booking.ReservationRoomType;
import org.example.booking.ReservationStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static org.example.gui.LoginWindow.*;

public class ManageReservationDialog extends JDialog {

    private final Reservation reservation;
    private final JLabel statusLabel;
    private final JButton cancelBtn;
    private final Runnable onCancelledCallback;

    public ManageReservationDialog(JFrame parent, Reservation reservation, Runnable onCancelledCallback) {
        super(parent, "Manage Reservation", true);
        this.reservation = reservation;
        this.onCancelledCallback = onCancelledCallback;

        setSize(600, 560);
        setLocationRelativeTo(parent);
        setResizable(false);

        statusLabel = makeFieldLabel("Status: " + reservation.getReservationStatus());
        cancelBtn   = buildAccentButton("Cancel Reservation", new Color(180, 40, 40));
        cancelBtn.setEnabled(reservation.getReservationStatus() == ReservationStatus.Confirmed);
        cancelBtn.addActionListener(e -> onCancelReservationClicked());

        JButton closeBtn = buildSecondaryButton("Close");
        closeBtn.addActionListener(e -> onCloseClicked());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(BG_COLOR);
        btnRow.add(closeBtn);
        btnRow.add(cancelBtn);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        content.add(makeTitleLabel("Reservation Details"));
        content.add(Box.createVerticalStrut(20));
        content.add(makeFieldLabel("Reference:  " + reservation.getReference()));
        content.add(Box.createVerticalStrut(6));
        content.add(makeFieldLabel("Check-In:   " + reservation.getStartDate()));
        content.add(Box.createVerticalStrut(6));
        content.add(makeFieldLabel("Check-Out:  " + reservation.getEndDate()));
        content.add(Box.createVerticalStrut(6));
        content.add(makeFieldLabel("Customer:   " + reservation.getCustomer().getFirstName()
                + " " + reservation.getCustomer().getLastName()));
        content.add(Box.createVerticalStrut(6));
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(makeFieldLabel("Booked Room Types:"));
        content.add(Box.createVerticalStrut(6));
        content.add(buildRoomTypesList());
        content.add(Box.createVerticalStrut(16));
        content.add(makeFieldLabel(String.format("Total Price:  $%.2f", reservation.getTotalPrice())));
        content.add(Box.createVerticalGlue());
        content.add(Box.createVerticalStrut(20));
        content.add(btnRow);

        setContentPane(content);
    }

    // Business Methods
    private void cancelReservation() {
        try {
            reservation.cancelReservation();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cancelling reservation: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Listeners
    private void onCancelReservationClicked() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel reservation " + reservation.getReference() + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        cancelReservation();
        statusLabel.setText("Status: " + reservation.getReservationStatus());
        cancelBtn.setEnabled(false);
        if (onCancelledCallback != null) onCancelledCallback.run();
    }

    private void onCloseClicked() {
        dispose();
    }

    // UI Helpers
    private JScrollPane buildRoomTypesList() {
        DefaultListModel<ReservationRoomType> model = new DefaultListModel<>();
        for (ReservationRoomType rrt : reservation.getReservationRoomTypes()) {
            model.addElement(rrt);
        }
        JList<ReservationRoomType> list = new JList<>(model);
        list.setBackground(FIELD_BG);
        list.setForeground(TEXT_COLOR);
        list.setFont(new Font("Arial", Font.PLAIN, 13));
        list.setBorder(new EmptyBorder(5, 8, 5, 8));
        list.setEnabled(false);

        JScrollPane scroll = new JScrollPane(list);
        scroll.getViewport().setBackground(FIELD_BG);
        scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        return scroll;
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
