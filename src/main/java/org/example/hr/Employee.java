package org.example.hr;

import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * Contract fields (FullTime/PartTime) are flattened into this class.
 * FullTime  → salary required,     hourlyRate/hoursPerWeek must be null.
 * PartTime  → hourlyRate + hoursPerWeek required, salary must be null.
 */
@Entity
public abstract class Employee extends Person{

    @NotBlank(message = "Employee ID is required")
    @Column(nullable = false, unique = true)
    private String employeeId;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;

    @NotNull(message = "Employment type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentType employmentType;

    @NotBlank(message = "Password hash is required")
    @Column(nullable = false)
    private String passwordHash;

    public Employee() {}

    public Employee(String firstName, String middleName, String lastName, LocalDate birthDate,
                    String employeeId, String plainPassword,
                    EmploymentType employmentType, LocalDate hireDate, String phoneNumber,
                    Double salary, Double hourlyRate, Integer hoursPerWeek) {
        super(firstName, middleName, lastName, birthDate);
        this.employeeId = employeeId;
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        setEmploymentType(employmentType);
        this.hireDate = hireDate;
        this.phoneNumber = phoneNumber;
        setSalary(salary);
        setHourlyRate(hourlyRate);
        setHoursPerWeek(hoursPerWeek);
    }


    // Flattened CONTRACT aspect — FullTime fields
    public static final double MIN_SALARY = 4800.0;
    public static final double MAX_SALARY = 9000.0;

    @DecimalMin(value = "4800.0", message = "Salary must be at least 4800")
    @DecimalMax(value = "9000.0", message = "Salary cannot exceed 9000")
    @Column(nullable = true)
    private Double salary;

    public Double getSalary() {return salary;}

    public void setSalary(Double salary) {
        if (employmentType == null) {
            throw new IllegalStateException("Employment type must be set before assigning contract fields.");
        }

        if (employmentType == EmploymentType.PartTime) {
            if (salary != null) {
                throw new IllegalStateException("Part-time employees cannot be assigned a salary.");
            }
            this.salary = null;
            return;
        }

        if (salary == null) {
            throw new IllegalArgumentException("Full-time employees must have a salary.");
        }
        if (salary < MIN_SALARY || salary > MAX_SALARY) {
            throw new IllegalArgumentException(
                    String.format("Salary must be between %.1f and %.1f.", MIN_SALARY, MAX_SALARY));
        }
        this.salary = salary;
    }


    // Flattened CONTRACT aspect — PartTime fields
    public static final double MIN_HOURLY_RATE = 31.0;
    public static final double MAX_HOURLY_RATE = 50.0;
    public static final int MIN_HOURS_PER_WEEK = 1;
    public static final int MAX_HOURS_PER_WEEK = 20;

    @DecimalMin(value = "31.0", message = "Hourly rate must be at least 31")
    @DecimalMax(value = "50.0", message = "Hourly rate cannot exceed 50")
    @Column(nullable = true)
    private Double hourlyRate;

    @Min(value = 1, message = "Hours per week must be at least 1")
    @Max(value = 20, message = "Hours per week cannot exceed 20")
    @Column(nullable = true)
    private Integer hoursPerWeek;

    public Double getHourlyRate() {return hourlyRate;}
    public void setHourlyRate(Double hourlyRate) {

        if (employmentType == null) {
            throw new IllegalStateException("Employment type must be set before assigning contract fields.");
        }

        if (employmentType == EmploymentType.FullTime) {
            if (hourlyRate != null) {
                throw new IllegalStateException("Full-time employees cannot be assigned an hourly rate.");
            }
            this.hourlyRate = null;
            return;
        }

        if (hourlyRate == null) {
            throw new IllegalArgumentException("Part-time employees must have an hourly rate.");
        }
        if (hourlyRate < MIN_HOURLY_RATE || hourlyRate > MAX_HOURLY_RATE) {
            throw new IllegalArgumentException(
                    String.format("Hourly rate must be between %.1f and %.1f.", MIN_HOURLY_RATE, MAX_HOURLY_RATE));
        }
        this.hourlyRate = hourlyRate;
    }

    public Integer getHoursPerWeek() {return hoursPerWeek;}
    public void setHoursPerWeek(Integer hoursPerWeek) {
        if (employmentType == null) {
            throw new IllegalStateException("Employment type must be set before assigning contract fields.");
        }
        if (employmentType == EmploymentType.FullTime) {
            if (hoursPerWeek != null) {
                throw new IllegalStateException("Full-time employees cannot be assigned hours per week.");
            }
            this.hoursPerWeek = null;
            return;
        }

        if (hoursPerWeek == null) {
            throw new IllegalArgumentException("Part-time employees must have hours per week assigned.");
        }
        if (hoursPerWeek < MIN_HOURS_PER_WEEK || hoursPerWeek > MAX_HOURS_PER_WEEK) {
            throw new IllegalArgumentException(
                    String.format("Hours per week must be between %d and %d.", MIN_HOURS_PER_WEEK, MAX_HOURS_PER_WEEK));
        }
        this.hoursPerWeek = hoursPerWeek;
    }

    // Derived Attribute /WeeklySalary = hourlyRate * hoursPerWeek
    @Transient
    public Double getWeeklySalary() {
        if (this.employmentType != EmploymentType.PartTime) {
            throw new IllegalStateException("Only part-time employees have a weekly salary calculation.");
        }
        if (hourlyRate != null && hoursPerWeek != null) {
            return hourlyRate * hoursPerWeek;
        }
        return 0.0;
    }

    public String getEmployeeId() {return employeeId;}
    public void setEmployeeId(String employeeId) {this.employeeId = employeeId;}

    public EmploymentType getEmploymentType() { return employmentType; }
    private void setEmploymentType(EmploymentType employmentType) {
        if (employmentType == null) throw new IllegalArgumentException("Employment type must be specified.");
        this.employmentType = employmentType;
    }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) {this.hireDate = hireDate;}

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

    public String getPasswordHash() {return passwordHash;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}

    @Override
    public String toString() {
        return getClass().getSimpleName() + " — " + getFirstName() + " " + getLastName()
                + "  (ID: " + employeeId + ")";
    }
}
