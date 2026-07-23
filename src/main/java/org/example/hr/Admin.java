package org.example.hr;

import org.example.HibernateUtil;
import org.example.booking.Customer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
public class Admin extends Person {

    @Column(nullable = false, unique = true)
    private String adminId;

    @Column(nullable = false)
    private String passwordHash;

    public Admin() {}

    public Admin(String firstName, String middleName, String lastName, LocalDate birthDate,
                 String adminId, String plainPassword) {
        super(firstName, middleName, lastName, birthDate);
        this.adminId = adminId;
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean checkPassword(String plain) {
        return BCrypt.checkpw(plain, passwordHash);
    }

    // =========================================================================
    // Use Case: Manage Customers
    // =========================================================================
    public static void createCustomer(String firstName, String middleName, String lastName,
                                      LocalDate birthDate, String email, String password) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Customer c = new Customer(firstName, middleName, lastName, birthDate, email, 0, password);
            session.save(c);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void deleteCustomer(long id) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Customer c = session.get(Customer.class, id);
            if (c == null) throw new Exception("Customer not found.");
            session.delete(c);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // =========================================================================
    // Use Case: Manage Employees
    // =========================================================================
    public static void createReceptionist(String firstName, String middleName, String lastName,
                                          LocalDate birthDate, String employeeId, String password,
                                          EmploymentType type, LocalDate hireDate, String phone,
                                          Double salary, Double hourlyRate, Integer hoursPerWeek,
                                          int deskNumber, Set<String> languages) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(new Receptionist(firstName, middleName, lastName, birthDate,
                    employeeId, password, type, hireDate, phone,
                    salary, hourlyRate, hoursPerWeek, deskNumber, languages));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void createHousekeeper(String firstName, String middleName, String lastName,
                                         LocalDate birthDate, String employeeId, String password,
                                         EmploymentType type, LocalDate hireDate, String phone,
                                         Double salary, Double hourlyRate, Integer hoursPerWeek,
                                         String section, int cartNumber) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(new Housekeeper(firstName, middleName, lastName, birthDate,
                    employeeId, password, type, hireDate, phone,
                    salary, hourlyRate, hoursPerWeek, section, cartNumber));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void createTechnician(String firstName, String middleName, String lastName,
                                        LocalDate birthDate, String employeeId, String password,
                                        EmploymentType type, LocalDate hireDate, String phone,
                                        Double salary, Double hourlyRate, Integer hoursPerWeek,
                                        String specialization, Set<String> certificates) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(new Technician(firstName, middleName, lastName, birthDate,
                    employeeId, password, type, hireDate, phone,
                    salary, hourlyRate, hoursPerWeek, specialization, certificates));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static void deleteEmployee(long id) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, id);
            if (emp == null) throw new Exception("Employee not found.");
            session.delete(emp);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " (Admin: " + adminId + ")";
    }
}
