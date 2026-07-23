package org.example.gui;

import org.example.HibernateUtil;
import org.example.booking.Customer;
import org.example.hr.*;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;

import static org.example.gui.LoginWindow.*;

public class AdminDashboard extends JFrame {

    private final LoginWindow loginWindow;
    private final Admin admin;

    private final DefaultListModel<Customer>  customerModel  = new DefaultListModel<>();
    private final DefaultListModel<Employee>  employeeModel  = new DefaultListModel<>();
    private final JList<Customer>             customerList   = new JList<>(customerModel);
    private final JList<Employee>             employeeList   = new JList<>(employeeModel);

    public AdminDashboard(LoginWindow loginWindow, Admin admin) {
        this.loginWindow = loginWindow;
        this.admin = admin;

        setTitle("InnPoint - Admin Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onLogoutClicked(); }
        });
        setSize(1100, 700);
        setMinimumSize(new Dimension(750, 480));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_COLOR);
        tabs.setForeground(TEXT_COLOR);
        tabs.addTab("Customers",  buildCustomersTab());
        tabs.addTab("Employees",  buildEmployeesTab());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);
        wrapper.add(buildHeader(), BorderLayout.NORTH);
        wrapper.add(tabs,          BorderLayout.CENTER);
        wrapper.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(wrapper);

        loadCustomers();
        loadEmployees();
    }

    // Business Methods
    private void loadCustomers() {
        customerModel.clear();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("from Customer", Customer.class).list()
                    .forEach(customerModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployees() {
        employeeModel.clear();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("from Employee", Employee.class).list()
                    .forEach(employeeModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCustomer() {
        Customer selected = customerList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete customer: " + selected + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Admin.deleteCustomer(selected.getId());
            loadCustomers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedEmployee() {
        Employee selected = employeeList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete employee: " + selected + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Admin.deleteEmployee(selected.getId());
            loadEmployees();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Listeners
    private void onCreateCustomerClicked()  { new CreateCustomerDialog(this).setVisible(true); }
    private void onDeleteCustomerClicked()  { deleteSelectedCustomer(); }
    private void onCreateEmployeeClicked()  { new CreateEmployeeDialog(this).setVisible(true); }
    private void onDeleteEmployeeClicked()  { deleteSelectedEmployee(); }
    private void onLogoutClicked() {
        loginWindow.showRoleSelection();
        loginWindow.setVisible(true);
        dispose();
    }

    // UI Construction
    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(15, 30, 10, 30));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Welcome, " + admin.getFirstName() + " " + admin.getLastName());
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(SUBTLE_TEXT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        header.add(Box.createVerticalStrut(10));
        return header;
    }

    private JPanel buildCustomersTab() {
        styleList(customerList);
        JScrollPane scroll = new JScrollPane(customerList);
        scroll.getViewport().setBackground(FIELD_BG);
        scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));

        JButton createBtn = buildAccentButton("Create Customer", ACCENT_RED);
        createBtn.addActionListener(e -> onCreateCustomerClicked());

        JButton deleteBtn = buildSecondaryButton("Delete Customer");
        deleteBtn.addActionListener(e -> onDeleteCustomerClicked());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(BG_COLOR);
        btnRow.add(deleteBtn);
        btnRow.add(createBtn);

        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(BG_COLOR);
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.add(scroll,  BorderLayout.CENTER);
        tab.add(btnRow,  BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildEmployeesTab() {
        styleList(employeeList);
        JScrollPane scroll = new JScrollPane(employeeList);
        scroll.getViewport().setBackground(FIELD_BG);
        scroll.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));

        JButton createBtn = buildAccentButton("Create Employee", ACCENT_RED);
        createBtn.addActionListener(e -> onCreateEmployeeClicked());

        JButton deleteBtn = buildSecondaryButton("Delete Employee");
        deleteBtn.addActionListener(e -> onDeleteEmployeeClicked());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(BG_COLOR);
        btnRow.add(deleteBtn);
        btnRow.add(createBtn);

        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(BG_COLOR);
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.add(scroll,  BorderLayout.CENTER);
        tab.add(btnRow,  BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildFooter() {
        JButton logoutBtn = buildSecondaryButton("Logout");
        logoutBtn.addActionListener(e -> onLogoutClicked());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG_COLOR);
        footer.add(logoutBtn);
        return footer;
    }

    // Inner Dialog: Create Customer
    private class CreateCustomerDialog extends JDialog {
        private final JTextField firstNameField  = new JTextField();
        private final JTextField middleNameField = new JTextField();
        private final JTextField lastNameField   = new JTextField();
        private final JTextField emailField      = new JTextField();
        private final JPasswordField passField   = new JPasswordField();
        private final DatePickerField birthPicker = new DatePickerField();

        CreateCustomerDialog(JFrame parent) {
            super(parent, "Create Customer", true);
            setSize(520, 560);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(BG_COLOR);
            content.setBorder(new EmptyBorder(25, 35, 25, 35));

            styleField(firstNameField); styleField(middleNameField);
            styleField(lastNameField);  styleField(emailField);
            styleField(passField);

            content.add(makeTitle("New Customer"));
            content.add(Box.createVerticalStrut(15));
            addRow(content, "First Name:", firstNameField);
            addRow(content, "Middle Name (optional):", middleNameField);
            addRow(content, "Last Name:", lastNameField);
            addRow(content, "Birth Date:", birthPicker);
            addRow(content, "Email:", emailField);
            addRow(content, "Password:", passField);
            content.add(Box.createVerticalStrut(15));
            content.add(buildButtons());
            setContentPane(content);
        }

        private JPanel buildButtons() {
            JButton saveBtn = buildAccentButton("Create", ACCENT_RED);
            saveBtn.addActionListener(e -> onSaveClicked());
            JButton cancelBtn = buildSecondaryButton("Cancel");
            cancelBtn.addActionListener(e -> dispose());
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            row.setBackground(BG_COLOR);
            row.add(cancelBtn); row.add(saveBtn);
            return row;
        }

        private void saveCustomer() {
            String first  = firstNameField.getText().trim();
            String middle = middleNameField.getText().trim();
            String last   = lastNameField.getText().trim();
            String email  = emailField.getText().trim();
            String pass   = new String(passField.getPassword()).trim();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields except Middle Name are required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Admin.createCustomer(first, middle.isEmpty() ? null : middle,
                        last, birthPicker.getDate(), email, pass);
                loadCustomers();
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onSaveClicked() { saveCustomer(); }
    }

    // Inner Dialog: Create Employee
    private class CreateEmployeeDialog extends JDialog {
        // Common fields
        private final JComboBox<String> typeCombo = new JComboBox<>(
                new String[]{"Receptionist", "Housekeeper", "Technician"});
        private final JTextField firstNameField  = new JTextField();
        private final JTextField middleNameField = new JTextField();
        private final JTextField lastNameField   = new JTextField();
        private final DatePickerField birthPicker = new DatePickerField();
        private final JTextField employeeIdField = new JTextField();
        private final JPasswordField passField   = new JPasswordField();
        private final JTextField phoneField      = new JTextField();
        private final JComboBox<EmploymentType> empTypeCombo =
                new JComboBox<>(EmploymentType.values());
        private final DatePickerField hirePicker  = new DatePickerField();
        private final JTextField salaryField      = new JTextField();
        private final JTextField hourlyRateField  = new JTextField();
        private final JTextField hoursPerWeekField= new JTextField();
        private final JPanel contractPanel        = new JPanel();

        // Receptionist-specific
        private final JTextField deskNumberField  = new JTextField();
        private final JTextField languagesField   = new JTextField();
        // Housekeeper-specific
        private final JTextField sectionField     = new JTextField();
        private final JTextField cartNumberField  = new JTextField();
        // Technician-specific
        private final JTextField specializationField  = new JTextField();
        private final JTextField certificatesField    = new JTextField();

        private final JPanel typeSpecificPanel = new JPanel(new CardLayout());

        CreateEmployeeDialog(JFrame parent) {
            super(parent, "Create Employee", true);
            setSize(560, 750);
            setLocationRelativeTo(parent);
            setResizable(false);

            for (JTextField f : new JTextField[]{firstNameField, middleNameField, lastNameField,
                    employeeIdField, phoneField, salaryField, hourlyRateField,
                    hoursPerWeekField, deskNumberField, languagesField, sectionField,
                    cartNumberField, specializationField, certificatesField}) {
                styleField(f);
            }
            styleField(passField);

            buildContractPanel();
            buildTypeSpecificPanel();

            typeCombo.setBackground(FIELD_BG);
            typeCombo.setForeground(TEXT_COLOR);
            typeCombo.setFont(new Font("Arial", Font.PLAIN, 13));
            typeCombo.addActionListener(e -> onTypeChanged());

            empTypeCombo.setBackground(FIELD_BG);
            empTypeCombo.setForeground(TEXT_COLOR);
            empTypeCombo.setFont(new Font("Arial", Font.PLAIN, 13));
            empTypeCombo.addActionListener(e -> onEmploymentTypeChanged());

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(BG_COLOR);
            content.setBorder(new EmptyBorder(20, 35, 20, 35));

            content.add(makeTitle("New Employee"));
            content.add(Box.createVerticalStrut(10));
            addRow(content, "Role:", typeCombo);
            addRow(content, "First Name:", firstNameField);
            addRow(content, "Middle Name (optional):", middleNameField);
            addRow(content, "Last Name:", lastNameField);
            addRow(content, "Birth Date:", birthPicker);
            addRow(content, "Employee ID:", employeeIdField);
            addRow(content, "Password:", passField);
            addRow(content, "Phone:", phoneField);
            addRow(content, "Employment Type:", empTypeCombo);
            addRow(content, "Hire Date:", hirePicker);
            content.add(contractPanel);
            content.add(typeSpecificPanel);
            content.add(Box.createVerticalStrut(10));
            content.add(buildButtons());

            JScrollPane scroll = new JScrollPane(content);
            scroll.getViewport().setBackground(BG_COLOR);
            scroll.setBorder(null);
            setContentPane(scroll);

            onEmploymentTypeChanged();
            onTypeChanged();
        }

        private void buildContractPanel() {
            contractPanel.setLayout(new BoxLayout(contractPanel, BoxLayout.Y_AXIS));
            contractPanel.setBackground(BG_COLOR);
            addRow(contractPanel, "Salary (FullTime):", salaryField);
            addRow(contractPanel, "Hourly Rate (PartTime):", hourlyRateField);
            addRow(contractPanel, "Hours/Week (PartTime):", hoursPerWeekField);
        }

        private void buildTypeSpecificPanel() {
            typeSpecificPanel.setBackground(BG_COLOR);

            JPanel receptionistPanel = new JPanel();
            receptionistPanel.setLayout(new BoxLayout(receptionistPanel, BoxLayout.Y_AXIS));
            receptionistPanel.setBackground(BG_COLOR);
            addRow(receptionistPanel, "Desk Number:", deskNumberField);
            addRow(receptionistPanel, "Languages (comma-separated):", languagesField);

            JPanel housekeeperPanel = new JPanel();
            housekeeperPanel.setLayout(new BoxLayout(housekeeperPanel, BoxLayout.Y_AXIS));
            housekeeperPanel.setBackground(BG_COLOR);
            addRow(housekeeperPanel, "Assigned Section:", sectionField);
            addRow(housekeeperPanel, "Cart Number:", cartNumberField);

            JPanel technicianPanel = new JPanel();
            technicianPanel.setLayout(new BoxLayout(technicianPanel, BoxLayout.Y_AXIS));
            technicianPanel.setBackground(BG_COLOR);
            addRow(technicianPanel, "Specialization:", specializationField);
            addRow(technicianPanel, "Certificates (comma-separated):", certificatesField);

            typeSpecificPanel.add(receptionistPanel, "Receptionist");
            typeSpecificPanel.add(housekeeperPanel,  "Housekeeper");
            typeSpecificPanel.add(technicianPanel,   "Technician");
        }

        private void onTypeChanged() {
            String selected = (String) typeCombo.getSelectedItem();
            ((CardLayout) typeSpecificPanel.getLayout()).show(typeSpecificPanel, selected);
        }

        private void onEmploymentTypeChanged() {
            EmploymentType selected = (EmploymentType) empTypeCombo.getSelectedItem();
            boolean isFullTime = selected == EmploymentType.FullTime;
            salaryField.setVisible(isFullTime);
            hourlyRateField.setVisible(!isFullTime);
            hoursPerWeekField.setVisible(!isFullTime);
            contractPanel.revalidate();
        }

        private JPanel buildButtons() {
            JButton saveBtn = buildAccentButton("Create", ACCENT_RED);
            saveBtn.addActionListener(e -> onSaveClicked());
            JButton cancelBtn = buildSecondaryButton("Cancel");
            cancelBtn.addActionListener(e -> dispose());
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            row.setBackground(BG_COLOR);
            row.add(cancelBtn); row.add(saveBtn);
            return row;
        }

        private void saveEmployee() {
            String type   = (String) typeCombo.getSelectedItem();
            String first  = firstNameField.getText().trim();
            String middle = middleNameField.getText().trim();
            String last   = lastNameField.getText().trim();
            String empId  = employeeIdField.getText().trim();
            String pass   = new String(passField.getPassword()).trim();
            String phone  = phoneField.getText().trim();
            EmploymentType empType = (EmploymentType) empTypeCombo.getSelectedItem();
            LocalDate birth = birthPicker.getDate();
            LocalDate hire  = hirePicker.getDate();

            if (first.isEmpty() || last.isEmpty() || empId.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All common fields are required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Double salary = null, hourlyRate = null;
                Integer hoursPerWeek = null;
                if (empType == EmploymentType.FullTime) {
                    salary = Double.parseDouble(salaryField.getText().trim());
                } else {
                    hourlyRate   = Double.parseDouble(hourlyRateField.getText().trim());
                    hoursPerWeek = Integer.parseInt(hoursPerWeekField.getText().trim());
                }
                String mid = middle.isEmpty() ? null : middle;

                switch (type) {
                    case "Receptionist" -> {
                        int desk = Integer.parseInt(deskNumberField.getText().trim());
                        Set<String> langs = new HashSet<>(Arrays.asList(
                                languagesField.getText().split(",")));
                        Admin.createReceptionist(first, mid, last, birth, empId, pass,
                                empType, hire, phone, salary, hourlyRate, hoursPerWeek, desk, langs);
                    }
                    case "Housekeeper" -> {
                        String section = sectionField.getText().trim();
                        int cart = Integer.parseInt(cartNumberField.getText().trim());
                        Admin.createHousekeeper(first, mid, last, birth, empId, pass,
                                empType, hire, phone, salary, hourlyRate, hoursPerWeek, section, cart);
                    }
                    case "Technician" -> {
                        String spec  = specializationField.getText().trim();
                        Set<String> certs = new HashSet<>(Arrays.asList(
                                certificatesField.getText().split(",")));
                        Admin.createTechnician(first, mid, last, birth, empId, pass,
                                empType, hire, phone, salary, hourlyRate, hoursPerWeek, spec, certs);
                    }
                }
                loadEmployees();
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for numeric fields.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onSaveClicked() { saveEmployee(); }
    }

    // Shared UI Helpers
    private static class DatePickerField extends JPanel {
        private final JSpinner spinner;
        DatePickerField() {
            setLayout(new BorderLayout());
            setBackground(BG_COLOR);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            Date init = Date.from(
                    LocalDate.now().minusYears(25).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            spinner = new JSpinner(new SpinnerDateModel(init, null, null, Calendar.DAY_OF_MONTH));
            spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
            JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            tf.setBackground(FIELD_BG);
            tf.setForeground(TEXT_COLOR);
            tf.setFont(new Font("Arial", Font.PLAIN, 13));
            spinner.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE));
            add(spinner, BorderLayout.CENTER);
        }
        LocalDate getDate() {
            return ((Date) spinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
    }

    private void addRow(JPanel panel, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(SUBTLE_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(field);
        panel.add(Box.createVerticalStrut(8));
    }

    private JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.BOLD, 20));
        l.setForeground(TEXT_COLOR);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private <T> void styleList(JList<T> list) {
        list.setBackground(FIELD_BG);
        list.setForeground(TEXT_COLOR);
        list.setFont(new Font("Arial", Font.PLAIN, 13));
        list.setBorder(new EmptyBorder(5, 8, 5, 8));
    }

    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
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
