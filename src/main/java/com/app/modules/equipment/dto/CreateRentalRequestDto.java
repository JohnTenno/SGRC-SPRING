package com.app.modules.equipment.dto;

import java.util.List;

public class CreateRentalRequestDto {
    private List<ItemDto> items;

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public static class ItemDto {
        private Integer equipmentId;
        private Integer quantity;

        public Integer getEquipmentId() {
            return equipmentId;
        }

        public void setEquipmentId(Integer equipmentId) {
            this.equipmentId = equipmentId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
