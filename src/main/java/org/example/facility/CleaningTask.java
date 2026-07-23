package org.example.facility;

import org.example.hr.Housekeeper;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Assigned date is required")
    @Column(nullable = false)
    private LocalDateTime assignedDate;

    @Column(nullable = true)
    private LocalDateTime completionTime;

    @Column(nullable = true)
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status = WorkStatus.Pending;

    @ManyToOne(optional = false)
    @JoinColumn(name = "housekeeper_id", nullable = false)
    private Housekeeper housekeeper;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public CleaningTask() {}

    public CleaningTask(Housekeeper housekeeper, Room room, String description) {
        this.housekeeper = housekeeper;
        this.room = room;
        this.description = description;
        this.assignedDate = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) {this.id = id;}

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) {this.assignedDate = assignedDate;}

    public LocalDateTime getCompletionTime() { return completionTime; }
    public void setCompletionTime(LocalDateTime completionTime) {
        if (completionTime.isBefore(this.assignedDate)) {
            throw new IllegalArgumentException("Completion time cannot be before assigned date.");
        }
        this.completionTime = completionTime;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WorkStatus getStatus() { return status; }
    public void setStatus(WorkStatus status) {this.status = status;}

    public Housekeeper getHousekeeper() { return housekeeper; }
    public void setHousekeeper(Housekeeper housekeeper) {this.housekeeper = housekeeper;}

    public Room getRoom() { return room; }
    public void setRoom(Room room) {this.room = room;}

    @Override
    public String toString() {
        return "Room " + room.getNumber() + " | " + status
                + (description != null ? " | " + description : "");
    }
}
