package com.app.modules.notification;

import com.app.modules.notification.dto.NotificationResponseDto;
import com.app.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(Authentication authentication) {
        return userRepository.findByEnrollment(authentication.getName())
                .map(user -> ResponseEntity.ok(notificationService.getByUserId(user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return userRepository.findByEnrollment(authentication.getName())
                .map(user -> ResponseEntity.ok(Map.of("count", notificationService.countUnread(user.getId()))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        boolean updated = notificationService.markAsRead(id);
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
