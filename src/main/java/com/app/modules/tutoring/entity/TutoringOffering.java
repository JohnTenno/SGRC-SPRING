package com.app.modules.tutoring.entity;

import com.app.modules.tutoring.util.IntegerListConverter;
import com.app.modules.tutoring.util.StringListConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutoring_offering")
public class TutoringOffering {

    @Id
    @Column(name = "employee_number", nullable = false, length = 20)
    private String employeeNumber;

    @Column(name = "schedule_summary", length = 50)
    private String scheduleSummary;

    @Column(name = "tutoring_location", length = 120)
    private String tutoringLocation;

    @Convert(converter = StringListConverter.class)
    @Column(name = "available_weekdays", columnDefinition = "TEXT")
    private List<String> availableWeekdays = new ArrayList<>();

    @Convert(converter = IntegerListConverter.class)
    @Column(name = "subject_ids", columnDefinition = "TEXT")
    private List<Integer> subjectIds = new ArrayList<>();

    public TutoringOffering() {
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getScheduleSummary() {
        return scheduleSummary;
    }

    public void setScheduleSummary(String scheduleSummary) {
        this.scheduleSummary = scheduleSummary;
    }

    public String getTutoringLocation() {
        return tutoringLocation;
    }

    public void setTutoringLocation(String tutoringLocation) {
        this.tutoringLocation = tutoringLocation;
    }

    public List<String> getAvailableWeekdays() {
        return availableWeekdays;
    }

    public void setAvailableWeekdays(List<String> availableWeekdays) {
        this.availableWeekdays = availableWeekdays != null ? availableWeekdays : new ArrayList<>();
    }

    public List<Integer> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Integer> subjectIds) {
        this.subjectIds = subjectIds != null ? subjectIds : new ArrayList<>();
    }
}
