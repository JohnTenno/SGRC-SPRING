package com.app.modules.equipment.dto;

import java.util.List;

public class AdminRentalRequestResponseDto {
    private Integer id;
    private String studentName;
    private String studentEnrollment;
    private String status;
    private String statusLabel;
    private String createdAt;
    private String itemsSummary;
    private Integer totalUnits;
    private List<ItemDto> items;

    public AdminRentalRequestResponseDto() {
    }

    public static String resolveStatusLabel(String status) {
        if (status == null)
            return "";
        return switch (status) {
            case "PENDING_PICKUP" -> "Pendiente de recoger";
            case "READY_FOR_PICKUP", "AWAITING_RETURN" -> "Equipo entregado";
            case "COMPLETED" -> "Completado";
            default -> status;
        };
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEnrollment() {
        return studentEnrollment;
    }

    public void setStudentEnrollment(String studentEnrollment) {
        this.studentEnrollment = studentEnrollment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }

    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    public static class ItemDto {
        private Integer id;
        private String type;
        private Integer quantity;
        private String image;

        public ItemDto(Integer id, String type, Integer quantity, String image) {
            this.id = id;
            this.type = type;
            this.quantity = quantity;
            this.image = image;
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

        public String getImage() {
            return image;
        }
    }
}
