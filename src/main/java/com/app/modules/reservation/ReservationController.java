package com.app.modules.reservation;

import com.app.modules.reservation.dto.CreateReservationDto;
import com.app.modules.reservation.dto.ReservationResponseDto;
import com.app.modules.reservation.dto.UpdateReservationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public List<ReservationResponseDto> getAll() {
        return reservationService.findAll().stream()
                .map(ReservationResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> getById(@PathVariable Integer id) {
        return reservationService.findById(id)
                .map(ReservationResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ReservationResponseDto create(@RequestBody CreateReservationDto dto) {
        return new ReservationResponseDto(reservationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> update(@PathVariable Integer id,
            @RequestBody UpdateReservationDto dto) {
        return reservationService.update(id, dto)
                .map(ReservationResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return reservationService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
