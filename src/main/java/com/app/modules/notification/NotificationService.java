package com.app.modules.notification;

import com.app.modules.notification.dto.NotificationResponseDto;
import com.app.modules.notification.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void saveReservationNotification(Integer userId, Integer reservationId, String message) {
        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setReservationId(reservationId);
        n.setMessage(message);
        n.setRead(false);
        n.setExpirationDate(LocalDateTime.now().plusDays(7));
        notificationRepository.save(n);
    }

    @Transactional
    public void saveEquipmentNotification(Integer userId, String message) {
        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setReservationId(null);
        n.setMessage(message);
        n.setRead(false);
        n.setExpirationDate(LocalDateTime.now().plusDays(7));
        notificationRepository.save(n);
    }

    public List<NotificationResponseDto> getByUserId(Integer userId) {
        return notificationRepository.findByUserIdOrderByIdDesc(userId)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    public long countUnread(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public boolean markAsRead(Integer notificationId) {
        return notificationRepository.findById(notificationId).map(n -> {
            n.setRead(true);
            notificationRepository.save(n);
            return true;
        }).orElse(false);
    }
}
