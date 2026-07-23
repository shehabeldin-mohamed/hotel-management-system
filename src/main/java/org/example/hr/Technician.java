package org.example.hr;

import org.example.facility.MaintenanceRequest;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Technician extends Employee {

    @NotBlank(message = "Specialization cannot be blank")
    @Column(nullable = false)
    private String specialization;

    @NotEmpty(message = "A Technician must have at least one certificate")
    @ElementCollection
    @CollectionTable(name = "Technician_Certificates", joinColumns = @JoinColumn(name = "technician_id"))
    @Column(name = "certificate", nullable = false)
    private Set<String> certificates = new HashSet<>();

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();

    public List<MaintenanceRequest> viewAssignedMaintenanceRequests() {return maintenanceRequests;
    }

    public void addMaintenanceRequest(MaintenanceRequest request) {
        maintenanceRequests.add(request);
        request.setTechnician(this);
    }

    public Technician() {}

    public Technician(String firstName, String middleName, String lastName, LocalDate birthDate,
                      String employeeId, String plainPassword,
                      EmploymentType employmentType, LocalDate hireDate, String phoneNumber,
                      Double salary, Double hourlyRate, Integer hoursPerWeek,
                      String specialization, Set<String> certificates) {
        super(firstName, middleName, lastName, birthDate,
                employeeId, plainPassword,
                employmentType, hireDate, phoneNumber,
                salary, hourlyRate, hoursPerWeek);
        setSpecialization(specialization);
        setCertificates(certificates);
    }

    public List<MaintenanceRequest> getMaintenanceRequests() {return maintenanceRequests;}
    public void setMaintenanceRequests(List<MaintenanceRequest> maintenanceRequests) {this.maintenanceRequests = maintenanceRequests;}

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) {this.specialization = specialization;}

    public Set<String> getCertificates() { return certificates; }

    public void setCertificates(Set<String> certificates) {
        if (certificates == null || certificates.isEmpty()) {
            throw new IllegalArgumentException("A Technician must have at least one certificate [1..*].");
        }
        for (String cert : certificates) {
            if (cert == null || cert.trim().isEmpty()) {
                throw new IllegalArgumentException("Certificate name cannot be empty.");
            }
        }
        this.certificates = new HashSet<>(certificates);
    }

    public void addCertificate(String cert) {
        if (cert == null || cert.trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate cannot be empty.");
        }
        this.certificates.add(cert);
    }

    public void removeCertificate(String cert) {
        if (this.certificates.size() <= 1 && this.certificates.contains(cert)) {
            throw new IllegalStateException("Cannot remove the last certificate. A Technician must have at least one [1..*].");
        }
        this.certificates.remove(cert);
    }

    @Override
    public String toString() {
        return super.toString() + " | " + specialization;
    }
}
