package org.example.gui;

import org.example.HibernateUtil;
import org.example.booking.Customer;
import org.example.booking.Reservation;
import org.example.booking.ReservationRoomType;
import org.example.facility.RoomType;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.*;
import java.util.List;

import static org.example.gui.LoginWindow.*;

public class CustomerDashboard extends JFrame {

    private final LoginWindow loginWindow;
    private Customer customer;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // Reservation flow state
    private LocalDate startDate;
    private LocalDate endDate;
    private List<RoomType> searchResults = new ArrayList<>();
    private final List<Map.Entry<RoomType, Integer>> selectedRooms = new ArrayList<>();
    private Reservation pendingReservation;

    // Panel references
    private ResultsPanel resultsPanel;
    private SummaryPanel summaryPanel;
    private ConfirmationPanel confirmationPanel;
    private MyReservationsPanel myReservationsPanel;
    private UpdatePersonalDataPanel updatePersonalDataPanel;
    private LoyaltyBalancePanel loyaltyBalancePanel;

    public CustomerDashboard(LoginWindow loginWindow, Customer customer) {
        this.loginWindow = loginWindow;
        this.customer = customer;

        setTitle("InnPoint - Customer Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });
        setSize(1100, 700);
        setMinimumSize(new Dimension(750, 520));
        setLocationRelativeTo(null);

        resultsPanel         = new ResultsPanel();
        summaryPanel         = new SummaryPanel();
        confirmationPanel    = new ConfirmationPanel();
        myReservationsPanel      = new MyReservationsPanel();
        updatePersonalDataPanel  = new UpdatePersonalDataPanel();
        loyaltyBalancePanel      = new LoyaltyBalancePanel();

        mainPanel.add(new SearchPanel(),       "SEARCH");
        mainPanel.add(resultsPanel,            "RESULTS");
        mainPanel.add(summaryPanel,            "SUMMARY");
        mainPanel.add(confirmationPanel,       "CONFIRMATION");
        mainPanel.add(myReservationsPanel,     "MY_RESERVATIONS");
        mainPanel.add(updatePersonalDataPanel, "UPDATE_DATA");
        mainPanel.add(loyaltyBalancePanel,     "LOYALTY_BALANCE");

        setContentPane(mainPanel);
    }

    // Navigation
    private void showSearch() {
        cardLayout.show(mainPanel, "SEARCH");
    }

    private void showResults() {
        resultsPanel.populate(searchResults);
        cardLayout.show(mainPanel, "RESULTS");
    }

    private void showSummary() {
        summaryPanel.refresh();
        cardLayout.show(mainPanel, "SUMMARY");
    }

    private void showConfirmation(Reservation reservation) {
        confirmationPanel.update(reservation);
        cardLayout.show(mainPanel, "CONFIRMATION");
    }

    private void showMyReservations() {
        myReservationsPanel.loadReservations();
        cardLayout.show(mainPanel, "MY_RESERVATIONS");
    }

    private void showUpdatePersonalData() {
        updatePersonalDataPanel.populate();
        cardLayout.show(mainPanel, "UPDATE_DATA");
    }

    private void showLoyaltyBalance() {
        loyaltyBalancePanel.refresh();
        cardLayout.show(mainPanel, "LOYALTY_BALANCE");
    }

    private void onWindowClosing() {
        loginWindow.showRoleSelection();
        loginWindow.setVisible(true);
        dispose();
    }

    // Business Methods
    private void performSearch() {
        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "Please select both dates.", "Missing Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (startDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Start date cannot be in the past.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!endDate.isAfter(startDate)) {
            JOptionPane.showMessageDialog(this, "End date must be after Start date.", "Invalid Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            searchResults = RoomType.searchAvailableRoomTypes(startDate, endDate);
            if (searchResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No rooms available for the selected dates.", "No Availability", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showResults();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRoomTypeToReservation(RoomType roomType) {
        RoomType loaded = searchResults.stream()
                .filter(rt -> rt.getId() == roomType.getId())
                .findFirst().orElse(roomType);

        int bookedCount = 0;
        for (ReservationRoomType rrt : loaded.getReservationRoomTypes()) {
            Reservation res = rrt.getReservation();
            if (res.getReservationStatus() != org.example.booking.ReservationStatus.Cancelled
                    && startDate.isBefore(res.getEndDate())
                    && res.getStartDate().isBefore(endDate)) {
                bookedCount++;
            }
        }
        int available = loaded.getRooms().size() - bookedCount;

        long alreadySelected = selectedRooms.stream()
                .filter(e -> e.getKey().getId() == roomType.getId())
                .count();

        if (alreadySelected >= available) {
            JOptionPane.showMessageDialog(this,
                    "No more rooms available for: " + roomType.getTypeName() +
                    ". Only " + available + " room(s) available for these dates.",
                    "No Availability", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int guests = promptGuestCount(roomType);
        if (guests == -1) return;
        selectedRooms.add(new AbstractMap.SimpleEntry<>(roomType, guests));
        showSummary();
    }

    private int promptGuestCount(RoomType roomType) {
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, roomType.getMaxCapacity(), 1);
        JSpinner spinner = new JSpinner(model);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Guests for: " + roomType.getTypeName()), BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);
        int result = JOptionPane.showConfirmDialog(this, panel, "Number of Guests", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return -1;
        return (int) spinner.getValue();
    }

    private void removeRoomFromSelection(int index) {
        selectedRooms.remove(index);
        summaryPanel.refresh();
    }

    private void finalizeReservation() {
        if (selectedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one room type before finalizing.", "No Rooms Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            pendingReservation = Reservation.createReservation(
                    customer.getId(), startDate, endDate, selectedRooms);
            new PaymentDialog(this, pendingReservation).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating reservation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePaymentSuccess() {
        try {
            pendingReservation.confirmAndAwardPoints();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Reservation confirmed = session.get(Reservation.class, pendingReservation.getId());
                if (confirmed == null) throw new Exception("Reservation not found after confirmation.");
                Customer reloaded = session.get(Customer.class, customer.getId());
                if (reloaded == null) throw new Exception("Customer not found.");
                customer = reloaded;
                selectedRooms.clear();
                showConfirmation(confirmed);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error confirming reservation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLoyaltyPointsPayment(int pointsToDeduct) {
        try {
            pendingReservation.confirmWithLoyaltyPoints(customer.getId(), pointsToDeduct);
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Reservation confirmed = session.get(Reservation.class, pendingReservation.getId());
                if (confirmed == null) throw new Exception("Reservation not found after confirmation.");
                Customer reloaded = session.get(Customer.class, customer.getId());
                if (reloaded == null) throw new Exception("Customer not found.");
                customer = reloaded;
                selectedRooms.clear();
                showConfirmation(confirmed);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing loyalty payment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePaymentCancelled() {
        try {
            pendingReservation.cancelReservation();
            pendingReservation = null;
            showSummary();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cancelling: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Inner Panel: Search
    private class SearchPanel extends JPanel {
        private final DatePickerPanel checkInPicker  = new DatePickerPanel(LocalDate.now());
        private final DatePickerPanel checkOutPicker = new DatePickerPanel(LocalDate.now().plusDays(1));

        SearchPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(40, 60, 40, 60));

            JButton searchBtn = buildAccentButton("Search Available Rooms", ACCENT_RED);
            searchBtn.addActionListener(e -> onSearchClicked());

            JButton myReservationsBtn = buildSecondaryButton("My Reservations");
            myReservationsBtn.addActionListener(e -> onMyReservationsClicked());

            JButton profileBtn = buildSecondaryButton("My Profile");
            profileBtn.addActionListener(e -> onProfileClicked());

            JButton loyaltyBtn = buildSecondaryButton("Loyalty Balance");
            loyaltyBtn.addActionListener(e -> onLoyaltyBalanceClicked());

            JButton logoutBtn = buildSecondaryButton("Logout");
            logoutBtn.addActionListener(e -> onLogoutClicked());

            add(makeTitleLabel("Find Your Room"));
            add(Box.createVerticalStrut(5));
            add(makeSubtitleLabel("Welcome, " + customer.getFirstName()));
            add(Box.createVerticalStrut(20));
            add(makeSeparator());
            add(Box.createVerticalStrut(20));
            add(makeFieldLabel("Start Date:"));
            add(Box.createVerticalStrut(5));
            add(checkInPicker);
            add(Box.createVerticalStrut(12));
            add(makeFieldLabel("End Date:"));
            add(Box.createVerticalStrut(5));
            add(checkOutPicker);
            add(Box.createVerticalStrut(25));
            add(searchBtn);
            add(Box.createVerticalStrut(10));
            add(myReservationsBtn);
            add(Box.createVerticalStrut(10));
            add(profileBtn);
            add(Box.createVerticalStrut(10));
            add(loyaltyBtn);
            add(Box.createVerticalStrut(10));
            add(logoutBtn);
        }

        private void onSearchClicked() {
            startDate = checkInPicker.getDate();
            endDate = checkOutPicker.getDate();
            performSearch();
        }

        private void onMyReservationsClicked()   { showMyReservations(); }
        private void onProfileClicked()           { showUpdatePersonalData(); }
        private void onLoyaltyBalanceClicked()    { showLoyaltyBalance(); }
        private void onLogoutClicked()            { onWindowClosing(); }
    }
    // Inner Panel: Results
    private class ResultsPanel extends JPanel {
        private final DefaultListModel<RoomType> listModel = new DefaultListModel<>();
        private final JList<RoomType> roomList = new JList<>(listModel);

        ResultsPanel() {
            setLayout(new BorderLayout(10, 15));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(20, 30, 20, 30));

            roomList.setBackground(FIELD_BG);
            roomList.setForeground(TEXT_COLOR);
            roomList.setFont(new Font("Arial", Font.PLAIN, 14));
            roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            roomList.setBorder(new EmptyBorder(5, 8, 5, 8));

            JScrollPane scroll = new JScrollPane(roomList);
            scroll.getViewport().setBackground(FIELD_BG);
            scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));

            JButton addBtn = buildAccentButton("Add to Reservation", ACCENT_RED);
            addBtn.addActionListener(e -> onAddRoomClicked());

            JButton backBtn = buildSecondaryButton("Back to Search");
            backBtn.addActionListener(e -> onBackToSearchClicked());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            btnRow.setBackground(BG_COLOR);
            btnRow.add(backBtn);
            btnRow.add(addBtn);

            add(makeTitleLabel("Available Room Types"), BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(btnRow, BorderLayout.SOUTH);
        }

        void populate(List<RoomType> rooms) {
            listModel.clear();
            for (RoomType rt : rooms) listModel.addElement(rt);
        }

        private void onAddRoomClicked() {
            RoomType selected = roomList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(CustomerDashboard.this, "Please select a room type first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            addRoomTypeToReservation(selected);
        }

        private void onBackToSearchClicked() {
            selectedRooms.clear();
            showSearch();
        }
    }

    // Inner Panel: Summary
    private class SummaryPanel extends JPanel {
        private final JLabel dateRangeLabel = makeFieldLabel("");
        private final JPanel roomsContainer = new JPanel();
        private final JLabel totalLabel = new JLabel();
        private final JLabel pointsPreviewLabel = new JLabel();

        SummaryPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(20, 30, 20, 30));

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBackground(BG_COLOR);
            header.add(makeTitleLabel("Reservation Summary"));
            header.add(Box.createVerticalStrut(5));
            header.add(makeSubtitleLabel(customer.getFirstName() + " " + customer.getLastName() + "  |  " + customer.getEmail()));
            header.add(Box.createVerticalStrut(3));
            header.add(dateRangeLabel);
            header.add(Box.createVerticalStrut(10));

            roomsContainer.setLayout(new BoxLayout(roomsContainer, BoxLayout.Y_AXIS));
            roomsContainer.setBackground(FIELD_BG);
            JScrollPane scroll = new JScrollPane(roomsContainer);
            scroll.getViewport().setBackground(FIELD_BG);
            scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));

            totalLabel.setForeground(TEXT_COLOR);
            totalLabel.setFont(new Font("Arial", Font.BOLD, 15));
            totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            pointsPreviewLabel.setForeground(SUBTLE_TEXT);
            pointsPreviewLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            pointsPreviewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton addAnotherBtn = buildSecondaryButton("Add Another Room Type");
            addAnotherBtn.addActionListener(e -> onAddAnotherClicked());

            JButton backBtn = buildSecondaryButton("Back to Search");
            backBtn.addActionListener(e -> onBackToSearchClicked());

            JButton finalizeBtn = buildAccentButton("Finalize Reservation", ACCENT_RED);
            finalizeBtn.addActionListener(e -> onFinalizeClicked());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            btnRow.setBackground(BG_COLOR);
            btnRow.add(backBtn);
            btnRow.add(addAnotherBtn);
            btnRow.add(finalizeBtn);

            JPanel footer = new JPanel();
            footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
            footer.setBackground(BG_COLOR);
            footer.setBorder(new EmptyBorder(10, 0, 0, 0));
            footer.add(totalLabel);
            footer.add(Box.createVerticalStrut(3));
            footer.add(pointsPreviewLabel);
            footer.add(Box.createVerticalStrut(10));
            footer.add(btnRow);

            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);
        }

        void refresh() {
            dateRangeLabel.setText(startDate + "  →  " + endDate);

            roomsContainer.removeAll();
            for (int i = 0; i < selectedRooms.size(); i++) {
                roomsContainer.add(buildRoomRow(i));
                roomsContainer.add(Box.createVerticalStrut(2));
            }
            roomsContainer.revalidate();
            roomsContainer.repaint();

            totalLabel.setText(String.format("Total: $%.2f",
                    Reservation.calculateTotalPrice(startDate, endDate, selectedRooms)));
            pointsPreviewLabel.setText("Points to earn on confirmation: " +
                    Reservation.calculateLoyaltyPointsEarned(startDate, endDate, selectedRooms.size()));
        }

        private JPanel buildRoomRow(int index) {
            Map.Entry<RoomType, Integer> entry = selectedRooms.get(index);
            JPanel row = new JPanel(new BorderLayout(5, 0));
            row.setBackground(FIELD_BG);
            row.setBorder(new EmptyBorder(6, 8, 6, 8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            JLabel label = new JLabel(entry.getKey() + "  —  " + entry.getValue() + " guest(s)");
            label.setForeground(TEXT_COLOR);
            label.setFont(new Font("Arial", Font.PLAIN, 13));

            JButton removeBtn = new JButton("Remove");
            removeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
            removeBtn.setForeground(TEXT_COLOR);
            removeBtn.setBackground(ACCENT_RED);
            removeBtn.setBorderPainted(false);
            removeBtn.setFocusPainted(false);
            removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeBtn.addActionListener(e -> onRemoveClicked(index));

            row.add(label, BorderLayout.CENTER);
            row.add(removeBtn, BorderLayout.EAST);
            return row;
        }

        private void onAddAnotherClicked()          { showResults(); }
        private void onBackToSearchClicked()        { selectedRooms.clear(); showSearch(); }
        private void onFinalizeClicked()            { finalizeReservation(); }
        private void onRemoveClicked(int index)     { removeRoomFromSelection(index); }
    }

    // Inner Dialog: Payment
    private class PaymentDialog extends JDialog {
        private final JRadioButton creditCardRadio = new JRadioButton("Credit Card");
        private final JRadioButton loyaltyPointsRadio;
        private final JPanel creditCardPanel = new JPanel();
        private final JPanel loyaltyPanel = new JPanel();
        private final JTextField cardholderField = new JTextField();
        private final JTextField cardNumberField = new JTextField();
        private final JTextField cvvField = new JTextField();

        PaymentDialog(JFrame parent, Reservation reservation) {
            super(parent, "Payment", true);
            setSize(580, 600);
            setLocationRelativeTo(parent);
            setResizable(false);

            int pointsRequired = (int) Reservation.calculateTotalPrice(startDate, endDate, selectedRooms);
            loyaltyPointsRadio = new JRadioButton(
                    "Loyalty Points  (" + customer.getLoyaltyPoints() + " available, " + pointsRequired + " required)");

            ButtonGroup group = new ButtonGroup();
            group.add(creditCardRadio);
            group.add(loyaltyPointsRadio);
            creditCardRadio.setSelected(true);

            styleRadioButton(creditCardRadio);
            styleRadioButton(loyaltyPointsRadio);

            creditCardRadio.addActionListener(e -> onCreditCardSelected());
            loyaltyPointsRadio.addActionListener(e -> onLoyaltyPointsSelected());

            buildCreditCardFields();
            buildLoyaltyPointsInfo(pointsRequired);
            JButton submitBtn = buildAccentButton("Submit Payment", ACCENT_RED);
            submitBtn.addActionListener(e -> onSubmitPayment());

            JButton cancelBtn = buildSecondaryButton("Cancel Payment");
            cancelBtn.addActionListener(e -> onCancelPayment());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            btnRow.setBackground(BG_COLOR);
            btnRow.add(cancelBtn);
            btnRow.add(submitBtn);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(BG_COLOR);
            content.setBorder(new EmptyBorder(30, 40, 30, 40));

            content.add(makeTitleLabel("Payment"));
            content.add(Box.createVerticalStrut(5));
            content.add(makeFieldLabel(String.format("Amount Due: $%.2f",
                    Reservation.calculateTotalPrice(startDate, endDate, selectedRooms))));
            content.add(Box.createVerticalStrut(15));
            content.add(makeFieldLabel("Payment Method:"));
            content.add(Box.createVerticalStrut(5));
            content.add(creditCardRadio);
            content.add(Box.createVerticalStrut(3));
            content.add(loyaltyPointsRadio);
            content.add(Box.createVerticalStrut(15));
            content.add(creditCardPanel);
            content.add(loyaltyPanel);
            content.add(Box.createVerticalGlue());
            content.add(btnRow);

            setContentPane(content);
        }

        private void buildCreditCardFields() {
            creditCardPanel.setLayout(new BoxLayout(creditCardPanel, BoxLayout.Y_AXIS));
            creditCardPanel.setBackground(BG_COLOR);
            loginWindow.styleTextField(cardholderField);
            loginWindow.styleTextField(cardNumberField);
            loginWindow.styleTextField(cvvField);
            creditCardPanel.add(makeFieldLabel("Cardholder Name:"));
            creditCardPanel.add(Box.createVerticalStrut(4));
            creditCardPanel.add(cardholderField);
            creditCardPanel.add(Box.createVerticalStrut(8));
            creditCardPanel.add(makeFieldLabel("Card Number:"));
            creditCardPanel.add(Box.createVerticalStrut(4));
            creditCardPanel.add(cardNumberField);
            creditCardPanel.add(Box.createVerticalStrut(8));
            creditCardPanel.add(makeFieldLabel("CVV:"));
            creditCardPanel.add(Box.createVerticalStrut(4));
            creditCardPanel.add(cvvField);
        }

        private void buildLoyaltyPointsInfo(int pointsRequired) {
            loyaltyPanel.setLayout(new BoxLayout(loyaltyPanel, BoxLayout.Y_AXIS));
            loyaltyPanel.setBackground(BG_COLOR);
            loyaltyPanel.setVisible(false);
            loyaltyPanel.add(makeFieldLabel(
                    "Balance: " + customer.getLoyaltyPoints() + " pts  |  Required: " + pointsRequired + " pts"));
        }

        private void onCreditCardSelected() {
            creditCardPanel.setVisible(true);
            loyaltyPanel.setVisible(false);
            pack();
        }

        private void onLoyaltyPointsSelected() {
            creditCardPanel.setVisible(false);
            loyaltyPanel.setVisible(true);
            pack();
        }

        private void onSubmitPayment() {
            if (creditCardRadio.isSelected()) {
                processCreditCardPayment();
            } else {
                processLoyaltyPointsPayment();
            }
        }

        private void processCreditCardPayment() {
            String name   = cardholderField.getText().trim();
            String number = cardNumberField.getText().trim();
            String cvv    = cvvField.getText().trim();

            if (name.isEmpty() || number.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all card details.", "Missing Details", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(this, "Cardholder name must contain letters only.", "Invalid Name", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!number.matches("\\d{12}")) {
                JOptionPane.showMessageDialog(this, "Card number must be exactly 12 digits.", "Invalid Card Number", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!cvv.matches("\\d{3}")) {
                JOptionPane.showMessageDialog(this, "CVV must be exactly 3 digits.", "Invalid CVV", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cvv.equals("000")) {
                JOptionPane.showMessageDialog(this, "Payment failed! Please check your details and try again.", "Payment Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            handlePaymentSuccess();
        }

        private void processLoyaltyPointsPayment() {
            int pointsRequired = (int) Reservation.calculateTotalPrice(startDate, endDate, selectedRooms);
            if (customer.getLoyaltyPoints() < pointsRequired) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient loyalty points. Need " + pointsRequired + ", have " + customer.getLoyaltyPoints() + ".",
                        "Insufficient Balance", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dispose();
            handleLoyaltyPointsPayment(pointsRequired);
        }

        private void onCancelPayment() {
            dispose();
            handlePaymentCancelled();
        }

        private void styleRadioButton(JRadioButton radio) {
            radio.setBackground(BG_COLOR);
            radio.setForeground(TEXT_COLOR);
            radio.setFont(new Font("Arial", Font.PLAIN, 13));
            radio.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    // Inner Panel: Confirmation
    private class ConfirmationPanel extends JPanel {
        private final JLabel refLabel     = makeFieldLabel("");
        private final JLabel datesLabel   = makeFieldLabel("");
        private final JLabel statusLabel  = makeFieldLabel("");
        private final JLabel pointsLabel  = makeFieldLabel("");
        private final JLabel emailLabel   = makeSubtitleLabel("");

        ConfirmationPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(60, 60, 60, 60));

            JLabel titleLabel = makeTitleLabel("Booking Confirmed!");
            titleLabel.setForeground(new Color(100, 220, 100));

            JButton doneBtn = buildAccentButton("Done", ACCENT_RED);
            doneBtn.addActionListener(e -> onDoneClicked());

            add(titleLabel);
            add(Box.createVerticalStrut(20));
            add(refLabel);
            add(Box.createVerticalStrut(8));
            add(datesLabel);
            add(Box.createVerticalStrut(8));
            add(statusLabel);
            add(Box.createVerticalStrut(8));
            add(pointsLabel);
            add(Box.createVerticalStrut(15));
            add(emailLabel);
            add(Box.createVerticalStrut(30));
            add(doneBtn);
        }

        void update(Reservation reservation) {
            refLabel.setText("Reservation ID: " + reservation.getReference());
            datesLabel.setText(reservation.getStartDate() + "  →  " + reservation.getEndDate());
            statusLabel.setText("Status: " + reservation.getReservationStatus());
            pointsLabel.setText("Loyalty Points Balance: " + customer.getLoyaltyPoints());
        }

        private void onDoneClicked() {
            selectedRooms.clear();
            showSearch();
        }
    }

    // UI Helpers
    private JLabel makeTitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.BOLD, 26));
        l.setForeground(TEXT_COLOR);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel makeSubtitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        l.setForeground(SUBTLE_TEXT);
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

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 50, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JButton buildAccentButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(color);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // Inner Panel: My Reservations
    private class MyReservationsPanel extends JPanel {
        private final DefaultListModel<Reservation>         reservationModel = new DefaultListModel<>();
        private final DefaultListModel<ReservationRoomType> roomTypeModel    = new DefaultListModel<>();

        private final JList<Reservation>         reservationList = new JList<>(reservationModel);
        private final JList<ReservationRoomType> roomTypeList    = new JList<>(roomTypeModel);

        MyReservationsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(20, 30, 20, 30));

            styleList(reservationList);
            styleList(roomTypeList);

            reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            reservationList.addListSelectionListener(e -> onReservationSelected(e.getValueIsAdjusting()));

            JScrollPane leftScroll = new JScrollPane(reservationList);
            leftScroll.getViewport().setBackground(FIELD_BG);
            leftScroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
            leftScroll.setColumnHeaderView(makeFieldLabel("My Reservations"));

            JScrollPane rightScroll = new JScrollPane(roomTypeList);
            rightScroll.getViewport().setBackground(FIELD_BG);
            rightScroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
            rightScroll.setColumnHeaderView(makeFieldLabel("Booked Room Types"));

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
            splitPane.setDividerLocation(340);
            splitPane.setBackground(BG_COLOR);
            splitPane.setBorder(null);

            JButton manageBtn = buildAccentButton("Manage Reservation", ACCENT_BLUE);
            manageBtn.addActionListener(e -> onManageClicked());

            JButton backBtn = buildSecondaryButton("Back to Search");
            backBtn.addActionListener(e -> onBackClicked());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnRow.setBackground(BG_COLOR);
            btnRow.add(backBtn);
            btnRow.add(manageBtn);

            add(makeTitleLabel("My Reservations"), BorderLayout.NORTH);
            add(splitPane, BorderLayout.CENTER);
            add(btnRow, BorderLayout.SOUTH);
        }

        void loadReservations() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Customer reloaded = session.get(Customer.class, customer.getId());
                if (reloaded == null) throw new Exception("Customer not found.");
                customer = reloaded;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Error loading reservations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            reservationModel.clear();
            roomTypeModel.clear();
            for (Reservation r : customer.getReservations()) {
                reservationModel.addElement(r);
            }
        }

        private void onReservationSelected(boolean isAdjusting) {
            if (isAdjusting) return;
            roomTypeModel.clear();
            Reservation selected = reservationList.getSelectedValue();
            if (selected == null) return;
            for (ReservationRoomType rrt : selected.getReservationRoomTypes()) {
                roomTypeModel.addElement(rrt);
            }
        }

        private void onManageClicked() {
            Reservation selected = reservationList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Please select a reservation first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new ManageReservationDialog(CustomerDashboard.this, selected, this::loadReservations)
                    .setVisible(true);
        }

        private void onBackClicked() {
            showSearch();
        }

        private <T> void styleList(JList<T> list) {
            list.setBackground(FIELD_BG);
            list.setForeground(TEXT_COLOR);
            list.setFont(new Font("Arial", Font.PLAIN, 13));
            list.setBorder(new EmptyBorder(5, 8, 5, 8));
        }
    }

    // Inner Panel: Update Personal Data
    private class UpdatePersonalDataPanel extends JPanel {
        private final JTextField firstNameField  = new JTextField();
        private final JTextField middleNameField = new JTextField();
        private final JTextField lastNameField   = new JTextField();
        private final JTextField emailField      = new JTextField();
        private final JPasswordField newPassField     = new JPasswordField();
        private final JPasswordField confirmPassField = new JPasswordField();

        UpdatePersonalDataPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(30, 60, 30, 60));

            loginWindow.styleTextField(firstNameField);
            loginWindow.styleTextField(middleNameField);
            loginWindow.styleTextField(lastNameField);
            loginWindow.styleTextField(emailField);
            loginWindow.styleTextField(newPassField);
            loginWindow.styleTextField(confirmPassField);

            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(BG_COLOR);
            form.add(makeTitleLabel("My Profile"));
            form.add(Box.createVerticalStrut(20));
            form.add(makeFieldLabel("First Name:"));         form.add(Box.createVerticalStrut(4)); form.add(firstNameField);
            form.add(Box.createVerticalStrut(10));
            form.add(makeFieldLabel("Middle Name:"));        form.add(Box.createVerticalStrut(4)); form.add(middleNameField);
            form.add(Box.createVerticalStrut(10));
            form.add(makeFieldLabel("Last Name:"));          form.add(Box.createVerticalStrut(4)); form.add(lastNameField);
            form.add(Box.createVerticalStrut(10));
            form.add(makeFieldLabel("Email:"));              form.add(Box.createVerticalStrut(4)); form.add(emailField);
            form.add(Box.createVerticalStrut(10));
            form.add(makeFieldLabel("New Password (leave blank to keep current):"));
            form.add(Box.createVerticalStrut(4)); form.add(newPassField);
            form.add(Box.createVerticalStrut(10));
            form.add(makeFieldLabel("Confirm New Password:")); form.add(Box.createVerticalStrut(4)); form.add(confirmPassField);

            JButton saveBtn = buildAccentButton("Save Changes", ACCENT_RED);
            saveBtn.addActionListener(e -> onSaveClicked());

            JButton backBtn = buildSecondaryButton("Back to Search");
            backBtn.addActionListener(e -> onBackClicked());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            btnRow.setBackground(BG_COLOR);
            btnRow.add(backBtn);
            btnRow.add(saveBtn);

            add(form, BorderLayout.CENTER);
            add(btnRow, BorderLayout.SOUTH);
        }

        void populate() {
            firstNameField.setText(customer.getFirstName());
            middleNameField.setText(customer.getMiddleName() != null ? customer.getMiddleName() : "");
            lastNameField.setText(customer.getLastName());
            emailField.setText(customer.getEmail());
            newPassField.setText("");
            confirmPassField.setText("");
        }

        private void saveChanges() {
            String firstName   = firstNameField.getText().trim();
            String middleName  = middleNameField.getText().trim();
            String lastName    = lastNameField.getText().trim();
            String email       = emailField.getText().trim();
            String newPass     = new String(newPassField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "First name, last name and email are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.isEmpty() && !newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                customer.updatePersonalData(firstName, middleName.isEmpty() ? null : middleName,
                        lastName, email, newPass.isEmpty() ? null : newPass);
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                showSearch();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Error updating profile: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onSaveClicked() { saveChanges(); }
        private void onBackClicked() { showSearch(); }
    }

    // Inner Panel: Loyalty Balance
    private class LoyaltyBalancePanel extends JPanel {
        private final JLabel balanceLabel = new JLabel();
        private final DefaultListModel<String> historyModel = new DefaultListModel<>();
        private final JList<String> historyList = new JList<>(historyModel);

        LoyaltyBalancePanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(30, 60, 30, 60));

            balanceLabel.setFont(new Font("Georgia", Font.BOLD, 36));
            balanceLabel.setForeground(new Color(100, 220, 100));
            balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            historyList.setBackground(FIELD_BG);
            historyList.setForeground(TEXT_COLOR);
            historyList.setFont(new Font("Arial", Font.PLAIN, 13));
            historyList.setBorder(new EmptyBorder(5, 8, 5, 8));

            JScrollPane scroll = new JScrollPane(historyList);
            scroll.getViewport().setBackground(FIELD_BG);
            scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));

            JButton backBtn = buildSecondaryButton("Back to Search");
            backBtn.addActionListener(e -> onBackClicked());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnRow.setBackground(BG_COLOR);
            btnRow.add(backBtn);

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBackground(BG_COLOR);
            header.add(makeTitleLabel("Loyalty Balance"));
            header.add(Box.createVerticalStrut(10));
            header.add(balanceLabel);
            header.add(Box.createVerticalStrut(5));
            header.add(makeFieldLabel("Points earned per reservation (10 pts × rooms × nights)"));
            header.add(Box.createVerticalStrut(15));

            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(btnRow, BorderLayout.SOUTH);
        }

        void refresh() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Customer reloaded = session.get(Customer.class, customer.getId());
                if (reloaded == null) throw new Exception("Customer not found.");
                customer = reloaded;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Error loading loyalty balance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            balanceLabel.setText(customer.getLoyaltyPoints() + " pts");
            historyModel.clear();
            for (Reservation r : customer.getReservations()) {
                if (r.getReservationStatus() == org.example.booking.ReservationStatus.Confirmed
                        || r.getReservationStatus() == org.example.booking.ReservationStatus.CheckedIn) {
                    int pts = r.getLoyaltyPointsEarned();
                    historyModel.addElement(r.getReference() + "  |  " + r.getStartDate()
                            + " → " + r.getEndDate() + "  |  +" + pts + " pts");
                }
            }
            if (historyModel.isEmpty()) {
                historyModel.addElement("No confirmed reservations yet.");
            }
        }

        private void onBackClicked() { showSearch(); }
    }

