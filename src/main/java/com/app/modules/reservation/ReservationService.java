package com.app.modules.reservation;

import com.app.modules.cubicle.CubicleRepository;
import com.app.modules.messaging.publisher.ReservationEventPublisher;
import com.app.modules.reservation.dto.CreateReservationDto;
import com.app.modules.reservation.dto.UpdateReservationDto;
import com.app.modules.reservation.entity.Reservation;
import com.app.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
