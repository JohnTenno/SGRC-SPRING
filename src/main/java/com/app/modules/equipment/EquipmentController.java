package com.app.modules.equipment;

import com.app.modules.equipment.dto.CreateRentalRequestDto;
import com.app.modules.equipment.dto.EquipmentTypeResponseDto;
import com.app.modules.equipment.dto.RentalRequestResponseDto;
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

    @GetMapping("/equipment-types")
    public ResponseEntity<List<EquipmentTypeResponseDto>> getEquipmentTypes() {
        return ResponseEntity.ok(equipmentService.getEquipmentTypes());
    }

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
}
