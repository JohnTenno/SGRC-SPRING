package com.app.modules.cubicle.dto;

import com.app.modules.cubicle.entity.Cubicle;

public class CubicleResponseDto {
    private Integer id;
    private Integer buildingId;
    private String buildingName;
    private String identifier;
    private Integer capacity;
    private String status;

    public CubicleResponseDto(Cubicle cubicle) {
        this.id = cubicle.getId();
        this.buildingId = cubicle.getBuilding().getId();
        this.buildingName = cubicle.getBuilding().getName();
        this.identifier = cubicle.getIdentifier();
        this.capacity = cubicle.getCapacity();
        this.status = cubicle.getStatus();
    }

    public Integer getId() {
        return id;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }
}
