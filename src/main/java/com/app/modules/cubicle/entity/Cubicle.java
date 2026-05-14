package com.app.modules.cubicle.entity;

import com.app.modules.building.entity.Building;
import jakarta.persistence.*;

@Entity
@Table(name = "cubicles")
public class Cubicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    private String identifier;
    private Integer capacity;
    private String status; // AVAILABLE, MAINTENANCE, OUT_OF_SERVICE

    public Cubicle() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
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
}
