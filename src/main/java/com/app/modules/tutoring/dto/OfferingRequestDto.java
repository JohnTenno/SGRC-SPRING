package com.app.modules.tutoring.dto;

import java.util.List;

public class OfferingRequestDto {
    private String scheduleSummary;
    private String tutoringLocation;
    private List<String> availableWeekdays;
    private List<Integer> subjectIds;

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
        this.availableWeekdays = availableWeekdays;
    }

    public List<Integer> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Integer> subjectIds) {
        this.subjectIds = subjectIds;
    }
}
