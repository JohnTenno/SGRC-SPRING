package com.app.modules.scheduler;

import com.app.modules.cubicle.CubicleRepository;
import com.app.modules.cubicle.entity.Cubicle;
import com.app.modules.notification.NotificationRepository;
import com.app.modules.notification.entity.Notification;
import com.app.modules.penalty.PenaltyRepository;
import com.app.modules.penalty.entity.Penalty;
import com.app.modules.reservation.ReservationRepository;
import com.app.modules.reservation.entity.Reservation;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import com.app.modules.websocket.WebSocketNotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ReservationSchedulerService {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CubicleRepository cubicleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PenaltyRepository penaltyRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private WebSocketNotificationService wsService;

    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> checkoutTasks = new ConcurrentHashMap<>();

    public ReservationSchedulerService() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("no-show-");
        taskScheduler.initialize();
    }

    @PostConstruct
    public void rescheduleOnStartup() {
        List<Reservation> pending = reservationRepository.findApprovedFromDate(LocalDate.now());
        for (Reservation r : pending) {
            scheduleNoShowCheck(r);
        }
        List<Reservation> active = reservationRepository.findActiveFromDate(LocalDate.now());
        for (Reservation r : active) {
            scheduleCheckout(r);
        }
    }

    public void scheduleNoShowCheck(Reservation reservation) {
        LocalDateTime fireAt = LocalDateTime.of(reservation.getDate(), reservation.getStartTime())
                .plusMinutes(15);
        Instant instant = fireAt.atZone(ZoneId.systemDefault()).toInstant();

        if (instant.isBefore(Instant.now())) {
            taskScheduler.execute(() -> executeNoShowCheck(reservation.getId()));
            return;
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeNoShowCheck(reservation.getId()),
                instant);
        scheduledTasks.put(reservation.getId(), future);
    }

    public void cancelNoShowCheck(Integer reservationId) {
        ScheduledFuture<?> future = scheduledTasks.remove(reservationId);
        if (future != null) {
            future.cancel(false);
        }
    }

    public void scheduleCheckout(Reservation reservation) {
        LocalDateTime fireAt = LocalDateTime.of(reservation.getDate(), reservation.getEndTime());
        Instant instant = fireAt.atZone(ZoneId.systemDefault()).toInstant();

        if (instant.isBefore(Instant.now())) {
            taskScheduler.execute(() -> executeCheckout(reservation.getId()));
            return;
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeCheckout(reservation.getId()),
                instant);
        checkoutTasks.put(reservation.getId(), future);
    }

    public void cancelCheckout(Integer reservationId) {
        ScheduledFuture<?> future = checkoutTasks.remove(reservationId);
        if (future != null) {
            future.cancel(false);
        }
    }

    @Transactional
    public void executeCheckout(Integer reservationId) {
        checkoutTasks.remove(reservationId);

        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            if (!"ACTIVE".equals(reservation.getStatus()))
                return;

            reservation.setStatus("COMPLETED");
            reservationRepository.save(reservation);

            Cubicle cubicle = reservation.getCubicle();
            cubicle.setStatus("AVAILABLE");
            cubicleRepository.save(cubicle);

            wsService.notifyCubicleCheckout(cubicle.getId(), reservation.getId());
            wsService.notifyAdminReservationUpdate(reservation.getId(), "COMPLETED");
        });
    }

    @Transactional
    public void executeNoShowCheck(Integer reservationId) {
        scheduledTasks.remove(reservationId);

        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            if (!"APPROVED".equals(reservation.getStatus()))
                return;

            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);

            Cubicle cubicle = reservation.getCubicle();
            cubicle.setStatus("AVAILABLE");
            cubicleRepository.save(cubicle);

            User user = reservation.getUser();
            boolean isTeacher = "TEACHER".equals(user.getRole());

            if (!isTeacher) {
                applyNoShowPenalty(user, reservation);
            }

            wsService.notifyCubicleNoShow(cubicle.getId(), reservation.getId());
            wsService.notifyAdminReservationUpdate(reservation.getId(), "CANCELLED");

            String msg = isTeacher
                    ? "Your reservation was canceled because you did not show up. You may reschedule without penalty."
                    : "Your reservation was canceled because you did not show up. A penalty has been recorded on your account.";
            wsService.notifyUser(user.getId(), "NO_SHOW", msg);

            saveNotification(user.getId(), reservation.getId(), msg);
        });
    }

    private void applyNoShowPenalty(User user, Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();

        Penalty penalty = new Penalty();
        penalty.setUserId(user.getId());
        penalty.setType("NO_SHOW");
        penalty.setReason("Did not arrive at the cubicle within the 15-minute tolerance period. Reservation #"
                + reservation.getId());
        penalty.setStartDate(now);
        penalty.setEndDate(now.plusDays(3));
        penalty.setActive(true);
        penaltyRepository.save(penalty);

        userRepository.findById(user.getId()).ifPresent(u -> {
            u.setBlocked(true);
            userRepository.save(u);
        });
    }

    private void saveNotification(Integer userId, Integer reservationId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setReservationId(reservationId);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setExpirationDate(LocalDateTime.now().plusDays(60));
        notificationRepository.save(notification);
    }
}
