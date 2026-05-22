package com.app.modules.cubicle.dto;

import com.app.modules.cubicle.entity.Cubicle;

public class CubicleResponseDto {
    private Integer id;
    private String identifier;
    private Integer capacity;
    private String status;
    private String qrToken;

    public CubicleResponseDto(Cubicle cubicle) {
        this.id = cubicle.getId();
        this.identifier = cubicle.getIdentifier();
        this.capacity = cubicle.getCapacity();
        this.status = cubicle.getStatus();
        this.qrToken = cubicle.getQrToken();
    }

    public Integer getId() {
        return id;
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

    public String getQrToken() {
        return qrToken;
    }
}
