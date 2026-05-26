package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.TutoringRequestResponseDto;
import com.app.modules.tutoring.dto.TutoringRequestSubmitDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutoring/requests")
public class TutoringRequestController {

    @Autowired
    private TutoringRequestService requestService;

    @PostMapping
    public ResponseEntity<?> submit(
            @RequestBody TutoringRequestSubmitDto dto,
            Authentication authentication) {
        try {
            TutoringRequestResponseDto response = requestService.submit(dto, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<TutoringRequestResponseDto>> getMyRequests(Authentication authentication) {
        return ResponseEntity.ok(requestService.getMyRequests(authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TutoringRequestResponseDto>> getAll() {
        return ResponseEntity.ok(requestService.getAll());
    }
}
