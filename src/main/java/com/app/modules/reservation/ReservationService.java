package com.app.modules.reservation;

import com.app.modules.cubicle.CubicleRepository;
import com.app.modules.messaging.publisher.ReservationEventPublisher;
import com.app.modules.reservation.dto.CreateMyReservationDto;
import com.app.modules.reservation.dto.CreateReservationDto;
import com.app.modules.reservation.dto.UpdateReservationDto;
import com.app.modules.reservation.entity.Reservation;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CubicleRepository cubicleRepository;

    @Autowired
    private ReservationEventPublisher eventPublisher;

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(Integer id) {
        return reservationRepository.findById(id);
    }

    public Reservation create(CreateReservationDto dto) {
        Reservation reservation = new Reservation();
        userRepository.findById(dto.getUserId()).ifPresent(reservation::setUser);
        cubicleRepository.findById(dto.getCubicleId()).ifPresent(reservation::setCubicle);
        reservation.setDate(dto.getDate());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        Reservation saved = reservationRepository.save(reservation);
        eventPublisher.publishCreated(saved);
        return saved;
    }

    public Optional<Reservation> update(Integer id, UpdateReservationDto dto) {
        return reservationRepository.findById(id).map(reservation -> {
            if (dto.getUserId() != null)
                userRepository.findById(dto.getUserId()).ifPresent(reservation::setUser);
            if (dto.getCubicleId() != null)
                cubicleRepository.findById(dto.getCubicleId()).ifPresent(reservation::setCubicle);
            if (dto.getDate() != null)
                reservation.setDate(dto.getDate());
            if (dto.getStartTime() != null)
                reservation.setStartTime(dto.getStartTime());
            if (dto.getEndTime() != null)
                reservation.setEndTime(dto.getEndTime());
            if (dto.getStatus() != null)
                reservation.setStatus(dto.getStatus());

            Reservation saved = reservationRepository.save(reservation);
            eventPublisher.publishUpdated(saved);
            return saved;
        });
    }

    public boolean delete(Integer id) {
        if (!reservationRepository.existsById(id))
            return false;
        reservationRepository.deleteById(id);
        return true;
    }

    public List<Reservation> findByUserEnrollment(String enrollment) {
        return reservationRepository.findByUserEnrollment(enrollment);
    }

    public Reservation createForUser(String enrollment, CreateMyReservationDto dto) {
        User user = userRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        cubicleRepository.findById(dto.getCubicleId()).ifPresent(reservation::setCubicle);
        reservation.setDate(dto.getDate());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setStatus("APPROVED");

        Reservation saved = reservationRepository.save(reservation);
        eventPublisher.publishCreated(saved);
        return saved;
    }

    public Reservation cancelReservation(Integer id, String enrollment) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No found reservation with id " + id));

        if (!reservation.getUser().getEnrollment().equals(enrollment)) {
            throw new IllegalStateException("You dont have permission to cancel this reservation");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new IllegalStateException("This reservation is already cancelled");
        }
        if ("ACTIVE".equals(reservation.getStatus())) {
            throw new IllegalStateException("You cannot cancel an active reservation");
        }

        LocalDateTime deadline = LocalDateTime.of(reservation.getDate(), reservation.getStartTime())
                .minusHours(1);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalStateException("You can only cancel with at least 1 hour of advance notice");
        }

        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }

    public java.util.Optional<java.util.Map<String, String>> findCurrentActiveSession(Integer cubicleId) {
        return reservationRepository.findCurrentActiveSession(cubicleId, java.time.LocalDate.now())
                .map(r -> {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("studentName", r.getUser().getFullName());
                    map.put("endTime", r.getEndTime().toString());
                    return map;
                });
    }

    public List<String> findOccupiedStartTimes(Integer cubicleId, java.time.LocalDate date) {
        return reservationRepository.findActiveByCubicleAndDate(cubicleId, date)
                .stream()
                .flatMap(r -> {
                    List<LocalTime> hours = new java.util.ArrayList<>();
                    LocalTime current = r.getStartTime();
                    while (current.isBefore(r.getEndTime())) {
                        hours.add(current);
                        current = current.plusHours(1);
                    }
                    return hours.stream();
                })
                .map(t -> {
                    String s = t.toString();
                    return s.length() > 5 ? s.substring(0, 5) : s;
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
