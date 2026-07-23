package org.example.facility;

import org.example.booking.ReservationRoomType;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Room number is required")
    @Column(nullable = false, unique = true)
    private String number;

    @NotNull(message = "Cleaning status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CleaningStatus cleaningStatus = CleaningStatus.Clean;

    @NotNull(message = "Maintenance status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus maintenanceStatus = MaintenanceStatus.Ok;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @OneToMany(mappedBy = "room")
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();

    public void addMaintenanceRequest(MaintenanceRequest request) {
        maintenanceRequests.add(request);
        request.setRoom(this);
    }

    @OneToMany(mappedBy = "room")
    private List<CleaningTask> cleaningTasks = new ArrayList<>();

    public void addCleaningTask(CleaningTask task) {
        cleaningTasks.add(task);
        task.setRoom(this);
    }

    @OneToMany(mappedBy = "assignedRoom")
    private List<ReservationRoomType> reservationRoomTypes = new ArrayList<>();

    public Room() {}

    public Room(String number) {
        this.number = number;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNumber() { return number; }
    public void setNumber(String number) {this.number = number;}

    public CleaningStatus getCleaningStatus() { return cleaningStatus; }

    public MaintenanceStatus getMaintenanceStatus() { return maintenanceStatus; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public List<MaintenanceRequest> getMaintenanceRequests() {return maintenanceRequests;}
    public void setMaintenanceRequests(List<MaintenanceRequest> maintenanceRequests) {this.maintenanceRequests = maintenanceRequests;}

    public List<CleaningTask> getCleaningTasks() {return cleaningTasks;}
    public void setCleaningTasks(List<CleaningTask> cleaningTasks) {this.cleaningTasks = cleaningTasks;}

    public List<ReservationRoomType> getReservationRoomTypes() {return reservationRoomTypes;}
    public void setReservationRoomTypes(List<ReservationRoomType> reservationRoomTypes) {this.reservationRoomTypes = reservationRoomTypes;}


    public void updateMaintenanceStatus(MaintenanceStatus maintenanceStatus) { this.maintenanceStatus = maintenanceStatus; }
    public void updateCleaningStatus(CleaningStatus cleaningStatus) { this.cleaningStatus = cleaningStatus; }

    public boolean isReadyForCheckIn() {
        return cleaningStatus == CleaningStatus.Clean
                && maintenanceStatus == MaintenanceStatus.Ok;
    }

    @Override
    public String toString() {
        return "Room " + number + "  [" + cleaningStatus + " / " + maintenanceStatus + "]";
    }
}
