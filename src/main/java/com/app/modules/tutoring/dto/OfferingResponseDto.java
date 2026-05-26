package com.app.modules.tutoring.dto;

import com.app.modules.tutoring.entity.TutoringOffering;

import java.util.List;

public class OfferingResponseDto {
    private String employeeNumber;
    private String scheduleSummary;
    private String tutoringLocation;
    private List<String> availableWeekdays;
    private List<Integer> subjectIds;

    public OfferingResponseDto(TutoringOffering o) {
        this.employeeNumber = o.getEmployeeNumber();
        this.scheduleSummary = o.getScheduleSummary();
        this.tutoringLocation = o.getTutoringLocation();
        this.availableWeekdays = o.getAvailableWeekdays();
        this.subjectIds = o.getSubjectIds();
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getScheduleSummary() {
        return scheduleSummary;
    }

    public String getTutoringLocation() {
        return tutoringLocation;
    }

    public List<String> getAvailableWeekdays() {
        return availableWeekdays;
    }

    public List<Integer> getSubjectIds() {
        return subjectIds;
    }
}
