package org.example.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import org.example.HibernateUtil;
import org.example.booking.Customer;
import org.example.hr.Admin;
import org.example.hr.Housekeeper;
import org.example.hr.Receptionist;
import org.example.hr.Technician;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

public class LoginWindow extends JFrame {

    public static final Color BG_COLOR      = new Color(26, 26, 46);
    public static final Color ACCENT_RED    = new Color(233, 69, 96);
    public static final Color ACCENT_BLUE   = new Color(15, 52, 96);
    public static final Color ACCENT_PURPLE = new Color(83, 52, 131);
    public static final Color TEXT_COLOR    = Color.WHITE;
    public static final Color SUBTLE_TEXT   = new Color(170, 170, 170);
    public static final Color FIELD_BG      = new Color(22, 33, 62);

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final LoginFormPanel loginFormPanel;

    public LoginWindow() {
        setTitle("InnPoint - Hotel Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 580);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginFormPanel = new LoginFormPanel();

        mainPanel.add(new RoleSelectionPanel(), "ROLE_SELECTION");
        mainPanel.add(loginFormPanel, "LOGIN_FORM");

        setContentPane(mainPanel);
        setVisible(true);
    }

    public void showRoleSelection() {
        cardLayout.show(mainPanel, "ROLE_SELECTION");
    }

    private void showLoginFormForRole(String role) {
        loginFormPanel.prepareForRole(role);
        cardLayout.show(mainPanel, "LOGIN_FORM");
    }

    private class RoleSelectionPanel extends JPanel {
        RoleSelectionPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(40, 50, 40, 50));

            JLabel title = new JLabel("InnPoint");
            title.setFont(new Font("Georgia", Font.BOLD, 42));
            title.setForeground(TEXT_COLOR);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subtitle = new JLabel("Hotel Management System");
            subtitle.setFont(new Font("Georgia", Font.PLAIN, 16));
            subtitle.setForeground(SUBTLE_TEXT);
            subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(50, 50, 80));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

            JLabel prompt = new JLabel("Please select your role to continue");
            prompt.setFont(new Font("Arial", Font.PLAIN, 14));
            prompt.setForeground(SUBTLE_TEXT);
            prompt.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton customerBtn     = createRoleButton("Customer",     ACCENT_RED);
            JButton receptionistBtn = createRoleButton("Receptionist", ACCENT_BLUE);
            JButton housekeeperBtn  = createRoleButton("Housekeeper",  new Color(22, 33, 62));
            JButton technicianBtn   = createRoleButton("Technician",   ACCENT_PURPLE);
            JButton adminBtn        = createRoleButton("Admin",        new Color(60, 60, 60));

            customerBtn.addActionListener(e     -> showLoginFormForRole("Customer"));
            receptionistBtn.addActionListener(e -> showLoginFormForRole("Receptionist"));
            housekeeperBtn.addActionListener(e  -> showLoginFormForRole("Housekeeper"));
            technicianBtn.addActionListener(e   -> showLoginFormForRole("Technician"));
            adminBtn.addActionListener(e        -> showLoginFormForRole("Admin"));