    // Inner Panel: Date Picker
    private class DatePickerPanel extends JPanel {
        private final JSpinner spinner;

        DatePickerPanel(LocalDate initial) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG_COLOR);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setAlignmentX(Component.CENTER_ALIGNMENT);

            Date minDate    = toDate(LocalDate.now());
            Date initialVal = toDate(initial.isBefore(LocalDate.now()) ? LocalDate.now() : initial);

            SpinnerDateModel model = new SpinnerDateModel(initialVal, minDate, null, Calendar.DAY_OF_MONTH);
            spinner = new JSpinner(model);
            spinner.setEditor(new JSpinner.DateEditor(spinner, "dd / MM / yyyy"));
            spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

            styleSpinner();
            add(spinner);
        }

        private void styleSpinner() {
            spinner.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
            JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            tf.setBackground(FIELD_BG);
            tf.setForeground(TEXT_COLOR);
            tf.setCaretColor(TEXT_COLOR);
            tf.setFont(new Font("Arial", Font.PLAIN, 14));
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setSelectionColor(ACCENT_RED);
            tf.setSelectedTextColor(TEXT_COLOR);

            tf.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    spinner.setBorder(BorderFactory.createLineBorder(ACCENT_RED, 2));
                }
                @Override public void focusLost(FocusEvent e) {
                    spinner.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
                }
            });
        }

        private Date toDate(LocalDate ld) {
            return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        LocalDate getDate() {
            return ((Date) spinner.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }
}
