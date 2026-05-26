package com.app.modules.cubicle;

import com.app.modules.cubicle.dto.CreateCubicleDto;
import com.app.modules.cubicle.dto.CubicleResponseDto;
import com.app.modules.cubicle.dto.UpdateCubicleDto;
import com.app.modules.reservation.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cubicles")
public class CubicleController {

    @Autowired
    private CubicleService cubicleService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public Page<CubicleResponseDto> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return cubicleService.findFiltered(search, status, minCapacity, pageable)
                .map(CubicleResponseDto::new);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CubicleResponseDto> getById(@PathVariable Integer id) {
        return cubicleService.findById(id)
                .map(CubicleResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CubicleResponseDto create(@RequestBody CreateCubicleDto dto) {
        return new CubicleResponseDto(cubicleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CubicleResponseDto> update(@PathVariable Integer id, @RequestBody UpdateCubicleDto dto) {
        return cubicleService.update(id, dto)
                .map(CubicleResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return cubicleService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/active-session")
    public ResponseEntity<?> getActiveSession(@PathVariable Integer id) {
        return reservationService.findCurrentActiveSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getOccupiedSlots(
            @PathVariable Integer id,
            @RequestParam("date") LocalDate date) {
        if (!cubicleService.findById(id).isPresent())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(reservationService.findOccupiedStartTimes(id, date));
    }
}