package com.app.modules.building.dto;

import com.app.modules.building.entity.Building;

public class BuildingResponseDto {
    private Integer id;
    private String name;
    private String location;

    public BuildingResponseDto(Building building) {
        this.id = building.getId();
        this.name = building.getName();
        this.location = building.getLocation();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
