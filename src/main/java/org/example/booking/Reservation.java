package org.example.booking;

import org.example.HibernateUtil;
import org.example.facility.RoomType;
import org.example.hr.Receptionist;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import org.example.facility.Room;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Reference cannot be blank")
    @Column(nullable = false, unique = true)
    private String reference;

    @NotNull(message = "Reservation date is required")
    @PastOrPresent(message = "Reservation date cannot be in the future")
    @Column(nullable = false)
    private LocalDate reservationDate;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Reservation status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    public Reservation() {}

    public Reservation(String reference, LocalDate reservationDate,
                       LocalDate startDate, LocalDate endDate,
                       ReservationStatus reservationStatus) throws Exception {
        this.reference = reference;
        this.reservationDate = reservationDate;
        setEndDate(endDate);
        setStartDate(startDate);
        this.reservationStatus = reservationStatus;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String referenceNumber) {this.reference = referenceNumber;}

    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) {this.reservationDate = reservationDate;}

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        if (startDate == null) throw new IllegalArgumentException("Start date is required.");

        if (this.endDate != null && startDate.isAfter(this.endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        this.startDate = startDate;
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) throws Exception {
        if (endDate == null) throw new IllegalArgumentException("End date is required.");

        if (this.startDate != null && endDate.isBefore(this.startDate)) {
            throw new Exception("End date cannot be before start date.");
        }
        this.endDate = endDate;
    }

    public ReservationStatus getReservationStatus() { return reservationStatus; }

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    // Null until a receptionist handles check-in (set via Receptionist.addReservation())
    @ManyToOne
    @JoinColumn(name = "receptionist_id", nullable = true)
    private Receptionist receptionist;

    public Receptionist getReceptionist() { return receptionist; }
    public void setReceptionist(Receptionist receptionist) { this.receptionist = receptionist; }

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ReservationRoomType> reservationRoomTypes = new ArrayList<>();

    public void addReservationRoomType(ReservationRoomType reservationRoomType) {
        this.reservationRoomTypes.add(reservationRoomType);
        reservationRoomType.setReservation(this);
    }

    public List<ReservationRoomType> getReservationRoomTypes() { return reservationRoomTypes; }
    public void setReservationRoomTypes(List<ReservationRoomType> reservationRoomTypes) {this.reservationRoomTypes = reservationRoomTypes;}

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private CheckIn checkIn;

    public CheckIn getCheckIn() { return checkIn; }

    public CheckIn createCheckIn(LocalDateTime checkInTime, String notes) {
        if (this.checkIn != null) {
            throw new IllegalStateException("Reservation already has a check-in.");
        }

        for (ReservationRoomType bookedType : reservationRoomTypes) {
            if (!bookedType.isRoomAssigned()) {
                throw new IllegalStateException(
                        "All booked room types must have a physical room assigned before check-in.");
            }
            if (!bookedType.getAssignedRoom().isReadyForCheckIn()) {
                throw new IllegalStateException(
                        "Room " + bookedType.getAssignedRoom().getNumber() + " is not ready for check-in " +
                        "(must be Clean and Ok for maintenance).");
            }
        }

        CheckIn newCheckIn = new CheckIn(this, checkInTime, notes);
        this.checkIn = newCheckIn;
        this.reservationStatus = ReservationStatus.CheckedIn;
        return newCheckIn;
    }

    @Entity
    @Table(name = "CheckIn")
    public static class CheckIn {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @NotNull(message = "Check-in time is required")
        @Column(nullable = false)
        private LocalDateTime checkInTime;

        @Column(nullable = true)
        private LocalDateTime actualCheckoutTime;

        @Column(nullable = true)
        private String notes;

        protected CheckIn() {}

        private CheckIn(Reservation reservation, LocalDateTime checkInTime,
                        String notes) {
            this.reservation = reservation;
            setCheckInTime(checkInTime);
            this.notes = notes;
        }

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public LocalDateTime getCheckInTime() { return checkInTime; }
        public void setCheckInTime(LocalDateTime checkInTime) {this.checkInTime = checkInTime;}

        public LocalDateTime getActualCheckoutTime() { return actualCheckoutTime; }
        public void setActualCheckoutTime(LocalDateTime actualCheckoutTime) throws Exception {
            if (actualCheckoutTime.isBefore(checkInTime)) {
                throw new Exception("Checkout time cannot be before check-in time.");
            }
            this.actualCheckoutTime = actualCheckoutTime;
        }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        @NotNull(message = "CheckIn must be tied to a Reservation")
        @OneToOne
        @JoinColumn(name = "reservation_id", nullable = false, unique = true)
        private Reservation reservation;

        public Reservation getReservation() { return reservation; }
        private void setReservation(Reservation reservation) { this.reservation = reservation; }

        @Override
        public String toString() {
            return "CheckIn @ " + checkInTime + (notes != null ? " | " + notes : "");
        }
    }
    public void changeReservationStatus(ReservationStatus newStatus) {
        switch (newStatus) {
            case Confirmed -> {
                if (this.reservationStatus == ReservationStatus.Cancelled)
                    throw new IllegalStateException("Cannot confirm a cancelled reservation.");
                if (this.reservationStatus == ReservationStatus.CheckedIn)
                    throw new IllegalStateException("Cannot change status of a checked-in reservation.");
            }
            case Cancelled -> {
                if (this.reservationStatus == ReservationStatus.CheckedIn)
                    throw new IllegalStateException("Cannot cancel a reservation after check-in.");
                if (this.reservationStatus == ReservationStatus.Completed)
                    throw new IllegalStateException("Cannot cancel a completed reservation.");
            }
            case CheckedIn ->
                    throw new IllegalStateException("Use createCheckIn() to perform check-in.");
            case Completed -> {
                if (this.reservationStatus != ReservationStatus.CheckedIn)
                    throw new IllegalStateException("Only a checked-in reservation can be completed.");
            }
            case Pending -> {
                if (this.reservationStatus != null)
                    throw new IllegalStateException("Cannot revert to Pending status.");
            }
        }
        this.reservationStatus = newStatus;
    }

    // =========================================================================
    // Use Case: Make a Reservation
    // =========================================================================
    public static Reservation createReservation(long customerId, LocalDate checkIn, LocalDate checkOut,
                                                List<Map.Entry<RoomType, Integer>> selectedRooms) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Customer customer = session.get(Customer.class, customerId);
            if (customer == null) throw new Exception("Customer not found.");

            Reservation reservation = new Reservation();
            reservation.setStartDate(checkIn);
            reservation.setEndDate(checkOut);
            reservation.setReservationDate(LocalDate.now());
            reservation.reservationStatus = ReservationStatus.Pending;
            reservation.setReference("RES-" + LocalDate.now().getYear() + "-" +
                    java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());

            customer.addReservation(reservation);

            for (Map.Entry<RoomType, Integer> entry : selectedRooms) {
                RoomType roomType = session.get(RoomType.class, entry.getKey().getId());
                ReservationRoomType rrt = new ReservationRoomType(reservation, roomType, entry.getValue());
                reservation.addReservationRoomType(rrt);
                roomType.addReservationRoomType(rrt);
            }

            session.save(reservation);
            session.update(customer);
            tx.commit();
            return reservation;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void completeReservation() throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Reservation managed = session.get(Reservation.class, this.id);
            if (managed == null) throw new Exception("Reservation not found.");
            managed.changeReservationStatus(ReservationStatus.Completed);
            session.update(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void cancelReservation() throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Reservation reservation = session.get(Reservation.class, this.id);
            if (reservation != null) {
                reservation.changeReservationStatus(ReservationStatus.Cancelled);
                session.update(reservation);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void confirmAndAwardPoints() throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Reservation reservation = session.get(Reservation.class, this.id);
            if (reservation == null) throw new Exception("Reservation not found.");
            reservation.changeReservationStatus(ReservationStatus.Confirmed);

            Customer customer = reservation.getCustomer();
            customer.addLoyaltyPoints(reservation.getLoyaltyPointsEarned());  // /loyaltyPointsEarned

            session.update(reservation);
            session.update(customer);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void confirmWithLoyaltyPoints(long customerId, int pointsToDeduct) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Reservation reservation = session.get(Reservation.class, this.id);
            if (reservation == null) throw new Exception("Reservation not found.");
            reservation.changeReservationStatus(ReservationStatus.Confirmed);

            Customer customer = session.get(Customer.class, customerId);
            if (customer == null) throw new Exception("Customer not found.");
            customer.deductLoyaltyPoints(pointsToDeduct);

            session.update(reservation);
            session.update(customer);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // =========================================================================
    // Use Case: Check In Customer
    // =========================================================================
    public void performCheckIn(Map<Long, Room> roomAssignments, String notes, Receptionist receptionist) throws Exception {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Reservation reservation = session.get(Reservation.class, this.id);
            if (reservation == null) throw new Exception("Reservation not found.");

            for (ReservationRoomType rrt : reservation.getReservationRoomTypes()) {
                Room room = roomAssignments.get(rrt.getId());
                if (room == null) throw new Exception("No room assigned for: " + rrt.getRoomType().getTypeName());
                rrt.setAssignedRoom(session.get(Room.class, room.getId()));
                session.update(rrt);
            }

            Receptionist managedReceptionist = session.get(Receptionist.class, receptionist.getId());
            managedReceptionist.addReservation(reservation);   // maintains qualified association map
            session.update(managedReceptionist);

            reservation.createCheckIn(LocalDateTime.now(), notes); // validates room readiness; sets CheckedIn
            session.update(reservation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }


    // /totalPrice = (endDate - startDate) * Σ roomType.basePrice
    // Static version — used in SummaryPanel before reservation is persisted.
    public static double calculateTotalPrice(LocalDate checkIn, LocalDate checkOut,
                                             List<Map.Entry<RoomType, Integer>> rooms) {
        if (checkIn == null || checkOut == null || rooms == null || rooms.isEmpty()) return 0.0;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) return 0.0;
        double total = 0.0;
        for (Map.Entry<RoomType, Integer> entry : rooms) {
            total += entry.getKey().getBasePrice() * nights;
        }
        return total;
    }

    // Instance version — used in ManageReservationDialog and confirmAndAwardPoints after reservation is saved.
    @Transient
    public double getTotalPrice() {
        if (startDate == null || endDate == null || reservationRoomTypes == null || reservationRoomTypes.isEmpty()) {
            return 0.0;
        }
        long numberOfNights = ChronoUnit.DAYS.between(startDate, endDate);
        if (numberOfNights <= 0) return 0.0;

        double totalPrice = 0.0;
        for (ReservationRoomType booking : reservationRoomTypes) {
            totalPrice += booking.getRoomType().getBasePrice();
        }
        return numberOfNights * totalPrice;
    }

    // /loyaltyPointsEarned = (endDate - startDate) * |reservationRoomTypes| * 10
    // Static version — used in SummaryPanel before reservation is persisted.
    public static int calculateLoyaltyPointsEarned(LocalDate checkIn, LocalDate checkOut, int roomCount) {
        if (checkIn == null || checkOut == null) return 0;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) return 0;
        return (int) (nights * roomCount * 10);
    }

    // Instance version — used in confirmAndAwardPoints after reservation is saved.
    @Transient
    public int getLoyaltyPointsEarned() {
        if (startDate == null || endDate == null || reservationRoomTypes == null || reservationRoomTypes.isEmpty()) {
            return 0;
        }

        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        if (nights <= 0) return 0;

        int numberOfRooms = reservationRoomTypes.size();
        int pointsPerRoomPerNight = 10;

        return (int) (nights * numberOfRooms * pointsPerRoomPerNight);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) | %s to %s",
                this.reference, this.reservationStatus, this.startDate, this.endDate);
    }
}
