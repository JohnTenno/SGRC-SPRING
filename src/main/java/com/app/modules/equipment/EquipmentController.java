package com.app.modules.equipment;

import com.app.modules.equipment.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    // ── Catalog ──────────────────────────────────────────────────────────────────

    @GetMapping("/equipment-types")
    public ResponseEntity<List<EquipmentTypeResponseDto>> getEquipmentTypes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String stockFilter) {
        return ResponseEntity.ok(equipmentService.getEquipmentTypes(search, stockFilter));
    }

    @PostMapping("/equipment-types")
    public ResponseEntity<EquipmentTypeResponseDto> createEquipmentType(@RequestBody CreateEquipmentTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipmentType(dto));
    }

    @PutMapping("/equipment-types/{id}")
    public ResponseEntity<EquipmentTypeResponseDto> updateEquipmentType(
            @PathVariable Integer id,
            @RequestBody UpdateEquipmentTypeDto dto) {
        return equipmentService.updateEquipmentType(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/equipment-types/{id}")
    public ResponseEntity<Void> deleteEquipmentType(@PathVariable Integer id) {
        try {
            return equipmentService.deleteEquipmentType(id)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ── Rental requests ───────────────────────────────────────────────────────────

    @PostMapping("/equipment-rental-requests")
    public ResponseEntity<?> createRentalRequest(
            @RequestBody CreateRentalRequestDto dto,
            Authentication authentication) {
        try {
            RentalRequestResponseDto response = equipmentService.createRentalRequest(dto, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/equipment-rental-requests")
    public ResponseEntity<List<AdminRentalRequestResponseDto>> getAdminRentalRequests(
            @RequestParam(required = false) List<String> status) {
        return ResponseEntity.ok(equipmentService.getAdminRentalRequests(status));
    }

    @GetMapping("/equipment-rental-requests/my")
    public ResponseEntity<List<AdminRentalRequestResponseDto>> getMyRentalRequests(Authentication authentication) {
        return ResponseEntity.ok(equipmentService.getStudentRentalRequests(authentication.getName()));
    }

    @GetMapping("/equipment-rental-requests/{id}")
    public ResponseEntity<AdminRentalRequestResponseDto> getRentalRequestById(@PathVariable Integer id) {
        return equipmentService.getRentalRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/equipment-rental-requests/{id}/status")
    public ResponseEntity<?> updateRentalRequestStatus(
            @PathVariable Integer id,
            @RequestBody UpdateRequestStatusDto dto) {
        return equipmentService.updateRentalRequestStatus(id, dto.getStatus())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
