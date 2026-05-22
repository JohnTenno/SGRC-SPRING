package com.app.modules.checkin;

import com.app.modules.cubicle.CubicleRepository;
import com.app.modules.cubicle.entity.Cubicle;
import com.app.modules.reservation.ReservationRepository;
import com.app.modules.reservation.entity.Reservation;
import com.app.modules.scheduler.ReservationSchedulerService;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import com.app.modules.websocket.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInService {

        @Autowired
        private CubicleRepository cubicleRepository;
        @Autowired
        private ReservationRepository reservationRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private WebSocketNotificationService wsService;
        @Autowired
        private ReservationSchedulerService schedulerService;

        @Transactional
        public Reservation checkIn(String qrToken, String enrollment) {
                Cubicle cubicle = cubicleRepository.findByQrToken(qrToken)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid QR"));

                User user = userRepository.findByEnrollment(enrollment)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                LocalDate today = LocalDate.now();
                LocalDateTime now = LocalDateTime.now();

                List<Reservation> candidates = reservationRepository.findForCheckIn(user.getId(), cubicle.getId(),
                                today);
                Reservation reservation = candidates.stream()
                                .filter(r -> !now.isAfter(LocalDateTime.of(today, r.getStartTime()).plusMinutes(15)))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "No reservation found for check-in or check-in window has passed"));

                reservation.setStatus("ACTIVE");
                reservationRepository.save(reservation);

                cubicle.setStatus("OCCUPIED");
                cubicleRepository.save(cubicle);

                schedulerService.cancelNoShowCheck(reservation.getId());
                schedulerService.scheduleCheckout(reservation);

                wsService.notifyCubicleCheckIn(cubicle.getId(), user.getFullName(), reservation.getId(),
                                reservation.getEndTime().toString());
                wsService.notifyAdminReservationUpdate(reservation.getId(), "ACTIVE");

                return reservation;
        }
}
