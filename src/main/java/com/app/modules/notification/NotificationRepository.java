package com.app.modules.notification;

import com.app.modules.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByIdDesc(Integer userId);

    long countByUserIdAndIsReadFalse(Integer userId);
}
