package org.example.hr;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "First name cannot be blank")
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = true)
    private String middleName;

    @NotBlank(message = "Last name cannot be blank")
    @Column(nullable = false)
    private String lastName;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    @Column(nullable = false)
    private LocalDate birthDate;

    public Person(){}

    public Person(String firstName, String middleName, String lastName, LocalDate birthDate) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        setBirthDate(birthDate);
    }

    // Derived Attribute /Age = current date - birthDate
    @Transient
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {this.firstName = firstName;}

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) {this.middleName = middleName;}

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {this.lastName = lastName;}

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) {
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future.");
        }
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return firstName + (middleName != null ? " " + middleName : "") + " " + lastName;
    }
}
