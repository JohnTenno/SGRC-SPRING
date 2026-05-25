package com.app.modules.equipment.dto;

import com.app.modules.equipment.entity.EquipmentType;

public class EquipmentTypeResponseDto {
    private Integer id;
    private String type;
    private String category;
    private String description;
    private String logoUrl;
    private Integer availableStock;
    private Integer totalStock;

    public EquipmentTypeResponseDto(EquipmentType e) {
        this.id = e.getId();
        this.type = e.getName();
        this.category = e.getCategory();
        this.description = e.getDescription();
        this.logoUrl = e.getLogoUrl();
        this.availableStock = e.getAvailableStock();
        this.totalStock = e.getTotalStock();
    }

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getTotalStock() {
        return totalStock;
    }
}