            add(title);
            add(Box.createVerticalStrut(5));
            add(subtitle);
            add(Box.createVerticalStrut(20));
            add(separator);
            add(Box.createVerticalStrut(20));
            add(prompt);
            add(Box.createVerticalStrut(20));
            add(customerBtn);
            add(Box.createVerticalStrut(10));
            add(receptionistBtn);
            add(Box.createVerticalStrut(10));
            add(housekeeperBtn);
            add(Box.createVerticalStrut(10));
            add(technicianBtn);
            add(Box.createVerticalStrut(10));
            add(adminBtn);
        }
    }

    private class LoginFormPanel extends JPanel {
        private String currentRole;
        private final JLabel roleLabel;
        private final JLabel usernameLabel;
        private final JTextField usernameField;
        private final JPasswordField passwordField;
        private final JLabel errorLabel;

        LoginFormPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG_COLOR);
            setBorder(new EmptyBorder(40, 50, 40, 50));

            JLabel title = new JLabel("InnPoint");
            title.setFont(new Font("Georgia", Font.BOLD, 32));
            title.setForeground(TEXT_COLOR);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            roleLabel = new JLabel();
            roleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            roleLabel.setForeground(SUBTLE_TEXT);
            roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(50, 50, 80));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

            usernameLabel = new JLabel();
            usernameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            usernameLabel.setForeground(SUBTLE_TEXT);
            usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            usernameField = createStyledTextField();

            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            passwordLabel.setForeground(SUBTLE_TEXT);
            passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            passwordField = new JPasswordField();
            styleTextField(passwordField);

            errorLabel = new JLabel(" ");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttonPanel.setBackground(BG_COLOR);
            buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JButton backBtn = new JButton("Back");
            backBtn.setFont(new Font("Arial", Font.BOLD, 13));
            backBtn.setForeground(SUBTLE_TEXT);
            backBtn.setBackground(BG_COLOR);
            backBtn.setBorder(BorderFactory.createLineBorder(SUBTLE_TEXT, 1, true));
            backBtn.setFocusPainted(false);
            backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            backBtn.setPreferredSize(new Dimension(100, 35));

            JButton loginBtn = new JButton("Login");
            loginBtn.setFont(new Font("Arial", Font.BOLD, 13));
            loginBtn.setForeground(TEXT_COLOR);
            loginBtn.setBackground(ACCENT_RED);
            loginBtn.setBorderPainted(false);
            loginBtn.setFocusPainted(false);
            loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            loginBtn.setPreferredSize(new Dimension(100, 35));

            backBtn.addActionListener(e -> showRoleSelection());

            loginBtn.addActionListener(e -> executeLogin());
            passwordField.addActionListener(e -> loginBtn.doClick());

            buttonPanel.add(backBtn);
            buttonPanel.add(loginBtn);

            add(title);
            add(Box.createVerticalStrut(5));
            add(roleLabel);
            add(Box.createVerticalStrut(15));
            add(separator);
            add(Box.createVerticalStrut(15));
            add(usernameLabel);
            add(Box.createVerticalStrut(5));
            add(usernameField);
            add(Box.createVerticalStrut(12));
            add(passwordLabel);
            add(Box.createVerticalStrut(5));
            add(passwordField);
            add(Box.createVerticalStrut(8));
            add(errorLabel);
            add(Box.createVerticalStrut(15));
            add(buttonPanel);
        }

        public void prepareForRole(String role) {
            this.currentRole = role;
            roleLabel.setText("Login as " + role);
            usernameLabel.setText(role.equals("Customer") ? "Email:" : role.equals("Admin") ? "Admin ID:" : "Employee ID:");
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setText(" ");
        }

        private void executeLogin() {
            String identifier = usernameField.getText().trim();
            String password   = new String(passwordField.getPassword()).trim();

            if (identifier.isEmpty() || password.isEmpty()) {
                errorLabel.setForeground(Color.RED);
                errorLabel.setText("Please enter both fields.");
                return;
            }

            errorLabel.setForeground(SUBTLE_TEXT);
            errorLabel.setText("Logging in...");

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                switch (currentRole) {
                    case "Customer" -> {
                        Customer customer = session.createQuery("from Customer where email = :email", Customer.class)
                                .setParameter("email", identifier)
                                .uniqueResult();
                        if (customer == null || !BCrypt.checkpw(password, customer.getPasswordHash())) {
                            showError("Invalid email or password.");
                            return;
                        }
                        new CustomerDashboard(LoginWindow.this, customer).setVisible(true);
                        LoginWindow.this.setVisible(false);
                    }
                    case "Receptionist" -> {
                        Receptionist receptionist = session.createQuery("from Receptionist where employeeId = :id", Receptionist.class)
                                .setParameter("id", identifier)
                                .uniqueResult();
                        if (receptionist == null || !BCrypt.checkpw(password, receptionist.getPasswordHash())) {
                            showError("Invalid Employee ID or password.");
                            return;
                        }
                        new ReceptionistDashboard(LoginWindow.this, receptionist).setVisible(true);
                        LoginWindow.this.setVisible(false);
                    }
                    case "Housekeeper" -> {
                        Housekeeper housekeeper = session.createQuery("from Housekeeper where employeeId = :id", Housekeeper.class)
                                .setParameter("id", identifier)
                                .uniqueResult();
                        if (housekeeper == null || !BCrypt.checkpw(password, housekeeper.getPasswordHash())) {
                            showError("Invalid Employee ID or password.");
                            return;
                        }
                        new HousekeeperDashboard(LoginWindow.this, housekeeper).setVisible(true);
                        LoginWindow.this.setVisible(false);
                    }
                    case "Technician" -> {
                        Technician technician = session.createQuery("from Technician where employeeId = :id", Technician.class)
                                .setParameter("id", identifier)
                                .uniqueResult();
                        if (technician == null || !BCrypt.checkpw(password, technician.getPasswordHash())) {
                            showError("Invalid Employee ID or password.");
                            return;
                        }
                        new TechnicianDashboard(LoginWindow.this, technician).setVisible(true);
                        LoginWindow.this.setVisible(false);
                    }
                    case "Admin" -> {
                        Admin admin = session.createQuery("from Admin where adminId = :id", Admin.class)
                                .setParameter("id", identifier)
                                .uniqueResult();
                        if (admin == null || !admin.checkPassword(password)) {
                            showError("Invalid Admin ID or password.");
                            return;
                        }
                        new AdminDashboard(LoginWindow.this, admin).setVisible(true);
                        LoginWindow.this.setVisible(false);
                    }
                }
            } catch (Exception ex) {
                showError("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void showError(String message) {
            errorLabel.setForeground(Color.RED);
            errorLabel.setText(message);
        }
    }

    private JButton createRoleButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(color);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    public JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    public void styleTextField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

}
