package org.example.facility;

import org.example.HibernateUtil;
import org.example.booking.Reservation;
import org.example.booking.ReservationRoomType;
import org.example.booking.ReservationStatus;
import org.hibernate.Session;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Type name is required")
    @Column(nullable = false, unique = true)
    private String typeName;

    @DecimalMin(value = "50", message = "Base price must be strictly positive")
    @Column(nullable = false)
    private double basePrice;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private int maxCapacity;

    @Column(nullable = true)
    private String description;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    public void addRoom(Room room) {
        rooms.add(room);
        room.setRoomType(this);
    }

    @OneToMany(mappedBy = "roomType")
    private List<ReservationRoomType> reservationRoomTypes = new ArrayList<>();

    public void addReservationRoomType(ReservationRoomType reservationRoomType) {
        reservationRoomTypes.add(reservationRoomType);
        reservationRoomType.setRoomType(this);
    }

    public RoomType() {}

    public RoomType(String typeName, double basePrice, int maxCapacity, String description) {
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.maxCapacity = maxCapacity;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) {this.typeName = typeName;}

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) {this.basePrice = basePrice;}

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) {this.maxCapacity = maxCapacity;}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }

    public void setReservationRoomTypes(List<ReservationRoomType> reservationRoomTypes) {this.reservationRoomTypes = reservationRoomTypes;}
    public List<ReservationRoomType> getReservationRoomTypes() {return reservationRoomTypes;}

    // =========================================================================
    // Use Case: Make a Reservation
    // =========================================================================
    public static List<RoomType> searchAvailableRoomTypes(LocalDate checkIn, LocalDate checkOut) {
        List<RoomType> available = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<RoomType> all = session.createQuery("from RoomType", RoomType.class).list();
            for (RoomType rt : all) {
                int bookedCount = 0;
                for (ReservationRoomType rrt : rt.getReservationRoomTypes()) {
                    Reservation res = rrt.getReservation();
                    if (res.getReservationStatus() != ReservationStatus.Cancelled) {
                        if (datesOverlap(checkIn, checkOut, res.getStartDate(), res.getEndDate())) {
                            bookedCount++;
                        }
                    }
                }
                if (rt.getRooms().size() > bookedCount) {
                    available.add(rt);
                }
            }
        }
        return available;
    }

    private static boolean datesOverlap(LocalDate s1, LocalDate e1, LocalDate s2, LocalDate e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    // =========================================================================
    // Use Case: Check In Customer
    // =========================================================================
    public static List<Room> SearchAvailableRoomsForCheckIn(long roomTypeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select r from Room r where r.roomType.id = :typeId " +
                    "and r not in (select rrt.assignedRoom from ReservationRoomType rrt " +
                    "where rrt.reservation.reservationStatus = :status and rrt.assignedRoom is not null)", Room.class)
                    .setParameter("typeId", roomTypeId)
                    .setParameter("status", ReservationStatus.CheckedIn)
                    .list();
        }
    }

    @Override
    public String toString() {
        return String.format("%s - $%.1f/night (Max: %d guests)",
                this.typeName, this.basePrice, this.maxCapacity);
    }
}
