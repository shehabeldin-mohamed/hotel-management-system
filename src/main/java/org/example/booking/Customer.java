package org.example.booking;

import org.example.HibernateUtil;
import org.example.hr.Person;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends Person {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    // Running loyalty points balance — increased on confirmation, decreased on redemption
    @NotNull(message = "Loyalty points required")
    @PositiveOrZero(message = "Loyalty points cannot be negative")
    @Column(nullable = false)
    private int loyaltyPoints;

    @NotBlank(message = "Password hash is required")
    @Column(nullable = false)
    private String passwordHash;

    // @Fetch(SUBSELECT) prevents Hibernate Cartesian product caused by multiple EAGER collections
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<Reservation> reservations = new ArrayList<>();

    public Customer() {}

    public Customer(String firstName, String middleName, String lastName,
                    LocalDate birthDate, String email,
                    int loyaltyPoints, String plainPassword) {
        super(firstName, middleName, lastName, birthDate);
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setCustomer(this);
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {this.email = email;}

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) {this.loyaltyPoints = loyaltyPoints;}

    public List<Reservation> getReservations() { return new ArrayList<>(reservations); }
    public void setReservations(List<Reservation> reservations) {this.reservations = reservations;}

    public String getPasswordHash() {return passwordHash;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}

    // =========================================================================
    // Use Case: Update Personal Data
    // =========================================================================
    public void updatePersonalData(String firstName, String middleName, String lastName,
                                    String email, String newPassword) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Customer c = session.get(Customer.class, this.getId());
            if (c == null) throw new Exception("Customer not found.");
            c.setFirstName(firstName);
            c.setMiddleName(middleName);
            c.setLastName(lastName);
            c.setEmail(email);
            if (newPassword != null && !newPassword.isEmpty()) {
                c.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            }
            session.update(c);
            tx.commit();
            this.setFirstName(firstName);
            this.setMiddleName(middleName);
            this.setLastName(lastName);
            this.setEmail(email);
            if (newPassword != null && !newPassword.isEmpty()) {
                this.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // Called by Reservation.confirmAndAwardPoints() after successful payment
    public void addLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot add negative loyalty points.");
        }
        this.loyaltyPoints += points;
    }

    // Called by Reservation.confirmWithLoyaltyPoints() when customer pays with points
    public void deductLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot deduct negative loyalty points.");
        }
        if (this.loyaltyPoints < points) {
            throw new IllegalStateException("Insufficient loyalty points balance. Current balance: " + this.loyaltyPoints);
        }
        this.loyaltyPoints -= points;
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " (" + email + ")";
    }
}
