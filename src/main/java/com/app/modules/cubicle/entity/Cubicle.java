package com.app.modules.cubicle.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cubicle")
public class Cubicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cubicle_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 80)
    private String identifier;

    @Column(name = "max_capacity", nullable = false)
    private Integer capacity;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "logo_url", nullable = false, length = 255)
    private String logoUrl;

    @Column(name = "qr_token", nullable = false, unique = true, length = 100)
    private String qrToken;

    public Cubicle() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
