package com.app.modules.equipment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_rental_request_item")
public class EquipmentRentalRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer id;

    @Column(name = "request_id", nullable = false)
    private Integer requestId;

    @Column(name = "equipment_type_id", nullable = false)
    private Integer equipmentTypeId;

    @Column(nullable = false)
    private Integer quantity;

    public EquipmentRentalRequestItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getEquipmentTypeId() {
        return equipmentTypeId;
    }

    public void setEquipmentTypeId(Integer equipmentTypeId) {
        this.equipmentTypeId = equipmentTypeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
