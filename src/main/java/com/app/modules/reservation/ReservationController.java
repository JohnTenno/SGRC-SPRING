package com.app.modules.reservation;

import com.app.modules.reservation.dto.CreateMyReservationDto;
import com.app.modules.reservation.dto.CreateReservationDto;
import com.app.modules.reservation.dto.ReservationResponseDto;
import com.app.modules.reservation.dto.UpdateReservationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping("/my")
    public List<ReservationResponseDto> getMy(Authentication auth) {
        return reservationService.findByUserEnrollment(auth.getName()).stream()
                .map(ReservationResponseDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/my")
    public ReservationResponseDto createMy(@RequestBody CreateMyReservationDto dto, Authentication auth) {
        return new ReservationResponseDto(reservationService.createForUser(auth.getName(), dto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id, Authentication auth) {
        try {
            return ResponseEntity.ok(new ReservationResponseDto(
                    reservationService.cancelReservation(id, auth.getName())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
