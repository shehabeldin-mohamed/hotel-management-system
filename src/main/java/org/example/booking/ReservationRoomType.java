package org.example.booking;

import org.example.facility.Room;
import org.example.facility.RoomType;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
public class ReservationRoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Min(value = 1, message = "Guests must be at least 1")
    @Column(nullable = false)
    private int numberOfGuests;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // ReservationRoomType 0..* ——— 1 RoomType
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    // Assigned at check-in time (null until receptionist assigns a physical room)
    @ManyToOne
    @JoinColumn(name = "assigned_room_id", nullable = true)
    private Room assignedRoom;

    public ReservationRoomType() {}

    public ReservationRoomType(Reservation reservation, RoomType roomType, int numberOfGuests) {
        this.reservation = reservation;
        this.roomType = roomType;
        this.numberOfGuests = numberOfGuests;
    }

    public long getId() { return id; }
    public void setId(long id) {this.id = id;}

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) {this.numberOfGuests = numberOfGuests;}

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Room getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(Room assignedRoom) {this.assignedRoom = assignedRoom;}

    // Guard used by Reservation.createCheckIn() — all slots must be assigned before check-in
    public boolean isRoomAssigned() { return assignedRoom != null; }

    @Override
    public String toString() {
        return String.format("%s (%d guests)",
                this.roomType.getTypeName(), this.numberOfGuests);
    }
}
