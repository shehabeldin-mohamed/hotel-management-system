package org.example.facility;

import org.example.hr.Technician;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Request date is required")
    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column(nullable = true)
    private LocalDateTime resolutionDate;

    @Column(nullable = true)
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status = WorkStatus.Pending;

    @ManyToOne(optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public MaintenanceRequest() {}

    public MaintenanceRequest(Technician technician, Room room, String description) {
        this.technician = technician;
        this.room = room;
        this.description = description;
        this.requestDate = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) {this.id = id;}

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) {this.requestDate = requestDate;}

    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDateTime resolutionDate) {
        if (resolutionDate.isBefore(this.requestDate)) {
            throw new IllegalArgumentException("Resolution date cannot be before request date.");
        }
        this.resolutionDate = resolutionDate;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WorkStatus getStatus() { return status; }
    public void setStatus(WorkStatus status) {this.status = status;}

    public Technician getTechnician() { return technician; }
    public void setTechnician(Technician technician) {this.technician = technician;}

    public Room getRoom() { return room; }
    public void setRoom(Room room) {this.room = room;}

    @Override
    public String toString() {
        return "Room " + room.getNumber() + " | " + status
                + (description != null ? " | " + description : "");
    }
}
