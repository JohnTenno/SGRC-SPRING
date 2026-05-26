package com.app.modules.tutoring.dto;

import com.app.modules.tutoring.entity.TutoringSubject;

public class SubjectResponseDto {
    private Integer id;
    private String name;
    private String description;

    public SubjectResponseDto(TutoringSubject s) {
        this.id = s.getId();
        this.name = s.getName();
        this.description = s.getDescription();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
