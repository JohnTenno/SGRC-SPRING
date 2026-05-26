package com.app.modules.equipment.dto;

import com.app.modules.equipment.entity.EquipmentType;

public class EquipmentTypeResponseDto {
    private Integer id;
    private String name;
    private String description;
    private String logoUrl;
    private Integer totalStock;

    public EquipmentTypeResponseDto(EquipmentType e) {
        this.id = e.getId();
        this.name = e.getName();
        this.description = e.getDescription();
        this.logoUrl = e.getLogoUrl();
        this.totalStock = e.getTotalStock();
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public Integer getTotalStock() {
        return totalStock;
    }
}
