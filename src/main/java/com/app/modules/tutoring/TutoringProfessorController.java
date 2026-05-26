package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutoring/professors")
public class TutoringProfessorController {

    @Autowired
    private TutoringProfessorService professorService;

    @GetMapping
    public ResponseEntity<List<ProfessorResponseDto>> getAll() {
        return ResponseEntity.ok(professorService.getAll());
    }

    @PostMapping
    public ResponseEntity<ProfessorResponseDto> create(@RequestBody ProfessorRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorService.create(dto));
    }

    @PutMapping("/{employeeNumber}")
    public ResponseEntity<ProfessorResponseDto> update(
            @PathVariable String employeeNumber,
            @RequestBody ProfessorRequestDto dto) {
        return professorService.update(employeeNumber, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{employeeNumber}")
    public ResponseEntity<Void> delete(@PathVariable String employeeNumber) {
        return professorService.delete(employeeNumber)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{employeeNumber}/offering")
    public ResponseEntity<OfferingResponseDto> getOffering(@PathVariable String employeeNumber) {
        return professorService.getOffering(employeeNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{employeeNumber}/offering")
    public ResponseEntity<OfferingResponseDto> updateOffering(
            @PathVariable String employeeNumber,
            @RequestBody OfferingRequestDto dto) {
        return professorService.updateOffering(employeeNumber, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
