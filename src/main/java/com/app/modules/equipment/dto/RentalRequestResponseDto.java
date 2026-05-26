package com.app.modules.equipment.dto;

import com.app.modules.equipment.entity.EquipmentRentalRequest;

import java.util.List;

public class RentalRequestResponseDto {
    private Integer id;
    private String status;
    private String createdAt;
    private String pickupLocation;
    private List<ItemDto> items;

    public RentalRequestResponseDto(EquipmentRentalRequest request, List<ItemDto> items) {
        this.id = request.getId();
        this.status = request.getStatus();
        this.createdAt = request.getCreatedAt().toString();
        this.pickupLocation = "Biblioteca — mostrador de material solicitado";
        this.items = items;
    }

    public Integer getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public static class ItemDto {
        private Integer id;
        private String type;
        private Integer quantity;
        private Integer totalStock;
        private String logoUrl;
        private String imageAlt;

        public ItemDto(Integer id, String type, Integer quantity, Integer totalStock,
                String logoUrl, String imageAlt) {
            this.id = id;
            this.type = type;
            this.quantity = quantity;
            this.totalStock = totalStock;
            this.logoUrl = logoUrl;
            this.imageAlt = imageAlt;
        }

        public Integer getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getTotalStock() {
            return totalStock;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public String getImageAlt() {
            return imageAlt;
        }
    }
}
