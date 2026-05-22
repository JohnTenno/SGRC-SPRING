package com.app.modules.reservation;

import com.app.modules.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.cubicle.id = :cubicleId AND r.date = :date AND r.status NOT IN ('CANCELLED', 'ACTIVE', 'COMPLETED') ORDER BY r.startTime ASC")
    List<Reservation> findForCheckIn(@Param("userId") Integer userId,
            @Param("cubicleId") Integer cubicleId,
            @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'APPROVED' AND r.date >= :today")
    List<Reservation> findApprovedFromDate(@Param("today") LocalDate today);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.date >= :today")
    List<Reservation> findActiveFromDate(@Param("today") LocalDate today);

    @Query("SELECT r FROM Reservation r WHERE r.user.enrollment = :enrollment ORDER BY r.date DESC, r.startTime DESC")
    List<Reservation> findByUserEnrollment(@Param("enrollment") String enrollment);

    @Query("SELECT r FROM Reservation r WHERE r.cubicle.id = :cubicleId AND r.date = :date AND r.status NOT IN ('CANCELLED')")
    List<Reservation> findActiveByCubicleAndDate(@Param("cubicleId") Integer cubicleId, @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.cubicle.id = :cubicleId AND r.date = :date AND r.status = 'ACTIVE'")
    Optional<Reservation> findCurrentActiveSession(@Param("cubicleId") Integer cubicleId,
            @Param("date") LocalDate date);
}
