package org.example.hr;

import org.example.booking.Reservation;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.*;

@Entity
public class Receptionist extends Employee {

    @NotNull(message = "Desk number is required")
    @Positive(message = "Desk number must be positive")
    @Column(nullable = false)
    private int deskNumber;

    @NotEmpty(message = "A Receptionist must speak at least one language")
    @ElementCollection
    @CollectionTable(name = "Receptionist_Languages", joinColumns = @JoinColumn(name = "receptionist_id"))
    @Column(name = "language", nullable = false)
    private Set<String> languages = new HashSet<>();

    @OneToMany(mappedBy = "receptionist")
    @MapKey(name = "reference")
    private Map<String, Reservation> handledReservations = new HashMap<>();

    public void addReservation(Reservation reservation) {
        if (reservation == null) throw new IllegalArgumentException("Reservation cannot be null.");
        handledReservations.put(reservation.getReference(), reservation);
        reservation.setReceptionist(this);
    }

    public Reservation findReservation(String reference) {
        if(!handledReservations.containsKey(reference)){
            throw new NoSuchElementException("Unable to find a reservation: " + reference);
        }
        return handledReservations.get(reference);
    }

    public Receptionist() {}

    public Receptionist(String firstName, String middleName, String lastName, LocalDate birthDate,
                        String employeeId, String plainPassword,
                        EmploymentType employmentType, LocalDate hireDate, String phoneNumber,
                        Double salary, Double hourlyRate, Integer hoursPerWeek,
                        int deskNumber, Set<String> languages) {
        super(firstName, middleName, lastName, birthDate,
                employeeId, plainPassword,
                employmentType, hireDate, phoneNumber,
                salary, hourlyRate, hoursPerWeek);
        setDeskNumber(deskNumber);
        setLanguages(languages);
    }

    public int getDeskNumber() { return deskNumber; }
    public void setDeskNumber(int deskNumber) {this.deskNumber = deskNumber;}

    public Set<String> getLanguages() { return languages; }

    public void setLanguages(Set<String> languages) {
        if (languages == null || languages.isEmpty()) {
            throw new IllegalArgumentException("A Receptionist must speak at least one language [1..*].");
        }
        for (String lang : languages) {
            if (lang == null || lang.trim().isEmpty()) {
                throw new IllegalArgumentException("Language cannot be empty.");
            }
        }
        this.languages = new HashSet<>(languages);
    }

    public void addLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be empty.");
        }
        this.languages.add(lang);
    }

    public void removeLanguage(String lang) {
        if (this.languages.size() <= 1 && this.languages.contains(lang)) {
            throw new IllegalStateException("Cannot remove the last language. A Receptionist must speak at least one [1..*].");
        }
        this.languages.remove(lang);
    }

    public Map<String, Reservation> getHandledReservations() { return handledReservations; }
    public void setHandledReservations(Map<String, Reservation> handledReservations) {this.handledReservations = handledReservations;}

    @Override
    public String toString() {
        return super.toString() + " | Desk: " + deskNumber;
    }
}
