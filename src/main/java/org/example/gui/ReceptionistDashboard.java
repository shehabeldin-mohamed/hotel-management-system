package org.example.gui;

import org.example.HibernateUtil;
import org.example.booking.Customer;
import org.example.booking.Reservation;
import org.example.booking.ReservationStatus;
import org.example.hr.Receptionist;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static org.example.gui.LoginWindow.*;

public class ReceptionistDashboard extends JFrame {

    private final LoginWindow loginWindow;
    private final Receptionist receptionist;

    private final DefaultListModel<Customer>    customerModel    = new DefaultListModel<>();
    private final DefaultListModel<Reservation> reservationModel = new DefaultListModel<>();

    private final JList<Customer>    customerList    = new JList<>(customerModel);
    private final JList<Reservation> reservationList = new JList<>(reservationModel);

    public ReceptionistDashboard(LoginWindow loginWindow, Receptionist receptionist) {
        this.loginWindow  = loginWindow;
        this.receptionist = receptionist;

        setTitle("InnPoint - Receptionist Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onLogoutClicked();
            }
        });
        setSize(1100, 700);
        setMinimumSize(new Dimension(750, 480));
        setLocationRelativeTo(null);

        setContentPane(buildMainPanel());
        loadCustomers();
    }

    private void loadCustomers() {
        customerModel.clear();
        reservationModel.clear();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Customer> customers = session.createQuery("from Customer", Customer.class).list();
            for (Customer c : customers) customerModel.addElement(c);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReservationsForCustomer(Customer customer) {
        reservationModel.clear();
        for (Reservation r : customer.getReservations()) {
            reservationModel.addElement(r);
        }
    }

    // UI Construction
    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        panel.add(buildHeader(),    BorderLayout.NORTH);
        panel.add(buildListsPane(), BorderLayout.CENTER);
        panel.add(buildFooter(),    BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG_COLOR);

        JLabel title = new JLabel("Reservations by Customer");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome, " + receptionist.getFirstName() + " " + receptionist.getLastName());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(SUBTLE_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 50, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(12));
        header.add(sep);
        header.add(Box.createVerticalStrut(10));
        return header;
    }

    private JSplitPane buildListsPane() {
        styleList(customerList);
        styleList(reservationList);

        customerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerList.addListSelectionListener(e -> onCustomerSelected(e.getValueIsAdjusting()));

        JScrollPane leftScroll = new JScrollPane(customerList);
        leftScroll.getViewport().setBackground(Color.GREEN);
        leftScroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
        leftScroll.setColumnHeaderView(makeFieldLabel("Customers"));

        JScrollPane rightScroll = new JScrollPane(reservationList);
        rightScroll.getViewport().setBackground(FIELD_BG);
        rightScroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
        rightScroll.setColumnHeaderView(makeFieldLabel("Reservations"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        split.setDividerLocation(340);
        split.setBackground(BG_COLOR);
        split.setBorder(null);
        return split;
    }

    private JPanel buildFooter() {
        JButton checkInBtn = buildAccentButton("Check In Customer", ACCENT_RED);
        checkInBtn.addActionListener(e -> onCheckInClicked());

        JButton completeBtn = buildAccentButton("Complete Reservation", new Color(46, 125, 50));
        completeBtn.addActionListener(e -> onCompleteClicked());

        JButton manageBtn = buildAccentButton("Manage Reservation", ACCENT_BLUE);
        manageBtn.addActionListener(e -> onManageClicked());

        JButton refreshBtn = buildSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> onRefreshClicked());

        JButton logoutBtn = buildSecondaryButton("Logout");
        logoutBtn.addActionListener(e -> onLogoutClicked());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        footer.setBackground(BG_COLOR);
        footer.add(logoutBtn);
        footer.add(refreshBtn);
        footer.add(manageBtn);
        footer.add(checkInBtn);
        footer.add(completeBtn);
        return footer;
    }

    // Listeners
    private void onCustomerSelected(boolean isAdjusting) {
        if (isAdjusting) return;
        Customer selected = customerList.getSelectedValue();
        if (selected == null) return;
        showReservationsForCustomer(selected);
    }

    private void onCheckInClicked() {
        Reservation selected = reservationList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selected.getReservationStatus() != ReservationStatus.Confirmed) {
            JOptionPane.showMessageDialog(this,
                    "Only Confirmed reservations can be checked in.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new CheckInDialog(this, selected, receptionist, this::loadCustomers).setVisible(true);
    }

    private void onCompleteClicked() {
        Reservation selected = reservationList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selected.getReservationStatus() != ReservationStatus.CheckedIn) {
            JOptionPane.showMessageDialog(this,
                    "Only checked-in reservations can be completed.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Complete reservation " + selected.getReference() + "?\nThis will check the guest out.",
                "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            selected.completeReservation();
            loadCustomers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error completing reservation: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onManageClicked() {
        Reservation selected = reservationList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ManageReservationDialog(this, selected, this::loadCustomers).setVisible(true);
    }

    private void onRefreshClicked() {
        loadCustomers();
    }

    private void onLogoutClicked() {
        loginWindow.showRoleSelection();
        loginWindow.setVisible(true);
        dispose();
    }

    // UI Helpers
    private <T> void styleList(JList<T> list) {
        list.setBackground(FIELD_BG);
        list.setForeground(TEXT_COLOR);
        list.setFont(new Font("Arial", Font.PLAIN, 13));
        list.setBorder(new EmptyBorder(5, 8, 5, 8));
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
