package com.app.modules.checkin;

import com.app.modules.reservation.entity.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping("/{qrToken}")
    public ResponseEntity<?> checkIn(@PathVariable String qrToken, Authentication auth) {
        try {
            Reservation reservation = checkInService.checkIn(qrToken, auth.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Check-in Successful",
                    "reservationId", reservation.getId(),
                    "status", reservation.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }
}
