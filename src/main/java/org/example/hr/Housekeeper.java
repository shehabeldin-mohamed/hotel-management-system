package org.example.hr;

import org.example.facility.CleaningTask;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Housekeeper extends Employee{

    @NotBlank(message = "Assigned section cannot be blank")
    @Column(nullable = false)
    private String assignedSection;

    @NotNull(message = "Cart number is required")
    @Positive(message = "Cart number must be a positive number")
    @Column(nullable = false)
    private int cartNumber;

    @OneToMany(mappedBy = "housekeeper", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<CleaningTask> cleaningTasks = new ArrayList<>();

    public void addCleaningTask(CleaningTask task) {
        cleaningTasks.add(task);
        task.setHousekeeper(this);
    }

    public Housekeeper() {}

    public Housekeeper(String firstName, String middleName, String lastName, LocalDate birthDate,
                       String employeeId, String plainPassword,
                       EmploymentType employmentType, LocalDate hireDate, String phoneNumber,
                       Double salary, Double hourlyRate, Integer hoursPerWeek,
                       String assignedSection, int cartNumber) {
        super(firstName, middleName, lastName, birthDate,
                employeeId, plainPassword,
                employmentType, hireDate, phoneNumber,
                salary, hourlyRate, hoursPerWeek);
        setAssignedSection(assignedSection);
        setCartNumber(cartNumber);
    }

    public String getAssignedSection() { return assignedSection; }
    public void setAssignedSection(String assignedSection) {this.assignedSection = assignedSection;}

    public int getCartNumber() { return cartNumber; }
    public void setCartNumber(int cartNumber) {this.cartNumber = cartNumber;}

    public List<CleaningTask> viewAssignedCleaningTasks() {return cleaningTasks;}
    public void setCleaningTasks(List<CleaningTask> cleaningTasks) {this.cleaningTasks = cleaningTasks;}

    @Override
    public String toString() {
        return super.toString() + " | Section: " + assignedSection + " | Cart: " + cartNumber;
    }
}
