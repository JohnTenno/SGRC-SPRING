package com.app.modules.faculty.dto;

import com.app.modules.faculty.entity.Faculty;

public class FacultyResponseDto {
    private Integer id;
    private String name;

    public FacultyResponseDto(Faculty faculty) {
        this.id = faculty.getId();
        this.name = faculty.getName();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
