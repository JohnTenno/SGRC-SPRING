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
        private Integer availableStock;
        private String image;
        private String imageAlt;

        public ItemDto(Integer id, String type, Integer quantity, Integer availableStock,
                String image, String imageAlt) {
            this.id = id;
            this.type = type;
            this.quantity = quantity;
            this.availableStock = availableStock;
            this.image = image;
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

        public Integer getAvailableStock() {
            return availableStock;
        }

        public String getImage() {
            return image;
        }

        public String getImageAlt() {
            return imageAlt;
        }
    }
}
